//@flow
import React from "react";
import {translate} from "react-i18next";
import {apiClient, Button, DownloadButton, Page} from "@scm-manager/ui-components";

type Props = {
  informationLink?: string,
  traceLink?: string,
  // context props
  t: string => string
};

type State = {
  startTraceLink?: string,
  stopTraceLink?: string,
  startTraceSuccess: boolean,
  startTraceFailed: boolean
}

class SupportPage extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      startTraceLink: undefined,
      stopTraceLink: undefined
    }
  }

  componentDidMount(): void {
    if (!!this.props.traceLink) {
      this.fetchTraceStatus();
    }
  }

  fetchTraceStatus = () => {
    apiClient.get(this.props.traceLink)
      .then(result => result.json())
      .then(traceLinks => {
        console.log(traceLinks);
        this.setState({
          startTraceLink: traceLinks._links.startTrace,
          stopTraceLink: traceLinks._links.stopTrace
        });
      });
  };


  render() {
    const {t, informationLink} = this.props;
    const {startTraceLink, stopTraceLink} = this.state;

    const message = this.createMessage();

    const informationPart = !!informationLink ? (
      <>
        <hr/>
        <p>
          {t("scm-support-plugin.collect.help")}
        </p>
        <br/>
        <DownloadButton displayName={t("scm-support-plugin.collect.button")} url={informationLink}/>
      </>) : null;

    const canStartTrace = !!startTraceLink;
    const canStopTrace = !!stopTraceLink;

    console.log("start:", canStartTrace, "stop:", canStopTrace);

    const tracePart = !!(startTraceLink || stopTraceLink) ? (
      <>
        <hr/>
        <p>
          {t("scm-support-plugin.trace.help")}
        </p>
        <p>
          <em>{t("scm-support-plugin.trace.warning")}</em>
        </p>
        <Button color="warning" label={t("scm-support-plugin.trace.startButton")} disabled={!canStartTrace} action={this.startTrace}/>
        <DownloadButton displayName={t("scm-support-plugin.trace.stopButton")} url={!stopTraceLink? "": stopTraceLink.href}
                        disabled={!canStopTrace} onClick={this.stopTrace}/>
        <br/>
      </>) : null;

    return (
      <Page
        title={t("scm-support-plugin.title")}
        subtitle={t("scm-support-plugin.subtitle")}
      >
        {message}
        {informationPart}
        {tracePart}
      </Page>
    );
  }

  createMessage = () => {
    const {startTraceSuccess, startTraceFailed} = this.state;
    if (startTraceSuccess) {
      return (<i>Success</i>); // TODO
    } else if (startTraceFailed) {
      return (<i>Failed</i>); // TODO
    }
    return null;
  };

  startTrace = (e: Event) => {
    const {startTraceLink} = this.state;
    console.log(startTraceLink);
    apiClient
      .post(startTraceLink.href, "")
      .then(result => {
        const startTraceSuccess = result.status === 204;
        const startTraceFailed = result.status !== 204;
        this.setState({
          startTraceFailed, startTraceSuccess
        }, this.fetchTraceStatus);
      })
      .catch(err => {
        this.setState({
          startTraceFailed: true,
          startTraceSuccess: false
        });
      });
  };

  stopTrace = () => {
    console.log("STOP");
    this.setState({
      startTraceFailed: false,
      startTraceSuccess: false,
      stopTraceLink: undefined
    });
    return true;
  };
}

export default translate("plugins")(SupportPage);
