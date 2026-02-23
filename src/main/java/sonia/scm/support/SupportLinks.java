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

import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

class SupportLinks {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  SupportLinks(Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  String createInformationLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), SupportResource.class);
    return linkBuilder.method("createSupportFile").parameters().href();
  }

  String createLogStatusLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), SupportResource.class);
    return linkBuilder.method("loggingState").parameters().href();
  }

  String createStartLogLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), SupportResource.class);
    return linkBuilder.method("enableTraceLogging").parameters().href();
  }

  String createStopLogLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), SupportResource.class);
    return linkBuilder.method("disableTraceLogging").parameters().href();
  }

  String createExistingLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), SupportResource.class);
    return linkBuilder.method("getExistingPackages").parameters().href();
  }
}
