/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.support;

//~--- non-JDK imports --------------------------------------------------------

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

import com.google.common.base.Charsets;
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
    "%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n";

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
      zipOutputStream = new ZipOutputStream(getZipBlob().getOutputStream(),
        Charsets.UTF_8);
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
