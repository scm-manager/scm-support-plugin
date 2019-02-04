//@flow
import React from "react";
import {translate} from "react-i18next";
import {apiClient, Button, DownloadButton, Loading, Page} from "@scm-manager/ui-components";

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
  startTraceFailed: boolean,
  processingLog: boolean
}

class SupportPage extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      startTraceLink: undefined,
      stopTraceLink: undefined,
      startTraceSuccess: false,
      startTraceFailed: false,
      processingLog: false
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
        this.setState({
          startTraceLink: traceLinks._links.startTrace,
          stopTraceLink: traceLinks._links.stopTrace,
          processingLog: traceLinks.processingLog
        }, () => {
          if (this.state.processingLog) {
            console.log("trigger next check");
            this.updateTraceStatusAfterWait();
          }
        });
      });
  };

  updateTraceStatusAfterWait = async () => {
    setTimeout(function () {
      this.fetchTraceStatus();
    }.bind(this), 1000)
  };

  render() {
    const {t, informationLink} = this.props;
    const {startTraceLink, stopTraceLink, processingLog} = this.state;

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

    const tracePart =
      processingLog ?
        (<Loading message={t("scm-support-plugin.log.loading")}/>) :
        (<>
            <hr/>
            <p>
              {t("scm-support-plugin.log.help")}
            </p>
            <p>
              <em>{t("scm-support-plugin.log.warning")}</em>
            </p>
            <br/>
            <Button color="warning" label={t("scm-support-plugin.log.startButton")} disabled={!canStartTrace}
                    action={this.startTrace}/>
            <DownloadButton displayName={t("scm-support-plugin.log.stopButton")}
                            url={!stopTraceLink ? "" : stopTraceLink.href}
                            disabled={!canStopTrace} onClick={this.stopTrace}/>
            <br/>
          </>);

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
      return (this.createNotification("scm-support-plugin.log.startSuccess"));
    } else if (startTraceFailed) {
      return (this.createNotification("scm-support-plugin.log.startFailed"));
    }
    return null;
  };

  createNotification = (messageKey: string) => {
    if (this.state.startTraceFailed || this.state.startTraceSuccess) {
      return (
        <div className="notification is-info">
          <button
            className="delete"
            onClick={() =>
              this.setState({startTraceFailed: false, startTraceSuccess: false})
            }
          />
          {this.props.t(messageKey)}
        </div>
      );
    }
  };

  startTrace = (e: Event) => {
    const {startTraceLink} = this.state;
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
        }, this.fetchTraceStatus);
      });
  };

  stopTrace = () => {
    this.setState({
      startTraceFailed: false,
      startTraceSuccess: false,
      stopTraceLink: undefined,
      processingLog: true
    }, () => {
      this.updateTraceStatusAfterWait();
    });
    return true;
  };
}

export default translate("plugins")(SupportPage);
