// @flow
import React from "react";
import { translate } from "react-i18next";
import { NavLink } from "@scm-manager/ui-components";

type Props = {
  //context objects
  t: string => string
};

class SupportNavLink extends React.Component<Props> {
  render() {
    const { t } = this.props;

    return (
      <NavLink
        to="/admin/support"
        icon="fas fa-life-ring"
        label={t("scm-support-plugin.navLink")}
      />
    );
  }
}

export default translate("plugins")(SupportNavLink);
