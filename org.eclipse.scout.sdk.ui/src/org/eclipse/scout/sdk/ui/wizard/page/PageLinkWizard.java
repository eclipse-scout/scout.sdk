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
package org.eclipse.scout.sdk.ui.wizard.page;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.ui.IWorkbench;

public class PageLinkWizard extends AbstractWorkspaceWizard {

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("LinkPage"));

    IScoutBundle clientBundle = UiUtility.getScoutBundleFromSelection(selection, ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT));
    IType holderType = UiUtility.getTypeFromSelection(selection);

    PageLinkWizardPage linkPageWizardPage = new PageLinkWizardPage(clientBundle);
    linkPageWizardPage.setHolderType(holderType);
    addPage(linkPageWizardPage);
  }
}
