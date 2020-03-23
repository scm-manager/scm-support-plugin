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
