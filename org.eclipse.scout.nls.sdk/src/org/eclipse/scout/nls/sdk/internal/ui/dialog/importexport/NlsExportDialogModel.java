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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.importexport;

import org.eclipse.scout.nls.sdk.internal.model.PropertyBasedModel;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * The <b> {@link NlsExportDialogModel} </b> defines a Export operation. Use this description
 * to parametrize the {@link NlsExportToCsvOperation}.
 * 
 * @see NlsExportToCsvOperation
 */
public class NlsExportDialogModel extends PropertyBasedModel {

  public static final String PROP_EXPORT_FILE = "exportFile";
  public static final String PROP_EXPORT_SELECTED_ITEMS = "exportSelectedItems";
  private INlsProject m_project;
  private INlsEntry[] m_selectedEntries;

  public NlsExportDialogModel(INlsProject project) {
    m_project = project;
  }

  public void setExportFile(String file) {
    if (!file.toLowerCase().endsWith(".xls")) {
      file = file + ".xls";
    }
    setProperty(PROP_EXPORT_FILE, file);
  }

  public String getExportFile() {
    return (String) getProperty(PROP_EXPORT_FILE);
  }

  public void setExportSelectedItems(boolean value) {
    setPropertyBool(PROP_EXPORT_SELECTED_ITEMS, value);
  }

  public boolean isExportSelectedItems() {
    return getPropertyBool(PROP_EXPORT_SELECTED_ITEMS);
  }

  public INlsEntry[] getSelectedEntries() {
    return m_selectedEntries;
  }

  public void setSelectedEntries(INlsEntry[] selectedEntries) {
    m_selectedEntries = selectedEntries;
  }

  public INlsProject getNlsProject() {
    return m_project;
  }

}
