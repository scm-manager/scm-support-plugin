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

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FilenameTimestampMapperTest {

  @Test
  void shouldReadOldTimestamp() {
    Instant instant = FilenameTimestampMapper.fromString("2026-06-17T06:34:48.584Z");

    assertThat(instant).isEqualTo(Instant.ofEpochMilli(1781678088584L));
  }

  @Test
  void shouldReadNewTimestamp() {
    Instant instant = FilenameTimestampMapper.fromString("2026-06-17T06-34-48.584Z");

    assertThat(instant).isEqualTo(Instant.ofEpochMilli(1781678088584L));
  }

  @Test
  void shouldWriteTimestampWithoutColon() {
    String representation = FilenameTimestampMapper.toString(Instant.ofEpochMilli(1781678088584L));

    assertThat(representation).isEqualTo("2026-06-17T06-34-48.584Z");
  }
}
