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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.custom;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ui.action.rename.ServiceRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;

public class CustomServiceNodePage extends AbstractServiceNodePage {

  public CustomServiceNodePage(AbstractPage parent, IType implementationType, IType interfaceType) {
    super(parent, implementationType, interfaceType);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CUSTOM_SERVICE_NODE_PAGE;
  }

  @Override
  public Action createRenameAction() {
    return new ServiceRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), getInterfaceType(), ScoutIdeProperties.SUFFIX_CUSTOM_SERVICE);
  }

}
