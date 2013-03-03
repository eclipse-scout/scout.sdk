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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

public class ServerSessionNodePage extends AbstractScoutTypePage {

  final IType iClientSession = TypeUtility.getType(RuntimeClasses.IClientSession);

  public ServerSessionNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setName(Texts.get("ServerSession"));
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
        ICachedTypeHierarchy clientSessionHierarchy = TypeUtility.getPrimaryTypeHierarchy(iClientSession);
        ITypeFilter filter = ScoutTypeFilters.getTypesInScoutBundles(clientBundle);
        IType[] allClientSessions = clientSessionHierarchy.getAllSubtypes(iClientSession, filter, TypeComparators.getTypeNameComparator());
        if (allClientSessions.length > 1) {
          ScoutSdkUi.logError("a client bundle '" + clientBundle + "' can have in maximum 1 client session");
        }
        else if (allClientSessions.length == 1) {
          IType clientSession = allClientSessions[0];
          new SharedContextPropertyTablePage(this, clientSession, getType());
        }
        else {
          ScoutSdkUi.logInfo("could not find a client session in bundle '" + clientBundle.getSymbolicName() + "'.");
        }

      }
      else {
        ScoutSdkUi.logInfo("could not find a client bundle name-correspondig to '" + getScoutBundle().getSymbolicName() + "'.");
      }
    }
  }
}
