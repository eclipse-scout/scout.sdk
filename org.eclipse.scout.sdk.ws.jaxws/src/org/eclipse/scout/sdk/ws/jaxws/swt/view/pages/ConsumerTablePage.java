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
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;

public class ConsumerTablePage extends AbstractPage {

  public ConsumerTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Consumer"));
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.CONSUMER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    new WebServiceConsumerTablePage(this);
    new AuthenticationHandlerTablePage(this, WebserviceEnum.Consumer);
  }
}
