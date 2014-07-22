/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.rap.ui.internal.wizard.export;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.export.ServerExportEntryHandler;
import org.eclipse.scout.sdk.ui.internal.wizard.export.AbstractExportProductWizardPage;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportServerWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link ExportRapWizardPage}</h3>
 *
 * @author Matthias Villiger
 */
public class ExportRapWizardPage extends AbstractExportProductWizardPage {

  private static final String SETTINGS_PRODUCT_FILE = "productFileRapSetting";
  private static final String SETTINGS_WAR_FILE_NAME = "warFileNameRapSetting";

  public ExportRapWizardPage(IScoutBundle scoutProject) {
    super(scoutProject, ExportRapWizardPage.class.getName(), Texts.get("ExportRapWebArchive"), IScoutSdkRapConstants.ScoutUiRapBundleId,
        SETTINGS_PRODUCT_FILE, SETTINGS_WAR_FILE_NAME);
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    if (!StringUtility.hasText(m_warFileName.getModifiableText())) {
      String alias = getWizard().getProjectAlias();
      if (StringUtility.hasText(alias)) {
        m_warFileName.setText(alias);
      }
    }
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    if (getWizard().getExportWizardPage().isNodesSelected(ServerExportEntryHandler.ID)) {
      ExportServerWizardPage server = (ExportServerWizardPage) getWizard().getPage(ExportServerWizardPage.class.getName());
      if (server != null && CompareUtility.equals(server.getWarName(), getWarName())) {
        multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("WarFileNameUsed")));
      }
    }
  }
}
