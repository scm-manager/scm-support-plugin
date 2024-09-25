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

import type { Links } from "@scm-manager/ui-types";
import { supportPredicate } from "./supportPredicate";

describe("test predicate", () => {
  const exec = (links: Links) => {
    return supportPredicate({
      links
    });
  };

  it("should return true", () => {
    const result = exec({
      supportInformation: {
        href: "http://..."
      }
    });
    expect(result).toBe(true);
  });

  it("should return false with other links", () => {
    const result = exec({
      otherLink: {
        href: "http://..."
      }
    });
    expect(result).toBe(false);
  });

  it("should return false without any links", () => {
    const result = exec({});
    expect(result).toBe(false);
  });
});
