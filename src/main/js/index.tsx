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

import React from "react";
import { Route } from "react-router-dom";
import { binder } from "@scm-manager/ui-extensions";
import { Link } from "@scm-manager/ui-types";
import SupportNavLink from "./SupportNavLink";
import SupportPage from "./SupportPage";
import { type PredicateProps, supportPredicate } from "./supportPredicate";

const SupportRoute = ({ links }: PredicateProps) => {
  return (
    <Route
      path="/admin/support"
      render={() => (
        <SupportPage
          informationLink={(links.supportInformation as Link)?.href}
          logLink={(links.logging as Link)?.href}
        />
      )}
    />
  );
};

binder.bind("admin.route", SupportRoute, supportPredicate);

binder.bind("admin.navigation", SupportNavLink, supportPredicate);
