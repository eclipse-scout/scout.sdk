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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class ServerSessionNodePage extends AbstractScoutTypePage {

  public ServerSessionNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setName(type.getElementName());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerSession));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SERVER_SESSION_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    IScoutBundle sharedBundle = getScoutBundle().getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
    if (sharedBundle != null) {
      IScoutBundle clientBundle = sharedBundle.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      if (clientBundle != null) {
        // find client session
        for (IType clientSession : ScoutTypeUtility.getClientSessionTypes(clientBundle)) {
          new SharedContextPropertyTablePage(this, clientSession, getType());
        }
      }
      else {
        ScoutSdkUi.logInfo("could not find a client bundle name-correspondig to '" + getScoutBundle().getSymbolicName() + "'.");
      }
    }
  }
}
