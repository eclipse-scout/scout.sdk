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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ClientScoutBundleExtension implements IScoutBundleProvider {

  public final static String ID = "org.eclipse.scout.sdk.ui.ClientBundle";

  @Override
  public void init(IScoutProjectWizard wizard, IScoutProject project) {
    IScoutBundle client = project.getClientBundle();
    wizard.getProjectWizardPage().setBundleNodeAvailable(client == null, ID);
  }

  @Override
  public IStatus getStatus(IScoutProjectWizard wizard) {
    if (!wizard.getProjectWizardPage().hasSelectedBundle(BundleTypes.Shared_Bundle)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientWithoutAShared"));
    }
    if (!wizard.getProjectWizardPage().hasSelectedBundle(BundleTypes.GUI_Bundle)) {
      return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "Be aware of creating a client bundle without a GUI bundle.");
    }
    return Status.OK_STATUS;
  }

  @Override
  public void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected) {
  }
}
