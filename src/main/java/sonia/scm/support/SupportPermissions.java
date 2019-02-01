package sonia.scm.support;

import com.github.sdorra.ssp.Constants;
import org.apache.shiro.SecurityUtils;

class SupportPermissions {

  private static final String TYPE = "support";

  private static final String ACTION_INFORMATION = "information";
  private static final String ACTION_TRACE = "trace";

  private SupportPermissions() {
  }

  static boolean isPermittedToReadInformation() {
    return isPermitted(ACTION_INFORMATION);
  }

  static boolean isPermittedToStartTrace() {
    return isPermitted(ACTION_TRACE);
  }

  private static boolean isPermitted(String action) {
    String permission = create(action);
    return SecurityUtils.getSubject().isPermitted(permission);
  }

  static void checkReadInformation() {
    check(ACTION_INFORMATION);
  }

  static void checkStartTrace() {
    check(ACTION_TRACE);
  }

  private static void check(String action) {
    String permission = create(action);
    SecurityUtils.getSubject().checkPermission(permission);
  }

  private static String create(String action) {
    return TYPE.concat(Constants.SEPARATOR).concat(action);
  }
}
