/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceManager;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ScoutSdkPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  private BooleanFieldEditor m_updateFormDataAutomaticallyField;
  private BooleanFieldEditor m_createClassIdAnnotationAutomaticallyField;
  private ComboFieldEditor m_logLevelField;

  public ScoutSdkPreferencePage() {
    super(GRID);
    setPreferenceStore(S2ESdkUiActivator.getDefault().getPreferenceStore());
    setDescription("Preferences for the Scout SDK");
  }

  @Override
  public void createFieldEditors() {
    m_updateFormDataAutomaticallyField = new BooleanFieldEditor(IDerivedResourceManager.PROP_AUTO_UPDATE, "Automatically update generated classes", getFieldEditorParent());
    addField(m_updateFormDataAutomaticallyField);

    m_createClassIdAnnotationAutomaticallyField = new BooleanFieldEditor(ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION, "Automatically create the @ClassId annotation for new classes.", getFieldEditorParent());
    addField(m_createClassIdAnnotationAutomaticallyField);

    String[][] comboValues = new String[][]{{"Error", "SEVERE"}, {"Warning", "WARNING"}, {"Info", "INFO"}, {"Debug", "FINE"}};
    m_logLevelField = new ComboFieldEditor(SdkLog.LOG_LEVEL_PROPERTY_NAME, "Log Level", comboValues, getFieldEditorParent());
    addField(m_logLevelField);
  }

  @Override
  public void init(IWorkbench workbench) {
    // nop
  }
}
