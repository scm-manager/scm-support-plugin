package sonia.scm.support;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;

@Extension
@Enrich(Index.class)
public class IndexHalEnricher implements HalEnricher {

  private final SupportLinks links;

  @Inject
  public IndexHalEnricher(SupportLinks links) {
    this.links = links;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    if (SupportPermissions.isPermittedToReadInformation()) {
      appender.appendLink("supportInformation", links.createInformationLink());
    }
    if (SupportPermissions.isPermittedToStartTrace()) {
      appender.appendLink("logging", links.createLogStatusLink());
    }
  }
}
