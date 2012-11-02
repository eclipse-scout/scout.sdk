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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class InstallClientSessionAction extends AbstractOperationAction {
  public InstallClientSessionAction() {
    super(Texts.get("Action_newTypeX", "Client Session"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientSessionAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return getOperations() != null;
  }

  public void init(ICachedTypeHierarchy clientSessionHierarchy, IScoutBundle scoutResource) {
    if (clientSessionHierarchy != null) {
      IType[] clientSessions = clientSessionHierarchy.getAllClasses(TypeFilters.getClassesInProject(scoutResource.getJavaProject()), null);
      if (clientSessions.length == 0) {
        setOperation(new InstallJavaFileOperation("templates/client/src/ClientSession.java", "ClientSession.java", scoutResource));
      }
    }
  }
}
