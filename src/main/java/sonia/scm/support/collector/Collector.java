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

import java.io.IOException;
import sonia.scm.plugin.ExtensionPoint;

/**
 *
 * @author Sebastian Sdorra
 */
@ExtensionPoint(multi = true)
public interface Collector
{

  /**
   * Method description
   *
   *
   * @param context
   * @throws java.io.IOException
   */
  public void collect(CollectorContext context) throws IOException;
}
