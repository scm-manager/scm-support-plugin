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
