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
package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.ui.extensions.bundle.INewScoutBundleHandler;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 *
 */
public class SharedScoutBundleExtension implements INewScoutBundleHandler {

  public static final String ID = "org.eclipse.scout.sdk.ui.SharedBundle";

  @Override
  public void init(IScoutProjectWizard wizard, ScoutBundleUiExtension extension) {
    IScoutBundle selected = wizard.getScoutProject();
    boolean available = selected == null || selected.getType().equals(IScoutBundle.TYPE_SHARED);
    wizard.getProjectWizardPage().setBundleNodeAvailable(available, available, ID);
  }

  @Override
  public IStatus getStatus(IScoutProjectWizard wizard) {
    if (!wizard.getProjectWizardPage().hasSelectedBundle(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SERVER)) {
      return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "Be aware of creating a shared bundle without a client or server bundle.");
    }
    return Status.OK_STATUS;
  }

  @Override
  public void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected) {
  }

  @SuppressWarnings("unchecked")
  @Override
  public void putProperties(IScoutProjectWizard wizard, PropertyMap properties) {
    IScoutBundle selected = wizard.getScoutProject();
    if (selected != null) {
      IScoutBundle shared = selected.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), true);
      if (shared != null && shared.getJavaProject() != null) {
        properties.setProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, shared.getProject().getName());
        properties.getProperty(IScoutProjectNewOperation.PROP_CREATED_BUNDLES, List.class).add(shared.getJavaProject());
      }
    }
  }
}
