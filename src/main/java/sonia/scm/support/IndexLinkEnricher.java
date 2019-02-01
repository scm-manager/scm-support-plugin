package sonia.scm.support;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkEnricher;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;

@Extension
@Enrich(Index.class)
public class IndexLinkEnricher implements LinkEnricher {

  private final SupportLinks links;

  @Inject
  public IndexLinkEnricher(SupportLinks links) {
    this.links = links;
  }

  @Override
  public void enrich(LinkEnricherContext context, LinkAppender appender) {
    if (SupportPermissions.isPermittedToReadInformation()) {
      appender.appendOne("supportInformation", links.createInformationLink());
    }
  }
}
