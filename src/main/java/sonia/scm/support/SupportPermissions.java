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

import com.github.sdorra.ssp.Constants;
import org.apache.shiro.SecurityUtils;

class SupportPermissions {

  private static final String TYPE = "support";

  private static final String ACTION_INFORMATION = "information";
  private static final String ACTION_LOG = "logging";

  private SupportPermissions() {
  }

  static boolean isPermittedToReadInformation() {
    return isPermitted(ACTION_INFORMATION);
  }

  static boolean isPermittedToStartTrace() {
    return isPermitted(ACTION_LOG);
  }

  private static boolean isPermitted(String action) {
    String permission = create(action);
    return SecurityUtils.getSubject().isPermitted(permission);
  }

  static void checkReadInformation() {
    check(ACTION_INFORMATION);
  }

  static void checkStartLog() {
    check(ACTION_LOG);
  }

  private static void check(String action) {
    String permission = create(action);
    SecurityUtils.getSubject().checkPermission(permission);
  }

  private static String create(String action) {
    return TYPE.concat(Constants.SEPARATOR).concat(action);
  }
}
