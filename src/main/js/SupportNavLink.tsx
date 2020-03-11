import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { SecondaryNavigationItem } from "@scm-manager/ui-components";

type Props = WithTranslation;

class SupportNavLink extends React.Component<Props> {
  render() {
    const { t } = this.props;

    return (
      <SecondaryNavigationItem
        to={`/admin/support`}
        icon="fas fa-life-ring"
        label={t("scm-support-plugin.navLink")}
        title={t("scm-support-plugin.navLink")}
      />
    );
  }
}

export default withTranslation("plugins")(SupportNavLink);
