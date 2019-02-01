package sonia.scm.support;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class SupportLinksTest {

  private SupportLinks links;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    links = new SupportLinks(Providers.of(pathInfoStore));
  }

  @Test
  void shouldCreateSupportLink() {
    assertThat(links.createInformationLink()).isEqualTo("/v2/plugins/support");
  }
}
