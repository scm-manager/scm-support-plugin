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



package sonia.scm.support.collector;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map.Entry;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class StackTraceCollector implements Collector
{

  /** Field description */
  private static final String NAME = "stacktraces/stacktrace.%s";

  /**
   * the logger for StackTraceCollector
   */
  private static final Logger logger =
    LoggerFactory.getLogger(StackTraceCollector.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   *
   * @throws IOException
   */
  @Override
  public void collect(CollectorContext context) throws IOException
  {
    for (int i = 0; i < 5; i++)
    {
      if (i > 0)
      {
        try
        {
          Thread.sleep(500l);
        }
        catch (InterruptedException ex)
        {
          logger.error("could not sleep", ex);
        }
      }

      PrintWriter writer = null;

      try
      {
        writer = context.createWriter(String.format(NAME, i));
        writeStackTraces(writer);
      }
      finally
      {
        Closeables.close(writer, true);
      }

    }

  }

  /**
   * Method description
   *
   *
   * @param writer
   */
  private void writeStackTraces(PrintWriter writer)
  {
    for (Entry<Thread, StackTraceElement[]> e :
      Thread.getAllStackTraces().entrySet())
    {
      Thread thread = e.getKey();

      if (thread.equals(Thread.currentThread()))
      {
        writer.append("Current ");
      }

      writer.append("Thread ").append(thread.getName());
      writer.append(" (").append(thread.getState().name()).println(")");

      StackTraceElement[] stackTrace = e.getValue();

      for (StackTraceElement ste : stackTrace)
      {
        writer.append("\t").println(ste);
      }

      writer.println();
    }
  }
}
