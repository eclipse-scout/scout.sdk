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

import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;

public class WebServicesTablePage extends AbstractPage {

  public WebServicesTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Webservices"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsIcons.WebservicesFolder));
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    new ProviderTablePage(this);
    new ConsumerTablePage(this);
    new HandlerTablePage(this);
    new SessionFactoryTablePage(this);
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.WEBSERVICES_TABLE_PAGE;
  }
}
