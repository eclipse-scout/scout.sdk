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

import java.util.Set;

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
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class CredentialValidationStrategyNodePage extends AbstractPage implements ITypePage {

  private IType m_type;

  public CredentialValidationStrategyNodePage(IPage parent, IType type) {
    setParent(parent);
    setName(type.getElementName());
    m_type = type;
    if (type.isBinary()) {
      setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.CredentialValidationStrategyBinary));
    }
    else {
      setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.CredentialValidationStrategy));
    }
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.CREDENTIAL_VALIDATION_STRATEGY_NODE_PAGE;
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

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    if (!m_type.isBinary()) {
      return newSet(DeleteAction.class, TypeRenameAction.class, ShowJavaReferencesAction.class);
    }
    return newSet(ShowJavaReferencesAction.class);
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public IType getType() {
    return m_type;
  }

  @Override
  public void setType(IType type) {
    m_type = type;
  }
}