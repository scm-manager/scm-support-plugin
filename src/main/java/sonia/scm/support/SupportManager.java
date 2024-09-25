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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.support.collector.Collector;
import sonia.scm.support.collector.CollectorContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public final class SupportManager
{

  /** Field description */
  private static final String NAME = "support";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param blobStoreFactory
   * @param collectors
   */
  @Inject
  public SupportManager(BlobStoreFactory blobStoreFactory,
    Set<Collector> collectors)
  {
    this.blobStore = blobStoreFactory.withName(NAME).build();
    this.collectors = collectors;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public Blob collectSupportData() throws IOException
  {
    SupportHandler handler = null;

    try
    {
      handler = new SupportHandler(blobStore);
      collectSupportData(handler);
    }
    finally
    {
      Closeables.close(handler, true);
    }

    return handler.getZipBlob();
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public synchronized Blob disableTraceLogging() throws IOException
  {
    if (loggingHandler == null)
    {
      throw new IllegalStateException("logging handler not set, could not stop");
    }

    try {
      processingLog = true;
      return createBlob();
    } finally {
      processingLog = false;
    }
  }

  private Blob createBlob() throws IOException {
    SupportHandler supportHandler = loggingHandler.getSupportHandler();

    try
    {
      loggingHandler.disable();
      collectSupportData(supportHandler);
    }
    finally
    {
      Closeables.close(loggingHandler.getSupportHandler(), true);
    }

    Blob blob = loggingHandler.getSupportHandler().getZipBlob();

    loggingHandler = null;

    return blob;
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  public synchronized void enableTraceLogging() throws IOException
  {
    if (loggingHandler != null)
    {
      throw new IllegalStateException("logging handler already set, could not start again");
    }

    loggingHandler = new LoggingHandler(new SupportHandler(blobStore));
    loggingHandler.enable();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isTraceLoggingEnabled()
  {
    return !isProcessingLog() && loggingHandler != null;
  }

  public boolean isProcessingLog() {
    return processingLog;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param supportHandler
   *
   * @throws IOException
   */
  private void collectSupportData(SupportHandler supportHandler)
    throws IOException
  {
    CollectorContext context =
      new CollectorContext(supportHandler.getZipOutputStream());

    for (Collector collector : collectors)
    {
      collector.collect(context);
    }
  }

  //~--- fields ---------------------------------------------------------------

  private final BlobStore blobStore;
  private final Set<Collector> collectors;

  private boolean processingLog = false;
  private LoggingHandler loggingHandler;
}
