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

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class SupportHandler implements Closeable
{

  /** Field description */
  private static final String PATTERN =
    "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%-10X{transaction_id}] %-5level %logger - %msg%n";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param blobStore
   */
  public SupportHandler(BlobStore blobStore)
  {
    this.blobStore = blobStore;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    if (outputStreamAppender != null)
    {
      outputStreamAppender.stop();
      Closeables.close(loggingOutputStream, true);
      loggingBlob.commit();

      if (zipOutputStream != null)
      {
        zipOutputStream.putNextEntry(new ZipEntry("scm-manager.log"));

        InputStream input = null;

        try
        {
          input = loggingBlob.getInputStream();
          ByteStreams.copy(input, zipOutputStream);
        }
        finally
        {
          Closeables.close(input, true);
          zipOutputStream.closeEntry();
        }
      }

      blobStore.remove(loggingBlob);
    }

    Closeables.close(zipOutputStream, true);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public LoggerContext getLoggerContext()
  {
    ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

    if (!(loggerFactory instanceof LoggerContext))
    {
      throw new IllegalStateException();
    }

    return (LoggerContext) loggerFactory;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public OutputStreamAppender<ILoggingEvent> getOutputStreamAppender()
    throws IOException
  {
    if (outputStreamAppender == null)
    {
      outputStreamAppender = createOutputStreamAppender();
    }

    return outputStreamAppender;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Blob getZipBlob()
  {
    if (zipBlob == null)
    {
      zipBlob = blobStore.create();
    }

    return zipBlob;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public ZipOutputStream getZipOutputStream() throws IOException
  {
    if (zipOutputStream == null)
    {
      zipOutputStream = new ZipOutputStream(getZipBlob().getOutputStream());
    }

    return zipOutputStream;
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
  private OutputStreamAppender<ILoggingEvent> createOutputStreamAppender()
    throws IOException
  {
    LoggerContext loggerContext = getLoggerContext();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();

    encoder.setContext(loggerContext);
    encoder.setPattern(PATTERN);
    encoder.start();

    OutputStreamAppender<ILoggingEvent> appender =
      new OutputStreamAppender<ILoggingEvent>();

    appender.setName("Support Appender");
    appender.setContext(loggerContext);
    appender.setEncoder(encoder);
    appender.setOutputStream(getLoggingOutputStream());

    appender.start();

    return appender;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private Blob getLoggingBlob()
  {
    if (loggingBlob == null)
    {
      loggingBlob = blobStore.create();
    }

    return loggingBlob;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private OutputStream getLoggingOutputStream() throws IOException
  {
    if (loggingOutputStream == null)
    {
      loggingOutputStream = getLoggingBlob().getOutputStream();
    }

    return loggingOutputStream;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final BlobStore blobStore;

  /** Field description */
  private Blob loggingBlob;

  /** Field description */
  private OutputStream loggingOutputStream;

  /** Field description */
  private OutputStreamAppender<ILoggingEvent> outputStreamAppender;

  /** Field description */
  private Blob zipBlob;

  /** Field description */
  private ZipOutputStream zipOutputStream;
}
