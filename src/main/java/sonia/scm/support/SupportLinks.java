/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
