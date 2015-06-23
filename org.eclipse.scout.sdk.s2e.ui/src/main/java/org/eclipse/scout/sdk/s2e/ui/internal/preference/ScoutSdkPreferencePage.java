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
package org.eclipse.scout.sdk.s2e.ui.internal.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateManager;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ScoutSdkPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  private BooleanFieldEditor m_updateFormDataAutomaticallyField;

  public ScoutSdkPreferencePage() {
    super(GRID);
    setPreferenceStore(S2ESdkUiActivator.getDefault().getPreferenceStore());
    setDescription("Preferences for the Scout SDK");
  }

  @Override
  public void createFieldEditors() {
    m_updateFormDataAutomaticallyField = new BooleanFieldEditor(IDtoAutoUpdateManager.PROP_AUTO_UPDATE, "Automatically update DTO classes", getFieldEditorParent());
    addField(m_updateFormDataAutomaticallyField);
  }

  @Override
  public void init(IWorkbench workbench) {
  }
}
