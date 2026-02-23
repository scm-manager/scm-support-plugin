/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.support;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.ContextEntry;
import sonia.scm.EagerSingleton;
import sonia.scm.NotFoundException;
import sonia.scm.plugin.Extension;
import sonia.scm.schedule.Scheduler;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.support.collector.Collector;
import sonia.scm.support.collector.CollectorContext;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Slf4j
@Extension
@EagerSingleton
class SupportManager {

  private static final String NAME = "support";
  private static final String HOURLY = "0 0 * * * ?";

  private final BlobStore blobStore;
  private final Set<Collector> collectors;

  private boolean processingLog = false;
  private LoggingHandler loggingHandler;

  @Inject
  public SupportManager(BlobStoreFactory blobStoreFactory, Set<Collector> collectors, Scheduler scheduler) {
    this.blobStore = blobStoreFactory.withName(NAME).build();
    this.collectors = collectors;
    scheduleCleanUp(scheduler);
  }

  public Blob collectSupportData() throws IOException {
    SupportHandler handler = null;

    log.info("gathering support data");

    try {
      handler = new SupportHandler(blobStore, "simple");
      collectSupportData(handler);
    } finally {
      Closeables.close(handler, true);
    }

    return handler.getZipBlob();
  }

  public synchronized Blob disableTraceLogging() throws IOException {
    log.info("disabling trace logging");
    if (loggingHandler == null) {
      throw new IllegalStateException("logging handler not set, could not stop");
    }

    try {
      processingLog = true;
      return createBlob();
    } finally {
      processingLog = false;
    }
  }

  public Collection<SupportPackage> getAll() {
    List<SupportPackage> all = blobStore.getAll().stream().map(SupportPackage::from).toList();
    getCurrentId()
      .flatMap(runningId -> all.stream().filter(p -> p.getBlob().getId().equals(runningId)).findAny())
      .ifPresent(p -> p.setRunning(true));
    return all;
  }

  public synchronized void enableTraceLogging() throws IOException {
    log.info("enabling trace logging");
    if (loggingHandler != null) {
      throw new IllegalStateException("logging handler already set, could not start again");
    }

    loggingHandler = new LoggingHandler(new SupportHandler(blobStore, "trace"));
    loggingHandler.enable();
  }

  public boolean isTraceLoggingEnabled() {
    return !isProcessingLog() && loggingHandler != null;
  }

  public boolean isProcessingLog() {
    return processingLog;
  }

  public SupportPackage get(String id) {
    return blobStore.getOptional(id)
      .map(SupportPackage::from)
      .orElseThrow(() -> NotFoundException.notFound(ContextEntry.ContextBuilder.entity("Support Package", id)));
  }

  public Blob download(String id) {
    return blobStore.getOptional(id)
      .orElseThrow(() -> NotFoundException.notFound(ContextEntry.ContextBuilder.entity("Support Package", id)));
  }

  public synchronized void delete(String id) {
    log.info("delete support package {}", id);
    doThrow().violation("running package cannot be deleted", id).when(isRunning(id));
    blobStore.remove(id);
  }

  private boolean isRunning(String blobId) {
    return getCurrentId()
      .map(s -> s.equals(blobId))
      .orElse(false);
  }

  @VisibleForTesting
  Optional<String> getCurrentId() {
    return ofNullable(loggingHandler)
      .map(LoggingHandler::getSupportHandler)
      .flatMap(SupportHandler::currentId);
  }

  private Blob createBlob() throws IOException {
    log.trace("creating blob");
    SupportHandler supportHandler = loggingHandler.getSupportHandler();

    try {
      loggingHandler.disable();
      collectSupportData(supportHandler);
    } finally {
      Closeables.close(loggingHandler.getSupportHandler(), true);
    }

    Blob blob = loggingHandler.getSupportHandler().getZipBlob();

    loggingHandler = null;

    log.trace("created blob with id {}", blob.getId());

    return blob;
  }

  private void collectSupportData(SupportHandler supportHandler) throws IOException {
    CollectorContext context = new CollectorContext(supportHandler.getZipOutputStream());

    for (Collector collector : collectors) {
      collector.collect(context);
    }
  }

  private void cleanUp() {
    log.debug("clean up support packages");
    Instant limit = Instant.now().minus(1, ChronoUnit.WEEKS);
    getAll().stream()
      .filter(supportPackage -> limit.isAfter(supportPackage.getCreationDate()))
      .map(SupportPackage::getBlob)
      .map(Blob::getId)
      .forEach(this::delete);
  }

  private void scheduleCleanUp(Scheduler scheduler) {
    scheduler.schedule(HOURLY, this::cleanUp);
  }
}
