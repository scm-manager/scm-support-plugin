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

package sonia.scm.support;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

@Getter
@SuppressWarnings("squid:S2160") // There is no need and no semantics for equals here
class LoggingStateDto extends HalRepresentation {
  LoggingStateDto(boolean logRunning, boolean processingLog, Links links) {
    super(links);
    this.logRunning = logRunning;
    this.processingLog = processingLog;
  }

  private boolean logRunning;
  private boolean processingLog;
}
