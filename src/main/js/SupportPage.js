//@flow
import React from "react";
import { translate } from "react-i18next";
import { Page } from "@scm-manager/ui-components";
import type { SupportLinks } from "./types";

type Props = {
  link: string,
  // context props
  t: string => string
};

type State = {
  links?: SupportLinks
};

class SupportPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: true
    };
  }

  render() {
    const { t } = this.props;

    return (
      <Page
        title={t("scm-support-plugin.root-page.title")}
        subtitle={t("scm-support-plugin.root-page.subtitle")}
      >

      </Page>
    );
  }
}

export default translate("plugins")(SupportPage);
