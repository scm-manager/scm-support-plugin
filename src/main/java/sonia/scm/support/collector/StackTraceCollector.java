/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
