package sonia.scm.support;

import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.inject.Inject;
import javax.inject.Provider;

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
}
