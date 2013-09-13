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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.testing.internal.SdkTestingApi;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link TestUtility}</h3>
 * 
 * @author Andreas Hoegger
 * @author Matthias Villiger
 * @since 3.9.0 15.03.2013
 */
public final class TestUtility {
  private TestUtility() {
  }

  /**
   * Specifies if the workspace should build automatically or not.
   * 
   * @param autoBuild
   *          true for automatic build, false to disable.
   * @throws CoreException
   */
  public static void setAutoBuildWorkspace(boolean autoBuild)
      throws CoreException {
    JdtUtility.setWorkspaceAutoBuilding(autoBuild);
  }

  /**
   * If there are egit plugins available in the environment where tests are
   * executed, the test might be blocked by the pop boxes created by egit.
   * This can be disabled (and enabled) by this method.
   * 
   * @param show
   *          true if the message boxes should be shown, false otherwise.
   */
  public static void showEgitMessageBoxes(boolean show) {
    try {
      // preference store as defined in
      // org.eclipse.ui.plugin.AbstractUIPlugin.getPreferenceStore()
      IPreferenceStore store = new ScopedPreferenceStore(
          InstanceScope.INSTANCE, "org.eclipse.egit.ui");

      // following constants are coming from class
      // org.eclipse.egit.ui.UIPreferences:
      store.setValue("show_detached_head_warning", show);
      store.setValue("show_git_prefix_warning", show);
      store.setValue("show_home_drive_warning", show);
      store.setValue("show_initial_config_dialog", show);
      store.setValue("show_rebase_confirm", show);
    }
    catch (Throwable e) {
      // NOP
    }
  }

  /**
   * Defines a new PDE target with given name and all directories of all
   * bundles in the running osgi runtime. This target is then resolved and set
   * as the current target platform.
   * 
   * @param targetName
   *          the name of the target
   * @param monitor
   *          The progress monitor
   * @throws CoreException
   */
  @SuppressWarnings("restriction")
  public static void loadRunningOsgiAsTarget(String targetName,
      IProgressMonitor monitor) throws CoreException {
    Bundle[] bundles = SdkTestingApi.getContext().getBundles();
    Set<File> dirs = new HashSet<File>();
    for (Bundle bundle : bundles) {
      if (bundle instanceof org.eclipse.osgi.framework.internal.core.AbstractBundle) {
        org.eclipse.osgi.framework.internal.core.AbstractBundle aBundle = (org.eclipse.osgi.framework.internal.core.AbstractBundle) bundle;
        if (aBundle.getBundleData() instanceof org.eclipse.osgi.baseadaptor.BaseData) {
          org.eclipse.osgi.baseadaptor.BaseData bundleData = (org.eclipse.osgi.baseadaptor.BaseData) aBundle
              .getBundleData();

          File file = bundleData.getBundleFile().getBaseFile();
          if (file != null && file.exists()) {
            dirs.add(file.getParentFile());
          }
        }
      }
    }
    TargetPlatformUtility.resolveTargetPlatform(dirs, targetName, true,
        monitor);
  }

  /**
   * Specifies if Scout should update the form datas automatically or not.
   * 
   * @param autoBuild
   *          true for auto update, false to disable.
   */
  public static void setAutoUpdateFormData(boolean autoBuild) {
    org.eclipse.scout.sdk.internal.ScoutSdk.getDefault()
        .setFormDataAutoUpdate(autoBuild);
  }
}
