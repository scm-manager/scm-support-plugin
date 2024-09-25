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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class WriterCollector implements Collector
{

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  public WriterCollector(String name)
  {
    this.name = name;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param writer
   */
  protected abstract void collect(PrintWriter writer);

  /**
   * Method description
   *
   *
   * @param context
   *
   * @throws IOException
   */
  @Override
  public final void collect(CollectorContext context) throws IOException
  {
    PrintWriter writer = null;

    try
    {
      writer = context.createWriter(name);
      collect(writer);
    }
    finally
    {
      Closeables.close(writer, true);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String name;
}
