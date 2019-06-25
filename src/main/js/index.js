// @flow
import React from "react";
import { Route } from "react-router-dom";
import { binder } from "@scm-manager/ui-extensions";
import type { Links } from "@scm-manager/ui-types";
import SupportNavLink from "./SupportNavLink";
import SupportPage from "./SupportPage";

type PredicateProps = {
  links: Links
};

// @VisibleForTesting
export const supportPredicate = ({ links }: PredicateProps) => {
  return !!(links && (links.supportInformation || links.logging));
};

const SupportRoute = ({ links }) => {
  return (
    <Route
      path="/admin/support"
      render={() => (
        <SupportPage
          informationLink={links.supportInformation.href}
          logLink={links.logging.href}
        />
      )}
    />
  );
};

binder.bind("admin.route", SupportRoute, supportPredicate);

binder.bind("admin.navigation", SupportNavLink, supportPredicate);
