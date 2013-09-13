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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class SessionFactoryNodePage extends AbstractPage {

  private IType m_type;

  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required

  public SessionFactoryNodePage(IPage parent, IType type) {
    setParent(parent);
    setName(type.getElementName());
    m_type = type;
    if (type.isBinary()) {
      setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.SessionFactoryBinary));
    }
    else {
      setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.SessionFactory));
    }

    m_bundle = getScoutBundle();
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.SESSION_FACTORY_NODE_PAGE;
  }

  @Override
  public int getQuality() {
    int quality = IMarker.SEVERITY_INFO;
    if (getType().exists()) {
      quality = ScoutSeverityManager.getInstance().getSeverityOf(getType());
    }
    return quality;
  }

  @Override
  public boolean handleDoubleClickedDelegate() {
    if (getType() != null) {
      try {
        JavaUI.openInEditor(getType());
      }
      catch (Exception e) {
        JaxWsSdk.logWarning("could not open type in editor", e);
      }
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    if (!m_type.isBinary()) {
      return new Class[]{DeleteAction.class, TypeRenameAction.class, ShowJavaReferencesAction.class};
    }
    return new Class[]{ShowJavaReferencesAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof TypeRenameAction) {
      TypeRenameAction action = (TypeRenameAction) menu;
      action.setOldName(getType().getElementName());
      action.setType(getType());
    }
    else if (menu instanceof ShowJavaReferencesAction) {
      ((ShowJavaReferencesAction) menu).setElement(getType());
    }
    else if (menu instanceof DeleteAction) {
      ((DeleteAction) menu).addType(getType());
      ((DeleteAction) menu).setName(getType().getElementName());
    }
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  public IType getType() {
    return m_type;
  }
}
