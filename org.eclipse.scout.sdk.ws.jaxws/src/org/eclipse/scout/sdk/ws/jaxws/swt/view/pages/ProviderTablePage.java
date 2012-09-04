/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.pages;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.JaxWsAliasChangeWizardAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;

public class ProviderTablePage extends AbstractPage {

  public ProviderTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Provider"));
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.PROVIDER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof JaxWsAliasChangeWizardAction) {
      ((JaxWsAliasChangeWizardAction) menu).init(getScoutResource());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{JaxWsAliasChangeWizardAction.class};
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  protected void loadChildrenImpl() {
    new WebServiceProviderTablePage(this);
    new AuthenticationHandlerTablePage(this, WebserviceEnum.Provider);
    new CredentialValidationStrategyTablePage(this);
  }
}
