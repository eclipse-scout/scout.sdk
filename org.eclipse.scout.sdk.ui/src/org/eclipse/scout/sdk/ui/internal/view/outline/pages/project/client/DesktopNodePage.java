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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class DesktopNodePage extends AbstractScoutTypePage {

  public DesktopNodePage(IPage parentPage, IType desktopType) {
    setParent(parentPage);
    setType(desktopType);
    setName(Texts.get("DesktopNodePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Desktop));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.DESKTOP_NODE_PAGE;
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    new DesktopMenuTablePage(this, getType());
    new DesktopOutlineTablePage(this, getType());
    new ToolButtonTablePage(this, getType());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ShowJavaReferencesAction.class};
  }
}
