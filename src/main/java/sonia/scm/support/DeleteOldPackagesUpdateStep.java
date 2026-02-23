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

import jakarta.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.version.Version;

import java.util.List;

@Extension
class DeleteOldPackagesUpdateStep implements UpdateStep {

  private final BlobStoreFactory blobStoreFactory;

  @Inject
  public DeleteOldPackagesUpdateStep(BlobStoreFactory blobStoreFactory) {
    this.blobStoreFactory = blobStoreFactory;
  }

  @Override
  @SuppressWarnings("java:S5852") // this is not exposed to user input, only to old store files
  public void doUpdate() {
    BlobStore store = blobStoreFactory.withName("support").build();
    List<Blob> existingPackages = store.getAll();
    for (Blob existingPackage : existingPackages) {
      if (!existingPackage.getId().matches(".*_.*_.*")) {
        store.remove(existingPackage.getId());
      }
    }
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("3.2.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.support.blobs";
  }
}
