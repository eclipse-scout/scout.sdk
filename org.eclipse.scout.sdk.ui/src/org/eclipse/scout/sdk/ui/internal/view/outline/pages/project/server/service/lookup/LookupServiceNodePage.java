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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.lookup;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.ServiceOperationNewAction;
import org.eclipse.scout.sdk.ui.action.delete.ServiceDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.ServiceRenameAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;

/**
 * represents the implementation of a service
 */
public class LookupServiceNodePage extends AbstractServiceNodePage {

  public LookupServiceNodePage(AbstractPage parent, IType type, IType interfaceType) {
    super(parent, type, interfaceType, ScoutIdeProperties.SUFFIX_LOOKUP_SERVICE);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.LOOKUP_SERVICE_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    // XXX add LookupCallTablePage / LookupCallNodePage
    // XXX add ServiceOperationTablePage / ServiceOperationNodePage
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ServiceRenameAction.class, ShowJavaReferencesAction.class, FormDataSqlBindingValidateAction.class,
        ServiceOperationNewAction.class, ServiceDeleteAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof ServiceDeleteAction) {
      ServiceDeleteAction action = (ServiceDeleteAction) menu;
      action.setServiceImplementation(getType());
    }
  }
}
