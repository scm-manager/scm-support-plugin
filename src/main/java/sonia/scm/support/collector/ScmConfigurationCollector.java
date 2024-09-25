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

import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import jakarta.xml.bind.JAXB;

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
