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

import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexHalEnricherTest {

  @Mock
  private HalAppender appender;
  @Mock
  private Subject subject;

  private IndexHalEnricher enricher;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    SupportLinks links = new SupportLinks(Providers.of(pathInfoStore));
    enricher = new IndexHalEnricher(links);
    ThreadContext.bind(subject);
  }

  @Test
  void shouldNotAppendInformationLinkWhenNotPermitted() {
    when(subject.isPermitted(anyString())).thenReturn(false);

    enricher.enrich(null, appender);

    verify(appender, never()).linkArrayBuilder(any());
    verify(appender, never()).appendLink(any(), any());
  }

  @Nested
  class WithPermissions {

    @Mock(answer = Answers.RETURNS_SELF)
    private HalAppender.LinkArrayBuilder linkArrayBuilder;

    @BeforeEach
    void setUp() {
      when(appender.linkArrayBuilder("support")).thenReturn(linkArrayBuilder);
    }

    @Test
    void shouldAppendInformationLinkWhenPermitted() {
      when(subject.isPermitted("support:information")).thenReturn(true);

      enricher.enrich(null, appender);

      verify(linkArrayBuilder).append("information", "/v2/plugins/support");
    }

    @Test
    void shouldAppendTraceLinkWhenPermitted() {
      when(subject.isPermitted("support:information")).thenReturn(true);
      when(subject.isPermitted("support:logging")).thenReturn(true);

      enricher.enrich(null, appender);

      verify(linkArrayBuilder).append("information", "/v2/plugins/support");
      verify(linkArrayBuilder).append("logging", "/v2/plugins/support/logging");
    }
  }
}
