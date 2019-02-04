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
