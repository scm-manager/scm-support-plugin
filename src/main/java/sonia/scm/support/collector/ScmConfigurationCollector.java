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

    configuration.load(cfg);

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
