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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.ServiceOperationNewAction;
import org.eclipse.scout.sdk.ui.action.delete.ServiceDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.ServiceRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.util.SdkProperties;

public class ClientServiceNodePage extends AbstractServiceNodePage {

  public ClientServiceNodePage(AbstractPage parent, IType type, IType interfaceType) {
    super(parent, type, interfaceType, SdkProperties.SUFFIX_SERVICE);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Service));
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(ServiceRenameAction.class, ShowJavaReferencesAction.class, ServiceOperationNewAction.class, ServiceDeleteAction.class);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CLIENT_SERVICE_NODE_PAGE;
  }
}