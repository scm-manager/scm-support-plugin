//@flow
import React from "react";
import { translate } from "react-i18next";
import {
  apiClient,
  Title,
  Subtitle,
  ButtonGroup,
  DownloadButton,
  Loading,
  Notification,
  Button
} from "@scm-manager/ui-components";

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
  stopLogSuccess: boolean,
  processingLog: boolean
};

class SupportPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      startLogLink: undefined,
      stopLogLink: undefined,
      startLogSuccess: false,
      startLogFailed: false,
      stopLogSuccess: false,
      processingLog: false
    };
  }

  componentDidMount(): void {
    if (!!this.props.logLink) {
      this.fetchLogStatus();
    }
  }

  fetchLogStatus = () => {
    apiClient
      .get(this.props.logLink)
      .then(result => result.json())
      .then(logLinks => {
        this.setState(
          {
            startLogLink: logLinks._links.startLog,
            stopLogLink: logLinks._links.stopLog,
            processingLog: logLinks.processingLog
          },
          () => {
            if (this.state.processingLog) {
              this.updateLogStatusAfterWait();
            }
          }
        );
      });
  };

  updateLogStatusAfterWait = async () => {
    setTimeout(
      function() {
        this.fetchLogStatus();
      }.bind(this),
      1000
    );
  };

  render() {
    const { t, informationLink } = this.props;
    const { startLogLink, stopLogLink, processingLog } = this.state;

    const message = this.createMessage();

    const informationPart = !!informationLink ? (
      <div className="content">
        <hr />
        <p>{t("scm-support-plugin.collect.help")}</p>
        <ul>
          <li>{t("scm-support-plugin.collect.helpItem.sysInfo")}</li>
          <li>{t("scm-support-plugin.collect.helpItem.support")}</li>
          <li>{t("scm-support-plugin.collect.helpItem.plugins")}</li>
          <li>{t("scm-support-plugin.collect.helpItem.stackTrace")}</li>
        </ul>
        <div className="level">
          <div className="level-left" />
          <div className="level-right">
            <DownloadButton
              displayName={t("scm-support-plugin.collect.button")}
              url={informationLink}
            />
          </div>
        </div>
      </div>
    ) : null;

    const startButton = startLogLink ? (
      <Button action={this.startLog} color="warning">
        {t("scm-support-plugin.log.startButton")}
      </Button>
    ) : (
      <Button color="warning" disabled={true}>
        {t("scm-support-plugin.log.startButton")}
      </Button>
    );

    const downloadButton = stopLogLink ? (
      <DownloadButton
        displayName={t("scm-support-plugin.log.stopButton")}
        url={stopLogLink.href}
        onClick={this.stopLog}
      />
    ) : (
      <DownloadButton
        displayName={t("scm-support-plugin.log.stopButton")}
        disabled={true}
      />
    );

    const logPart = processingLog ? (
      <Loading message={t("scm-support-plugin.log.loading")} />
    ) : (
      <div className="content">
        <hr />
        <p>{t("scm-support-plugin.log.help")}</p>
        <p>
          <span className="icon has-text-warning">
            <i className="fas fa-exclamation-triangle" />
          </span>{" "}
          <em className="it-warning">{t("scm-support-plugin.log.warning")}</em>
        </p>
        <div className="level">
          <div className="level-left" />
          <div className="level-right">
            <ButtonGroup>
              {startButton}
              {downloadButton}
            </ButtonGroup>
          </div>
        </div>
      </div>
    );

    return (
      <>
        <Title title={t("scm-support-plugin.title")} />
        <Subtitle subtitle={t("scm-support-plugin.subtitle")} />
        {message}
        {informationPart}
        {logPart}
      </>
    );
  }

  createMessage = () => {
    const { t } = this.props;
    const { startLogSuccess, startLogFailed, stopLogSuccess } = this.state;

    const onClose = () => {
      this.setState({
        startLogSuccess: false,
        startLogFailed: false,
        stopLogSuccess: false
      });
    };

    if (startLogSuccess) {
      return (
        <Notification type={"success"} onClose={onClose}>
          {t("scm-support-plugin.log.startSuccess")}
        </Notification>
      );
    } else if (startLogFailed) {
      return (
        <Notification type={"warning"} onClose={onClose}>
          {t("scm-support-plugin.log.startFailed")}
        </Notification>
      );
    } else if (stopLogSuccess) {
      return (
        <Notification type={"success"} onClose={onClose}>
          {t("scm-support-plugin.log.stopSuccess")}
        </Notification>
      );
    }
    return null;
  };

  startLog = (e: Event) => {
    const { startLogLink } = this.state;
    // if (!startLogLink) return;
    apiClient
      .post(startLogLink.href, "")
      .then(result => {
        const startLogSuccess = result.status === 204;
        const startLogFailed = result.status !== 204;
        this.setState(
          {
            startLogFailed,
            startLogSuccess,
            stopLogSuccess: false
          },
          this.fetchLogStatus
        );
      })
      .catch(err => {
        this.setState(
          {
            startLogFailed: true,
            startLogSuccess: false,
            stopLogSuccess: false
          },
          this.fetchLogStatus
        );
      });
  };

  stopLog = () => {
    this.setState(
      {
        startLogFailed: false,
        startLogSuccess: false,
        stopLogSuccess: true,
        stopLogLink: undefined,
        processingLog: true
      },
      () => {
        this.updateLogStatusAfterWait();
      }
    );
    return true;
  };
}

export default translate("plugins")(SupportPage);
