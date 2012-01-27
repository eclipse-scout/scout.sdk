/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * <p>
 * This class represents a preference page that is contributed to the Preferences dialog.
 * </p>
 * <p>
 * This page is used to modify preferences only. Values are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 * </p>
 */
public class JaxWsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  private BooleanFieldEditor refreshLocalJobField;
  private IntegerFieldEditor pollingIntervalField;

  public JaxWsPreferencePage() {
    super(GRID);
    setPreferenceStore(JaxWsSdk.getDefault().getPreferenceStore());
    setDescription(Texts.get("JaxWsPreferences"));
  }

  @Override
  public void init(IWorkbench workbench) {
  }

  /**
   * Creates the field editors. Field editors are abstractions of the common GUI
   * blocks needed to manipulate various types of preferences. Each field editor
   * knows how to save and restore itself.
   */
  @Override
  public void createFieldEditors() {
    addField(new BooleanFieldEditor(IPreferenceConstants.PREF_STUB_GENERATION_DEBUG_MODE, Texts.get("BuildStubInDebugMode"), getFieldEditorParent()));
    addField(new BooleanFieldEditor(IPreferenceConstants.PREF_STUB_GENERATION_KEEP_LAUNCH_CONFIG, Texts.get("KeepLaunchConfiguration"), getFieldEditorParent()));
  }
}
