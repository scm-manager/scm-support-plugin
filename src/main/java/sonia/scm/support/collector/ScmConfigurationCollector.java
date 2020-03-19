/**
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

import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class ScmConfigurationCollector implements Collector
{

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  @Inject
  public ScmConfigurationCollector(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

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
    ScmConfiguration cfg = new ScmConfiguration();

    cfg.load(configuration);

    String proxyServer = cfg.getProxyServer();

    if (!Strings.isNullOrEmpty(proxyServer))
    {
      cfg.setProxyServer("(is set)");
    }

    String proxyUser = cfg.getProxyUser();

    if (!Strings.isNullOrEmpty(proxyUser))
    {
      cfg.setProxyUser("(is set)");
    }

    String proxyPassword = cfg.getProxyPassword();

    if (!Strings.isNullOrEmpty(proxyPassword))
    {
      cfg.setProxyPassword("(is set)");
    }

    OutputStream output = null;

    try
    {
      output = context.createOutputStream("config.xml");
      JAXB.marshal(cfg, output);
    }
    finally
    {
      Closeables.close(output, true);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ScmConfiguration configuration;
}
