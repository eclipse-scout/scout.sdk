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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>IconNodePage</h3> a node forks the Icon editor to open on selection.
 */
public class IconNodePage extends AbstractPage {

  private final IType m_iconsType;

  public IconNodePage(IPage parentPage, IType iconsType) {
    m_iconsType = iconsType;
    setParent(parentPage);
    setName(Texts.get("Icons"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Icons));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.ICON_NODE_PAGE;
  }

  /**
   * shared bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public boolean isChildrenLoaded() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    // void
  }

  public IType getIconsType() {
    return m_iconsType;
  }
}
