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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public final class CollectorContext
{

  /**
   * Constructs ...
   *
   *
   * @param zipOutputStream
   */
  public CollectorContext(ZipOutputStream zipOutputStream)
  {
    this.zipOutputStream = zipOutputStream;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  public OutputStream createOutputStream(String name) throws IOException
  {
    zipOutputStream.putNextEntry(new ZipEntry(name));

    return new OutputStream()
    {

      @Override
      public void write(int b) throws IOException
      {
        zipOutputStream.write(b);
      }

      @Override
      public void close() throws IOException
      {
        zipOutputStream.closeEntry();
      }

    };
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  public PrintWriter createWriter(String name) throws IOException
  {
    zipOutputStream.putNextEntry(new ZipEntry(name));

    return new PrintWriter(zipOutputStream)
    {

      @Override
      public void close()
      {
        this.flush();

        try
        {
          zipOutputStream.closeEntry();
        }
        catch (IOException ex)
        {

          // ignore
        }
      }
    };

  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ZipOutputStream zipOutputStream;
}
