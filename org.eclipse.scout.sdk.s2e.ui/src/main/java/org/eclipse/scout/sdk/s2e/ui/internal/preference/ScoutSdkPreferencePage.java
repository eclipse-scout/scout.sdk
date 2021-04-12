/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.s2e.derived.IDerivedResourceManager;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ScoutSdkPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  public ScoutSdkPreferencePage() {
    super(GRID);
    setPreferenceStore(S2ESdkUiActivator.getDefault().getPreferenceStore());
    setDescription("Preferences for the Scout SDK");
  }

  @Override
  public void createFieldEditors() {
    FieldEditor updateFormDataAutomaticallyField = new BooleanFieldEditor(IDerivedResourceManager.PROP_AUTO_UPDATE, "Automatically update generated classes", getFieldEditorParent());
    addField(updateFormDataAutomaticallyField);

    FieldEditor createClassIdField =
        new BooleanFieldEditor(S2ESdkUiActivator.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION, "Automatically create the @ClassId annotation for new classes.", getFieldEditorParent());
    addField(createClassIdField);

    var comboValues = new String[][]{{"Error", "SEVERE"}, {"Warning", "WARNING"}, {"Info", "INFO"}, {"Debug", "FINE"}};
    FieldEditor logLevelField = new ComboFieldEditor(SdkLog.LOG_LEVEL_PROPERTY_NAME, "Log Level", comboValues, getFieldEditorParent());
    addField(logLevelField);
  }

  @Override
  public void init(IWorkbench workbench) {
    // nop
  }
}
