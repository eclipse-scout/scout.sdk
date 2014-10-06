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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.ui.extensions.bundle.INewScoutBundleHandler;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class ClientScoutBundleExtension implements INewScoutBundleHandler {

  public static final String ID = "org.eclipse.scout.sdk.ui.ClientBundle";

  @Override
  public void init(IScoutProjectWizard wizard, ScoutBundleUiExtension extension) {
    IScoutBundle selected = wizard.getScoutProject();
    boolean clientAvailable = selected == null || selected.hasType(IScoutBundle.TYPE_CLIENT) || selected.hasType(IScoutBundle.TYPE_SHARED);
    wizard.getProjectWizardPage().setBundleNodeAvailable(clientAvailable, clientAvailable, ID);
  }

  @Override
  public IStatus getStatus(IScoutProjectWizard wizard) {
    IScoutBundle selected = wizard.getScoutProject();
    if (selected == null) {
      if (!wizard.getProjectWizardPage().hasSelectedBundle(IScoutBundle.TYPE_SHARED)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientWithoutAShared"));
      }
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
      IScoutBundle client = selected.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), true);
      if (client != null && client.getJavaProject() != null) {
        properties.setProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, client.getProject().getName());
        properties.getProperty(IScoutProjectNewOperation.PROP_CREATED_BUNDLES, List.class).add(client.getJavaProject());
      }
    }
  }
}
