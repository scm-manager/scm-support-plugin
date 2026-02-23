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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.api.v2.resources.BaseMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.user.UserDisplayManager;

@Mapper
abstract class ExistingPackageMapper extends BaseMapper<SupportPackage, SupportPackageDto> {

  @Inject
  private ScmPathInfoStore pathInfoStore;
  @Inject
  private UserDisplayManager userDisplayManager;

  public abstract SupportPackageDto map(SupportPackage supportPackage);

  @ObjectFactory
  SupportPackageDto create(SupportPackage supportPackage) {
    ResourceLinks resourceLinks = new ResourceLinks();
    return new SupportPackageDto(Links.linkingTo()
      .self(resourceLinks.self(supportPackage))
      .single(Link.link("download", resourceLinks.download(supportPackage)))
      .build());
  }

  @AfterMapping
  void updateUser(@MappingTarget SupportPackageDto dto) {
    userDisplayManager.get(dto.getCreatedBy()).ifPresent(user -> dto.setCreatedBy(user.getDisplayName()));
  }

  class ResourceLinks {
    private final LinkBuilder linkBuilder = new LinkBuilder(() -> pathInfoStore.get().getApiRestUri(), SupportResource.class);

    public String download(SupportPackage supportPackage) {
      return linkBuilder
        .method("downloadExistingPackage").parameters(supportPackage.getBlob().getId())
        .href();
    }

    public String self(SupportPackage supportPackage) {
      return linkBuilder
        .method("getExistingPackage").parameters(supportPackage.getBlob().getId())
        .href();
    }
  }

  @VisibleForTesting
  void setPathInfoStore(ScmPathInfoStore pathInfoStore) {
    this.pathInfoStore = pathInfoStore;
  }

  @VisibleForTesting
  void setUserDisplayManager(UserDisplayManager userDisplayManager) {
    this.userDisplayManager = userDisplayManager;
  }
}
