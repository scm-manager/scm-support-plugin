//@flow
import React from "react";
import {translate} from "react-i18next";
import {DownloadButton, Page} from "@scm-manager/ui-components";

type Props = {
  informationLink: string,
  // context props
  t: string => string
};

class SupportPage extends React.Component<Props> {
  render() {
    const {t, informationLink} = this.props;

    const informationPart = (
      <>
        <p>
          {t("scm-support-plugin.collect.help")}
        </p>
        <br/>
        <DownloadButton displayName={t("scm-support-plugin.collect.button")} url={informationLink}/>
      </>);

    return (
      <Page
        title={t("scm-support-plugin.title")}
        subtitle={t("scm-support-plugin.subtitle")}
      >
        <hr/>
        {informationPart}
      </Page>
    );
  }
}

export default translate("plugins")(SupportPage);
