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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

public class ServerSessionNodePage extends AbstractScoutTypePage {

  final IType iClientSession = ScoutSdk.getType(RuntimeClasses.IClientSession);

  public ServerSessionNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setName("Server Session");
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SERVER_SESSION_NODE_PAGE;
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  protected void loadChildrenImpl() {
    IScoutBundle clientBundle = getScoutResource().getScoutProject().getClientBundle();

    if (clientBundle != null) {
      // find client session
      ICachedTypeHierarchy clientSessionHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iClientSession);
      ITypeFilter filter = TypeFilters.getClassesInProject(clientBundle.getJavaProject());
      IType[] allClientSessions = clientSessionHierarchy.getAllSubtypes(iClientSession, filter, TypeComparators.getTypeNameComparator());
      if (allClientSessions.length > 1) {
        ScoutSdkUi.logError("a client bundle '" + clientBundle + "' can have in maximum 1 client session");
      }
      else if (allClientSessions.length == 1) {
        IType clientSession = allClientSessions[0];
        new SharedContextPropertyTablePage(this, clientSession, getType());
      }
      else {
        ScoutSdkUi.logInfo("could not find a client session in bundle '" + clientBundle.getBundleName() + "'.");
      }

    }
    else {
      ScoutSdkUi.logInfo("could not find a client bundle name-correspondig to '" + getScoutResource().getBundleName() + "'.");
    }
  }

}
