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

import jakarta.inject.Inject;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.plugin.Extension;

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
      HalAppender.LinkArrayBuilder builder = appender.linkArrayBuilder("support")
        .append("information", links.createInformationLink())
        .append("existing", links.createExistingLink());
      if (SupportPermissions.isPermittedToStartTrace()) {
        builder.append("logging", links.createLogStatusLink());
      }
      builder.build();
    }
  }
}
