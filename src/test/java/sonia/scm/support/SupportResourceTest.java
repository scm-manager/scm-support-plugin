/**
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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.Blob;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportResourceTest {

  @Mock
  private SupportManager supportManager;

  @Mock
  private Blob blob;

  @Mock
  private SupportLinks links;

  @Mock
  private Subject subject;

  @InjectMocks
  private SupportResource resource;

  @BeforeEach
  void init() {
    ThreadContext.bind(subject);
  }

  @Nested
  class WithPermissions {
    @BeforeEach
    void init() throws IOException {
      when(supportManager.collectSupportData()).thenReturn(blob);
      when(blob.getId()).thenReturn("blobid");
    }

    @Test
    void shouldCreateSupportInformation() throws IOException {
      Response response = resource.createSupportFile();

      assertThat(response.getStatus()).isEqualTo(200);
    }
  }

  @Nested
  class WithoutPermissions {
    @BeforeEach
    void init() {
      doThrow(new AuthorizationException()).when(subject).checkPermission("support:information");
    }

    @Test
    void shouldFailSupportInformation() {

      Assertions.assertThrows(AuthorizationException.class, () -> resource.createSupportFile());
    }
  }
}
