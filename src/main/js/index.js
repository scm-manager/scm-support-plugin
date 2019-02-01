// @flow
import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import { ProtectedRoute } from "@scm-manager/ui-components";
import SupportNavigation from "./SupportNavigation";
import SupportPage from "./SupportPage";
import type { Links } from "@scm-manager/ui-types";

type RouteProps = {
  authenticated: boolean,
  links: Links
};

const SupportRoute = ({ authenticated, links }: RouteProps) => {
  return (
    <>
      <ProtectedRoute
        path="/support"
        component={() => <SupportPage informationLink={links.supportInformation.href} />}
        authenticated={authenticated}
      />
    </>
  );
};

binder.bind("main.route", SupportRoute);

type PredicateProps = {
  links: Links
};

// @VisibleForTesting
export const predicate = ({ links }: PredicateProps) => {
  return !!(links && links.supportInformation);
};

binder.bind("primary-navigation", SupportNavigation, predicate);
