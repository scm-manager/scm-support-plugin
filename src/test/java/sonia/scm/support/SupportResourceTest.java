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
