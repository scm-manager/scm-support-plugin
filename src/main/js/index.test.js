// @flow

import { predicate } from "./index";

describe("test predicate", () => {
  const exec = links => {
    return predicate({ links });
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
