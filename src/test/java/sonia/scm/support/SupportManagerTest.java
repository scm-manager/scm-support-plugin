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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.schedule.Scheduler;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.support.collector.Collector;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportManagerTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private BlobStoreFactory blobStoreFactory;
  @Mock
  private BlobStore blobStore;
  @Mock
  private Scheduler scheduler;

  private SupportManager supportManager;

  private String currentId;

  @BeforeEach
  void setUp() {
    when(blobStoreFactory.withName("support").build()).thenReturn(blobStore);
    supportManager = new SupportManager(blobStoreFactory, emptySet(), scheduler) {
      @Override
      Optional<String> getCurrentId() {
        return Optional.ofNullable(currentId);
      }
    };
  }

  @Nested
  class WithData {
    @BeforeEach
    void mockBlobs() {
      Blob blob1 = mock(Blob.class);
      when(blob1.getId()).thenReturn("simple_1986-02-08T09:30:00.0Z_trillian");
      when(blob1.getSize()).thenReturn(42L);
      Blob blob2 = mock(Blob.class);
      when(blob2.getId()).thenReturn("trace-running_1986-10-19T06:00:00.0Z_dent");
      when(blob2.getSize()).thenReturn(987654321L);
      when(blobStore.getAll()).thenReturn(List.of(blob1, blob2));
    }

    @Test
    void shouldReturnAllPackages() {
      Collection<SupportPackage> all = supportManager.getAll();

      assertThat(all).extracting("createdBy").containsExactly("trillian", "dent");
      assertThat(all).extracting("size").containsExactly(42L, 987654321L);
      assertThat(all).extracting("size").containsExactly(42L, 987654321L);
      assertThat(all).extracting("type").containsExactly("simple", "trace-running");
      assertThat(all).extracting("creationDate")
        .containsExactly(Instant.parse("1986-02-08T09:30:00.0Z"), Instant.parse("1986-10-19T06:00:00.0Z"));
    }

    @Test
    void shouldMarkRunningPackage() {
      currentId = "trace-running_1986-10-19T06:00:00.0Z_dent";

      Collection<SupportPackage> all = supportManager.getAll();

      assertThat(all.stream()
        .filter(p -> p.getBlob().getId().equals("trace-running_1986-10-19T06:00:00.0Z_dent"))
        .findFirst()).get().extracting("running").isEqualTo(true);
    }
  }

  @Test
  void shouldDeletePackage() {
    supportManager.delete("trace-running_1986-10-19T06:00:00.0Z_dent");

    verify(blobStore).remove("trace-running_1986-10-19T06:00:00.0Z_dent");
  }

  @Test
  void shouldNotDeletePackageForRunningTrace() {
    currentId = "trace-running_1986-10-19T06:00:00.0Z_dent";

    assertThrows(
      ScmConstraintViolationException.class,
      () -> supportManager.delete("trace-running_1986-10-19T06:00:00.0Z_dent")
    );

    verify(blobStore, never()).remove(anyString());
  }
}
