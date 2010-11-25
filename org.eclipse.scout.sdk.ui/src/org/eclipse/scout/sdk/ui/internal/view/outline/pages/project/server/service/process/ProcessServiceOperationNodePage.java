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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.process;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.ServiceOperationNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;

/**
 * <h3> {@link ProcessServiceOperationNodePage}</h3> ...
 */
public class ProcessServiceOperationNodePage extends ServiceOperationNodePage {

  public ProcessServiceOperationNodePage(AbstractPage parent, IMethod method, IMethod implementationMethod) {
    super(parent, method, implementationMethod);
  }

  // @Override
  // public void fillContextMenu(IMenuManager manager){
  // super.fillContextMenu(manager);
  // manager.add(new Separator());
  // FormFromServiceOpNewWizard wizard=new FormFromServiceOpNewWizard(getImplementationOpMethod(), getInterfaceOpMethod());
  // wizard.setClientBundles(getScoutResource().getScoutProject().getClientBundles());
  // manager.add(new WizardAction("Auto-generate Form", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_ADD), wizard));
  //
  // }
}
