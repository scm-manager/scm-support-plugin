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

import lombok.Data;
import sonia.scm.store.Blob;

import java.time.Instant;

@Data
class SupportPackage {
  private final String type;
  private final Instant creationDate;
  private final String createdBy;
  private final long size;
  private final Blob blob;

  private boolean running;

  SupportPackage(String type, Instant creationDate, String createdBy, long size, Blob blob) {
    this.type = type;
    this.creationDate = creationDate;
    this.createdBy = createdBy;
    this.size = size;
    this.blob = blob;
  }

  static SupportPackage from(Blob blob) {
    String id = blob.getId();
    String[] parts = id.split("_", 3);
    if (parts.length != 3) {
      return new SupportPackage("unknown", null, null, blob.getSize(), blob);
    }
    return new SupportPackage(parts[0], Instant.parse(parts[1]), parts[2], blob.getSize(), blob);
  }
}
