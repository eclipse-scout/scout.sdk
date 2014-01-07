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
package org.eclipse.scout.sdk.ui.extensions.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class ScoutSdkPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  private BooleanFieldEditor m_updateFormDataAutomaticallyField;
  private BooleanFieldEditor m_targetPackageConfigEnabledField;
  private BooleanFieldEditor m_automaticallyCreateClassIdAnnotation;

  public ScoutSdkPreferencePage() {
    super(GRID);
    setPreferenceStore(ScoutSdkUi.getDefault().getPreferenceStore());
    setDescription(Texts.get("ScoutSDKPreferences"));
  }

  /**
   * Creates the field editors. Field editors are abstractions of
   * the common GUI blocks needed to manipulate various types
   * of preferences. Each field editor knows how to save and
   * restore itself.
   */
  @Override
  public void createFieldEditors() {
    m_updateFormDataAutomaticallyField = new BooleanFieldEditor(IDtoAutoUpdateManager.PROP_AUTO_UPDATE, Texts.get("UpdateModelDataAutomatically"), getFieldEditorParent());
    addField(m_updateFormDataAutomaticallyField);

    m_targetPackageConfigEnabledField = new BooleanFieldEditor(DefaultTargetPackage.PROP_USE_LEGACY_TARGET_PACKAGE, Texts.get("UseLegacyTargetPackage"), getFieldEditorParent());
    addField(m_targetPackageConfigEnabledField);

    m_automaticallyCreateClassIdAnnotation = new BooleanFieldEditor(ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION, Texts.get("AutomaticallyCreateClassIdAnnotation"), getFieldEditorParent());
    addField(m_automaticallyCreateClassIdAnnotation);
  }

  @Override
  public void init(IWorkbench workbench) {
  }
}
