// @flow
import React from "react";
import { PrimaryNavigationLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

const SupportNavigation = ({ t }) => {
  return (
    <PrimaryNavigationLink
      to="/support/information"
      label={t("scm-support-plugin.primary-navigation")}
      key="support"
    />
  );
};

export default translate("plugins")(SupportNavigation);
