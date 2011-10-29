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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project;

import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>UiSwtNodePage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class UiSwingNodePage extends AbstractPage {

  private final IScoutBundle m_uiSwingBundle;

  public UiSwingNodePage(AbstractPage parentPage, IScoutBundle uiSwingBundle) {
    setParent(parentPage);
    m_uiSwingBundle = uiSwingBundle;
    setName(getScoutResource().getSimpleName());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SwingBundle));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.UI_SWING_NODE_PAGE;
  }

  @Override
  public int getOrder() {
    return 10;
  }

  @Override
  public boolean isChildrenLoaded() {
    return true;
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public IScoutBundle getScoutResource() {
    return m_uiSwingBundle;
  }

}
