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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Card, CardListBox, Icon } from "@scm-manager/ui-core";
import { HalRepresentation, Link } from "@scm-manager/ui-types";
import { Dialog, Menu } from "@scm-manager/ui-overlays";
import { DateFromNow, SmallLoadingSpinner } from "@scm-manager/ui-components";
import { useDeletePackage } from "./SupportPage";

export type ExistingPackage = HalRepresentation & {
  type: "trace-only" | "trace" | "simple" | "unknown";
  creationDate: string;
  createdBy: string;
  size: number;
  running: boolean;
};

const getExtendedRunningOrAbortedType = (existingPackage: ExistingPackage) => {
  if (existingPackage.running) {
    return "trace-running";
  } else {
    return existingPackage.type;
  }
};

const ExistingPackageItem: FC<{ existingPackage: ExistingPackage }> = ({ existingPackage }) => {
  const [t] = useTranslation("plugins");
  const { remove, isLoading } = useDeletePackage();
  const link = existingPackage._links.download ? (
    <a href={(existingPackage._links.download as Link).href}>
      {t("scm-support-plugin.existingPackages.types." + getExtendedRunningOrAbortedType(existingPackage))}
    </a>
  ) : (
    t("scm-support-plugin.existingPackages.types." + getExtendedRunningOrAbortedType(existingPackage))
  );
  const action = existingPackage.running ? undefined : (
    <Menu>
      <Menu.DialogButton
        title={t("scm-support-plugin.existingPackages.delete.title")}
        description={t("scm-support-plugin.existingPackages.delete.message")}
        footer={[
          <Dialog.CloseButton key="yes" onClick={() => remove(existingPackage)} isLoading={isLoading}>
            {t("scm-support-plugin.existingPackages.delete.confirmAlert.submit")}
          </Dialog.CloseButton>,
          <Dialog.CloseButton key="no" variant="primary" autoFocus>
            {t("scm-support-plugin.existingPackages.delete.confirmAlert.cancel")}
          </Dialog.CloseButton>,
        ]}
      >
        <Icon>trash</Icon>
        {t("scm-support-plugin.existingPackages.delete.title")}
      </Menu.DialogButton>
    </Menu>
  );
  if (existingPackage.type === "unknown") {
    return (
      <CardListBox.Card action={action}>
        <Card.Row className="is-flex">
          <Card.Title>{link}</Card.Title>
        </Card.Row>
      </CardListBox.Card>
    );
  } else {
    return (
      <CardListBox.Card action={action} avatar={existingPackage.running ? <SmallLoadingSpinner /> : undefined}>
        <Card.Row className="is-flex">
          <Card.Title>{link}</Card.Title>
        </Card.Row>
        <Card.SecondaryRow>
          <Card.Details>
            <Card.Details.Detail>{existingPackage.createdBy}</Card.Details.Detail>
            <Card.Details.Detail>
              <DateFromNow date={new Date(existingPackage.creationDate)} />
            </Card.Details.Detail>
            <Card.Details.Detail>{existingPackage.size.toLocaleString()} byte </Card.Details.Detail>
          </Card.Details>
        </Card.SecondaryRow>
      </CardListBox.Card>
    );
  }
};

export const ExistingPackageList: FC<{ existingPackages: Array<ExistingPackage> }> = ({ existingPackages }) => {
  const [t] = useTranslation("plugins");

  if (existingPackages.length === 0) {
    return <p>{t("scm-support-plugin.existingPackages.noPackages")}</p>;
  }
  return (
    <>
      <p>{t("scm-support-plugin.existingPackages.cleanUpMessage")}</p>
      <CardListBox>
        {existingPackages
          .sort((a, b) => (!a.creationDate ? 1 : !b.creationDate ? -1 : a.creationDate > b.creationDate ? -1 : 1))
          .map((p) => (
            <ExistingPackageItem key={p.creationDate} existingPackage={p} />
          ))}
      </CardListBox>
    </>
  );
};
