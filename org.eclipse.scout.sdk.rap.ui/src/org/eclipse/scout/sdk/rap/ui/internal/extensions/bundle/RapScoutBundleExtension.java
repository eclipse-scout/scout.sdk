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
package org.eclipse.scout.sdk.rap.ui.internal.extensions.bundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.rap.operations.project.UiRapBundleNewOperation;
import org.eclipse.scout.sdk.rap.ui.internal.wizard.project.RapTargetPlatformWizardPage;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class RapScoutBundleExtension implements IScoutBundleProvider {
  public static final String BUNDLE_ID = "org.eclipse.scout.sdk.ui.UiRapBundle";

  public RapScoutBundleExtension() {
  }

  @Override
  public IStatus getStatus(IScoutProjectWizard wizard) {
    if (!wizard.getProjectWizardPage().hasSelectedBundle(BundleTypes.Client_Bundle)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Be aware of creating a RAP bundle without a client bundle.");
    }
    return Status.OK_STATUS;
  }

  @Override
  public void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected) {
    AbstractScoutWizardPage page = wizard.getPage(RapTargetPlatformWizardPage.class.getName());
    if (page == null) {
      page = new RapTargetPlatformWizardPage();
      wizard.addPage(page);
    }
    page.setExcludePage(!selected);
  }

  @Override
  public IJavaProject createBundle(IScoutProjectWizard wizard, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    TemplateVariableSet variables = TemplateVariableSet.createNew(wizard.getProjectWizardPage().getProjectName(), wizard.getProjectWizardPage().getProjectNamePostfix(), wizard.getProjectWizardPage().getProjectAlias());
    try {
      (variables).setVariable("RAP_REPOSITORY_LOCATION", "http://download.eclipse.org/rt/rap/latest-stable/runtime");
      (variables).setVariable("BUNDLE_RAP_NAME", variables.getVariable(ITemplateVariableSet.VAR_PROJECT_NAME) + ".ui.rap" + variables.getVariable(ITemplateVariableSet.VAR_PROJECT_POSTFIX));
      UiRapBundleNewOperation rapOp = new UiRapBundleNewOperation(variables);
      rapOp.run(monitor, workingCopyManager);
//      new FillUiSwtPluginOperation(swtOp.getCreatedProject(), variables).run(monitor, workingCopyManager);
      return rapOp.getJavaProject();
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("could not create UI RAP bundle", e);
      return null;
    }

  }

}
