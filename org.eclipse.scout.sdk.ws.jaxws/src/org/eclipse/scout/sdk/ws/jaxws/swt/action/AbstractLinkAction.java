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
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;

public abstract class AbstractLinkAction extends AbstractScoutHandler implements IPresenterAction {

  private String m_leadingText;
  private String m_linkText;

  public AbstractLinkAction(String menuText, ImageDescriptor icon) {
    this(menuText, icon, null);
  }

  public AbstractLinkAction(String menuText, ImageDescriptor icon, String keyStroke) {
    super(menuText, icon, keyStroke, false, Category.WS);
  }

  @Override
  public String getLeadingText() {
    return m_leadingText;
  }

  public void setLeadingText(String leadingText) {
    m_leadingText = leadingText;
  }

  @Override
  public String getLinkText() {
    return m_linkText;
  }

  public void setLinkText(String linkText) {
    m_linkText = linkText;
  }
}
