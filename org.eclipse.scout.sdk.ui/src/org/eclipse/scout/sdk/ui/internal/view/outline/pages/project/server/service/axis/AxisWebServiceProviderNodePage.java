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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.axis;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.action.delete.AxisWebServiceUndeployAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class AxisWebServiceProviderNodePage extends AbstractScoutTypePage {

  public AxisWebServiceProviderNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.AXIS_WEB_SERVICE_PROVIDER_NODE_PAGE;
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
  }

  @Override
  public Action createDeleteAction() {
    return new AxisWebServiceUndeployAction(getOutlineView().getSite().getShell(), "Un-Publish " + getName(), getScoutResource(), getType());
  }

}
