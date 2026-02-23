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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.apache.shiro.SecurityUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SupportHandler implements Closeable {

  private static final String PATTERN =
    "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%-10X{transaction_id}] %-5level %logger - %msg%n";

  private final BlobStore blobStore;
  private final String type;

  private Blob loggingBlob;
  private OutputStream loggingOutputStream;
  private OutputStreamAppender<ILoggingEvent> outputStreamAppender;
  private Blob zipBlob;
  private ZipOutputStream zipOutputStream;

  public SupportHandler(BlobStore blobStore, String type) {
    this.blobStore = blobStore;
    this.type = type;
  }

  @Override
  public void close() throws IOException {
    if (outputStreamAppender != null) {
      outputStreamAppender.stop();
      Closeables.close(loggingOutputStream, true);
      loggingBlob.commit();

      if (zipOutputStream != null) {
        zipOutputStream.putNextEntry(new ZipEntry("scm-manager.log"));

        InputStream input = null;

        try {
          input = loggingBlob.getInputStream();
          ByteStreams.copy(input, zipOutputStream);
        } finally {
          Closeables.close(input, true);
          zipOutputStream.closeEntry();
        }
      }

      blobStore.remove(loggingBlob);
    }

    Closeables.close(zipOutputStream, true);
  }

  public LoggerContext getLoggerContext() {
    ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

    if (!(loggerFactory instanceof LoggerContext)) {
      throw new IllegalStateException();
    }

    return (LoggerContext) loggerFactory;
  }

  public OutputStreamAppender<ILoggingEvent> getOutputStreamAppender() throws IOException {
    if (outputStreamAppender == null) {
      outputStreamAppender = createOutputStreamAppender();
    }

    return outputStreamAppender;
  }

  Optional<String> currentId() {
    return Optional.ofNullable(loggingBlob).map(Blob::getId);
  }

  Blob getZipBlob() {
    if (zipBlob == null) {
      zipBlob = blobStore.create(createBlobId(type));
    }

    return zipBlob;
  }

  public ZipOutputStream getZipOutputStream() throws IOException {
    if (zipOutputStream == null) {
      zipOutputStream = new ZipOutputStream(getZipBlob().getOutputStream());
    }

    return zipOutputStream;
  }

  private OutputStreamAppender<ILoggingEvent> createOutputStreamAppender() throws IOException {
    LoggerContext loggerContext = getLoggerContext();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();

    encoder.setContext(loggerContext);
    encoder.setPattern(PATTERN);
    encoder.start();

    OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();

    appender.setName("Support Appender");
    appender.setContext(loggerContext);
    appender.setEncoder(encoder);
    appender.setOutputStream(getLoggingOutputStream());

    appender.start();

    return appender;
  }

  private Blob getLoggingBlob() {
    if (loggingBlob == null) {
      loggingBlob = blobStore.create(createBlobId("trace-only"));
    }

    return loggingBlob;
  }

  private String createBlobId(String type) {
    return String.format("%s_%s_%s", type, Instant.now(), SecurityUtils.getSubject().getPrincipal());
  }

  private OutputStream getLoggingOutputStream() throws IOException {
    if (loggingOutputStream == null) {
      loggingOutputStream = getLoggingBlob().getOutputStream();
    }

    return loggingOutputStream;
  }
}
