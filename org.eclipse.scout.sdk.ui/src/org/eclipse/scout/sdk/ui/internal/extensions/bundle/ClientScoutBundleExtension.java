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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.CreateClientBundleOperation;
import org.eclipse.scout.sdk.operation.project.FillClientPluginOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;

/**
 *
 */
public class ClientScoutBundleExtension implements IScoutBundleProvider {

  public static final String BUNDLE_ID = "org.eclipse.scout.sdk.ui.ClientBundle";

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

  @Override
  public IJavaProject createBundle(IScoutProjectWizard wizard, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    TemplateVariableSet variables = TemplateVariableSet.createNew(wizard.getProjectWizardPage().getProjectName(), wizard.getProjectWizardPage().getProjectNamePostfix(), wizard.getProjectWizardPage().getProjectAlias());
    try {
      CreateClientBundleOperation op = new CreateClientBundleOperation(variables);
      op.run(monitor, workingCopyManager);
      new FillClientPluginOperation(op.getCreatedProject(), variables).run(monitor, workingCopyManager);
      return op.getJavaProject();
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("could not create client bundle", e);
      return null;
    }
  }
}
