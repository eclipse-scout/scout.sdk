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
package org.eclipse.scout.sdk.ui.wizard.services;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.ui.IWorkbench;

public class ServiceOperationNewWizard extends AbstractWorkspaceWizard {

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    IType serviceImpl = UiUtility.getTypeFromSelection(selection);
    IType serviceInterface = null;
    Object firstElement = selection.getFirstElement();
    if (firstElement instanceof AbstractServiceNodePage) {
      serviceInterface = ((AbstractServiceNodePage) firstElement).getInterfaceType();
    }

    setWindowTitle(Texts.get("NewServiceOperationNoPopup"));
    ServiceOperationNewWizardPage page1 = new ServiceOperationNewWizardPage(serviceInterface, serviceImpl);
    addPage(page1);
  }
}
