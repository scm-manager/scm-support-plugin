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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * This formatter replaces colons in the default string representation for instant with dashes so that the
 * timestamp can safely be used in the filename on Windows systems, too.
 */
final class FilenameTimestampMapper {

  private static final DateTimeFormatter SAFE_FORMATTER = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd'T'HH-mm-ss")
    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
    .appendLiteral('Z')
    .toFormatter()
    .withZone(ZoneOffset.UTC);

  private FilenameTimestampMapper() {
  }

  static String toString(Instant instant) {
    return SAFE_FORMATTER.format(instant);
  }

  static Instant fromString(String s) {
    if (s.contains(":")) {
      return Instant.parse(s);
    }
    return Instant.from(SAFE_FORMATTER.parse(s));
  }
}
