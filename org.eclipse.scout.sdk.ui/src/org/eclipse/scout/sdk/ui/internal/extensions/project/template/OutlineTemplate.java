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
package org.eclipse.scout.sdk.ui.internal.extensions.project.template;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.ui.extensions.project.template.IProjectTemplate;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ClientScoutBundleExtension;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 *
 */
public class OutlineTemplate implements IProjectTemplate {

  public static final String ID = "org.eclipse.scout.sdk.ui.outlineTemplate";

  @Override
  public String getText() {
    return Texts.get("OutlineTreeAndTableForm");
  }

  @Override
  public String getDescription() {
    return Texts.get("OutlineTemplateDesc");
  }

  @Override
  public void apply(IScoutProject project, IProgressMonitor monitor, IWorkingCopyManager manager) {
    try {
      OutlineTemplateOperation op = new OutlineTemplateOperation(project);
      op.validate();
      op.run(monitor, manager);
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("could not apply OutlineTemplate.", e);
    }
  }

  @Override
  public boolean isApplicable(IScoutProjectWizard wizard) {
    return wizard.getProjectWizardPage().isBundleNodesSelected(ClientScoutBundleExtension.BUNDLE_ID);
  }

}
