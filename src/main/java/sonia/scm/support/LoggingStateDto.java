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
