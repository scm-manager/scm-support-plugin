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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Button, Level, Loading, Notification, Subtitle, Title, useDocumentTitle } from "@scm-manager/ui-core";
import { apiClient, DownloadButton } from "@scm-manager/ui-components";
import { Link } from "@scm-manager/ui-types";
import { QueryClient, useMutation, useQuery, useQueryClient } from "react-query";
import { ExistingPackage, ExistingPackageList } from "./ExistingPackageList";

const SUPPORT_PACKAGES_QUERY_KEY = "supportPackages";

type Props = {
  informationLink: string;
  logLink?: string;
  existingLink: string;
};

async function invalidateExistingPackages(queryClient: QueryClient) {
  await queryClient.invalidateQueries(SUPPORT_PACKAGES_QUERY_KEY);
}

export const useDeletePackage = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, ExistingPackage>(
    (existingPackage) => {
      const deleteUrl = (existingPackage._links.self as Link).href;
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, branch) => invalidateExistingPackages(queryClient),
    },
  );
  return {
    remove: (existingPackage: ExistingPackage) => {
      mutate(existingPackage);
    },
    isLoading,
    error,
    isDeleted: !!data,
  };
};

const SupportPage: FC<Props> = ({ informationLink, logLink, existingLink }) => {
  const [t] = useTranslation("plugins");

  const [startLogLink, setStartLogLink] = useState<Link | undefined>(undefined);
  const [stopLogLink, setStopLogLink] = useState<Link | undefined>(undefined);
  const [startLogSuccess, setStartLogSuccess] = useState<boolean>(false);
  const [startLogFailed, setStartLogFailed] = useState<boolean>(false);
  const [stopLogSuccess, setStopLogSuccess] = useState<boolean>(false);
  const [processingLog, setProcessingLog] = useState<boolean>(false);
  const queryClient = useQueryClient();

  const {
    data: existingPackages,
    isLoading: isLoadingExistingPackages,
    error: errorExistingPackages,
  } = useQuery<ExistingPackage, Error>({
    queryKey: SUPPORT_PACKAGES_QUERY_KEY,
    queryFn: () => apiClient.get(existingLink).then((response) => response.json()),
  });

  useDocumentTitle(t("scm-support-plugin.title"));

  useEffect(() => {
    if (!!logLink) {
      fetchLogStatus();
    }
  }, [logLink]);

  const fetchLogStatus = () => {
    apiClient
      .get(logLink!)
      .then((result) => result.json())
      .then((logLinks) => {
        setStartLogLink(logLinks._links.startLog);
        setStopLogLink(logLinks._links.stopLog);
        setProcessingLog(logLinks.processingLog);
        invalidateExistingPackages(queryClient);

        if (logLinks.processingLog) {
          updateLogStatusAfterWait();
        }
      });
  };

  const updateLogStatusAfterWait = async () => {
    setTimeout(
      function () {
        fetchLogStatus();
        invalidateExistingPackages(queryClient);
      }.bind(this),
      1000,
    );
  };

  const createMessage = () => {
    const onClose = () => {
      setStartLogSuccess(false);
      setStartLogFailed(false);
      setStopLogSuccess(false);
    };

    if (startLogSuccess) {
      return (
        <Notification type="success" onClose={onClose}>
          {t("scm-support-plugin.log.startSuccess")}
        </Notification>
      );
    } else if (startLogFailed) {
      return (
        <Notification type="warning" onClose={onClose}>
          {t("scm-support-plugin.log.startFailed")}
        </Notification>
      );
    } else if (stopLogSuccess) {
      return (
        <Notification type="success" onClose={onClose}>
          {t("scm-support-plugin.log.stopSuccess")}
        </Notification>
      );
    }
    return null;
  };

  const startLog = () => {
    apiClient
      .post((startLogLink as Link).href, "")
      .then((result) => {
        const startLogSuccess = result.status === 204;
        const startLogFailed = result.status !== 204;

        setStartLogFailed(startLogFailed);
        setStartLogSuccess(startLogSuccess);
        setStopLogSuccess(false);
        fetchLogStatus();
      })
      .catch((err) => {
        setStartLogFailed(true);
        setStartLogSuccess(false);
        setStopLogSuccess(false);
        fetchLogStatus();
      });
  };

  const stopLog = () => {
    setStartLogFailed(false);
    setStartLogSuccess(false);
    setStopLogSuccess(true);
    setStopLogLink(undefined);
    setProcessingLog(true);
    updateLogStatusAfterWait().then((r) => console.log(r));
    return true;
  };

  const message = createMessage();

  const informationPart = !!informationLink ? (
    <div className="content">
      <hr />
      <p>{t("scm-support-plugin.collect.help")}</p>
      <div className="level">
        <div className="level-left">
          <ul>
            <li>{t("scm-support-plugin.collect.helpItem.sysInfo")}</li>
            <li>{t("scm-support-plugin.collect.helpItem.support")}</li>
            <li>{t("scm-support-plugin.collect.helpItem.plugins")}</li>
            <li>{t("scm-support-plugin.collect.helpItem.stackTrace")}</li>
          </ul>
        </div>
        <Level
          right={
            <DownloadButton
              displayName={t("scm-support-plugin.collect.button")}
              url={informationLink}
              onClick={() => {
                setTimeout(
                  function () {
                    invalidateExistingPackages(queryClient);
                  }.bind(this),
                  5000,
                );
              }}
            />
          }
        />
      </div>
    </div>
  ) : null;

  const startButton = startLogLink ? (
    <Button onClick={() => startLog()} variant="signal">
      {t("scm-support-plugin.log.startButton")}
    </Button>
  ) : (
    <Button variant="signal" disabled={true}>
      {t("scm-support-plugin.log.startButton")}
    </Button>
  );

  const downloadButton = stopLogLink ? (
    <DownloadButton displayName={t("scm-support-plugin.log.stopButton")} url={stopLogLink.href} onClick={stopLog} />
  ) : (
    <DownloadButton displayName={t("scm-support-plugin.log.stopButton")} disabled={true} />
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
      <Level
        right={
          <div className="buttons">
            {startButton}
            {downloadButton}
          </div>
        }
      />
    </div>
  );

  const existingPackagesTable = isLoadingExistingPackages ? (
    <Loading />
  ) : errorExistingPackages ? (
    <Notification type="warning">
      {t("scm-support-plugin.existingPackages.loadingError")}: {errorExistingPackages.message}
    </Notification>
  ) : (
    <ExistingPackageList existingPackages={existingPackages!._embedded!.supportPackages as Array<ExistingPackage>} />
  );

  return (
    <>
      <Title>{t("scm-support-plugin.title")}</Title>
      <Subtitle>{t("scm-support-plugin.subtitle")}</Subtitle>
      {message}
      {informationPart}
      {logPart}
      <hr />
      <Subtitle>{t("scm-support-plugin.existingPackages.title")}</Subtitle>
      {existingPackagesTable}
    </>
  );
};
export default SupportPage;
