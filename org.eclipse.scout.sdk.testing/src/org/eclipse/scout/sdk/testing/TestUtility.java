/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.testing;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * <h3>{@link TestUtility}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.9.0 15.03.2013
 */
public final class TestUtility {
  private TestUtility() {
  }

  public static void setAutoBuildWorkspace(boolean autoBuild) throws CoreException {
    JdtUtility.setWorkspaceAutoBuilding(autoBuild);
  }

  public static void showEgitMessageBoxes(boolean show) {
    try {
      // preference store as defined in org.eclipse.ui.plugin.AbstractUIPlugin.getPreferenceStore()
      @SuppressWarnings("deprecation")
      IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.egit.ui");

      // following constants are coming from class org.eclipse.egit.ui.UIPreferences:
      store.setValue("show_detached_head_warning", show);
      store.setValue("show_git_prefix_warning", show);
      store.setValue("show_home_drive_warning", show);
      store.setValue("show_initial_config_dialog", show);
      store.setValue("show_rebase_confirm", show);
    }
    catch (Throwable e) {
      //NOP
    }
  }

  @SuppressWarnings("restriction")
  public static void setAutoUpdateFormData(boolean autoBuild) {
    org.eclipse.scout.sdk.internal.ScoutSdk.getDefault().setFormDataAutoUpdate(autoBuild);
  }
}
