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

import de.otto.edison.hal.HalRepresentation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.store.Blob;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportResourceTest {

  @Mock
  private SupportManager supportManager;

  @Mock
  private SupportLinks links;

  @Mock
  private Subject subject;

  @Mock
  private UserDisplayManager userDisplayManager;

  @Mock
  private UriInfo uriInfo;

  private SupportResource resource;

  @BeforeEach
  void init() {
    ExistingPackageMapperImpl existingPackageMapper = new ExistingPackageMapperImpl();
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> uriInfo.getBaseUri());
    existingPackageMapper.setPathInfoStore(pathInfoStore);
    existingPackageMapper.setUserDisplayManager(userDisplayManager);
    SupportPackageCollectionToDtoMapper collectionToDtoMapper = new SupportPackageCollectionToDtoMapper(
      existingPackageMapper,
      pathInfoStore
    );
    resource = new SupportResource(supportManager, links, existingPackageMapper, collectionToDtoMapper);
    ThreadContext.bind(subject);
    lenient().when(uriInfo.getBaseUri()).thenReturn(URI.create("https://scm.hitchhiker.org/scm"));
    lenient().when(uriInfo.getAbsolutePath()).thenReturn(URI.create("https://scm.hitchhiker.org/scm/support"));
  }

  @Nested
  class WithData {

    @Mock
    private Blob blob;

    @BeforeEach
    void init() {
      when(blob.getId()).thenReturn("simple_2026-02-17T10:14:02.929802980Z_trillian");
    }

    @Nested
    class ForSupportCreation {

      @BeforeEach
      void init() throws IOException {
        when(supportManager.collectSupportData()).thenReturn(blob);
      }

      @Test
      void shouldCreateSupportInformation() {
        Response response = resource.createSupportFile(uriInfo);

        assertThat(response.getStatus()).isEqualTo(200);
      }
    }

    @Test
    void shouldGetExistingPackages() {
      when(blob.getSize()).thenReturn(42L);
      SupportPackage existingSupportPackage = SupportPackage.from(blob);
      when(supportManager.getAll()).thenReturn(List.of(existingSupportPackage));

      HalRepresentation response = resource.getExistingPackages(uriInfo);

      assertThat(response.getEmbedded().getItemsBy("supportPackages"))
        .hasSize(1)
        .extracting("size")
        .containsExactly(42L);
    }

    @Test
    void shouldGetExistingPackage() {
      when(blob.getSize()).thenReturn(42L);
      SupportPackage existingSupportPackage = SupportPackage.from(blob);
      when(supportManager.get(blob.getId())).thenReturn(existingSupportPackage);
      when(userDisplayManager.get("trillian")).thenReturn(Optional.of(DisplayUser.from(new User(
        "trillian",
        "Trisha McMillan",
        null
      ))));

      SupportPackageDto response = resource.getExistingPackage(blob.getId());

      assertThat(response.getType()).isEqualTo("simple");
      assertThat(response.getSize()).isEqualTo(42L);
      assertThat(response.getCreatedBy()).isEqualTo("Trisha McMillan");
      assertThat(response.getCreationDate()).asString().isEqualTo("2026-02-17T10:14:02.929802980Z");
      assertThat(response.getLinks().getLinkBy("self"))
        .get()
        .extracting("href")
        .isEqualTo("https://scm.hitchhiker.org/v2/plugins/support/packages/simple_2026-02-17T10:14:02.929802980Z_trillian");
      assertThat(response.getLinks().getLinkBy("download"))
        .get()
        .extracting("href")
        .isEqualTo("https://scm.hitchhiker.org/v2/plugins/support/packages/simple_2026-02-17T10:14:02.929802980Z_trillian/download");
    }

    @Test
    void shouldGetExistingPackageWithoutKnownUser() {
      when(blob.getSize()).thenReturn(42L);
      SupportPackage existingSupportPackage = SupportPackage.from(blob);
      when(supportManager.get(blob.getId())).thenReturn(existingSupportPackage);
      when(userDisplayManager.get("trillian")).thenReturn(Optional.empty());

      SupportPackageDto response = resource.getExistingPackage(blob.getId());

      assertThat(response.getCreatedBy()).isEqualTo("trillian");
    }

    @Test
    void shouldDeleteExistingPackage() {
      resource.deleteExistingPackage(blob.getId());

      verify(supportManager).delete(blob.getId());
    }

    @Test
    void shouldThrowNotFoundForMissingSupportPackage() {
      when(supportManager.get(blob.getId())).thenThrow(NotFoundException.notFound(ContextEntry.ContextBuilder.entity("p", "x")));

      Assertions.assertThrows(
        NotFoundException.class,
        () -> resource.getExistingPackage(blob.getId())
      );
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
      Assertions.assertThrows(AuthorizationException.class, () -> resource.createSupportFile(uriInfo));
    }

    @Test
    void shouldFailToLoadExistingPackages() {
      Assertions.assertThrows(AuthorizationException.class, () -> resource.getExistingPackages(uriInfo));
    }

    @Test
    void shouldFailToLoadExistingPackage() {
      Assertions.assertThrows(AuthorizationException.class, () -> resource.getExistingPackage("some"));
    }

    @Test
    void shouldFailToDownloadExistingPackage() {
      Assertions.assertThrows(AuthorizationException.class, () -> resource.downloadExistingPackage(uriInfo, "some"));
    }

    @Test
    void shouldFailToDeleteExistingPackage() {
      Assertions.assertThrows(AuthorizationException.class, () -> resource.deleteExistingPackage("some"));
    }
  }
}
