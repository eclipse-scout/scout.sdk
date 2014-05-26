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
package org.eclipse.scout.sdk.ui.action;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 *
 */
public class InstallClientSessionAction extends AbstractOperationAction {
  public InstallClientSessionAction() {
    super(Texts.get("Action_newTypeX", "Client Session"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientSessionAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return getOperationCount() > 0;
  }

  public void init(ICachedTypeHierarchy clientSessionHierarchy, IScoutBundle scoutResource) {
    if (clientSessionHierarchy != null && !scoutResource.isBinary()) {
      Set<IType> clientSessions = clientSessionHierarchy.getAllClasses(ScoutTypeFilters.getTypesInScoutBundles(scoutResource), null);
      if (clientSessions.size() == 0) {
        IScoutBundle shared = scoutResource.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
        if (shared != null) {
          String pck = scoutResource.getPackageName("ui.desktop");
          IType desktopType = TypeUtility.getType(pck + ".Desktop");
          if (TypeUtility.exists(desktopType)) {
            HashMap<String, String> props = new HashMap<String, String>(2);
            props.put(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, shared.getSymbolicName());
            props.put(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, scoutResource.getSymbolicName());
            setOperation(new InstallJavaFileOperation("templates/client/src/ClientSession.java", "ClientSession.java", scoutResource, props));
          }
        }
      }
    }
  }
}
