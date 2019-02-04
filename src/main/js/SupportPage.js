//@flow
import React from "react";
import {translate} from "react-i18next";
import {apiClient, Button, DownloadButton, Loading, Page} from "@scm-manager/ui-components";

type Props = {
  informationLink?: string,
  logLink?: string,
  // context props
  t: string => string
};

type State = {
  startLogLink?: string,
  stopLogLink?: string,
  startLogSuccess: boolean,
  startLogFailed: boolean,
  processingLog: boolean
}

class SupportPage extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      startLogLink: undefined,
      stopLogLink: undefined,
      startLogSuccess: false,
      startLogFailed: false,
      processingLog: false
    }
  }

  componentDidMount(): void {
    if (!!this.props.logLink) {
      this.fetchLogStatus();
    }
  }

  fetchLogStatus = () => {
    apiClient.get(this.props.logLink)
      .then(result => result.json())
      .then(logLinks => {
        this.setState({
          startLogLink: logLinks._links.startLog,
          stopLogLink: logLinks._links.stopLog,
          processingLog: logLinks.processingLog
        }, () => {
          if (this.state.processingLog) {
            this.updateLogStatusAfterWait();
          }
        });
      });
  };

  updateLogStatusAfterWait = async () => {
    setTimeout(function () {
      this.fetchLogStatus();
    }.bind(this), 1000)
  };

  render() {
    const {t, informationLink} = this.props;
    const {startLogLink, stopLogLink, processingLog} = this.state;

    const message = this.createMessage();

    const infoItemCount = t("scm-support-plugin.collect.helpItemCount");

    const infoItems = Array(parseInt(infoItemCount));
    for(let i = 0; i < infoItemCount; i++) {
      const textKey = "scm-support-plugin.collect.helpItem" + (i + 1);
      const text = t(textKey);
      if (text === textKey) {
        break;
      }
      infoItems.push((<li>{text}</li>));
    }

    const informationPart = !!informationLink ? (
      <div className="content">
        <hr/>
        <p>
          {t("scm-support-plugin.collect.help")}
        </p>
          <ul>
          {infoItems}
          </ul>
        <br/>
        <DownloadButton displayName={t("scm-support-plugin.collect.button")} url={informationLink}/>
      </div>) : null;

    const canStartLog = !!startLogLink;
    const canStopLog = !!stopLogLink;

    const logPart =
      processingLog ?
        (<Loading message={t("scm-support-plugin.log.loading")}/>) :
        (<div className="content">
          <hr/>
          <p>
            {t("scm-support-plugin.log.help")}
          </p>
          <p>
            <em>{t("scm-support-plugin.log.warning")}</em>
          </p>
          <br/>
          <a color="warning" className="button is-large is-link is-warning" disabled={!canStartLog}
             onClick={this.startLog}><span>{t("scm-support-plugin.log.startButton")}</span></a>
          <DownloadButton displayName={t("scm-support-plugin.log.stopButton")}
                          url={!stopLogLink ? "" : stopLogLink.href}
                          disabled={!canStopLog} onClick={this.stopLog}/>
          <br/>
        </div>);

    return (
      <Page
        title={t("scm-support-plugin.title")}
        subtitle={t("scm-support-plugin.subtitle")}
      >
        {message}
        {informationPart}
        {logPart}
      </Page>
    );
  }

  createMessage = () => {
    const {startLogSuccess, startLogFailed} = this.state;
    if (startLogSuccess) {
      return (this.createNotification("scm-support-plugin.log.startSuccess"));
    } else if (startLogFailed) {
      return (this.createNotification("scm-support-plugin.log.startFailed"));
    }
    return null;
  };

  createNotification = (messageKey: string) => {
    if (this.state.startLogFailed || this.state.startLogSuccess) {
      return (
        <div className={this.state.startLogFailed? "notification is-warning": "notification is-info"}>
          <button
            className="delete"
            onClick={() =>
              this.setState({startLogFailed: false, startLogSuccess: false})
            }
          />
          {this.props.t(messageKey)}
        </div>
      );
    }
  };

  startLog = (e: Event) => {
    const {startLogLink} = this.state;
    apiClient
      .post(startLogLink.href, "")
      .then(result => {
        const startLogSuccess = result.status === 204;
        const startLogFailed = result.status !== 204;
        this.setState({
          startLogFailed, startLogSuccess
        }, this.fetchLogStatus);
      })
      .catch(err => {
        this.setState({
          startLogFailed: true,
          startLogSuccess: false
        }, this.fetchLogStatus);
      });
  };

  stopLog = () => {
    this.setState({
      startLogFailed: false,
      startLogSuccess: false,
      stopLogLink: undefined,
      processingLog: true
    }, () => {
      this.updateLogStatusAfterWait();
    });
    return true;
  };
}

export default translate("plugins")(SupportPage);
