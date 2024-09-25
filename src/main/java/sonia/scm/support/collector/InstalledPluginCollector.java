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

import com.google.inject.Inject;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginManager;

import java.io.PrintWriter;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class InstalledPluginCollector extends WriterCollector
{

  /**
   * Constructs ...
   *
   *
   * @param pluginManager
   */
  @Inject
  public InstalledPluginCollector(PluginManager pluginManager)
  {
    super("plugins.txt");
    this.pluginManager = pluginManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param writer
   */
  @Override
  protected void collect(PrintWriter writer)
  {
    for (InstalledPlugin plugin : pluginManager.getInstalled())
    {
      writer.println(plugin.getId());
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final PluginManager pluginManager;
}
