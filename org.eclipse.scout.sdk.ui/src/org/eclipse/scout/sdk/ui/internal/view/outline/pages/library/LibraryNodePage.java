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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.library;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.library.LibrariesBundleUnlinkAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

/**
 * <h3>{@link LibraryNodePage}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 28.02.2012
 */
public class LibraryNodePage extends AbstractPage {

  private final IPluginModelBase m_pluginModel;

  public LibraryNodePage(IPage parent, IPluginModelBase pluginModel) {
    m_pluginModel = pluginModel;
    setParent(parent);
    setName(m_pluginModel.getPluginBase().getId());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Library));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.LIBRARIES_NODE_PAGE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{LibrariesBundleUnlinkAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu.getClass() == LibrariesBundleUnlinkAction.class) {
      LibrariesBundleUnlinkAction unlinkAction = (LibrariesBundleUnlinkAction) menu;
      unlinkAction.addLibraryToRemove(getScoutBundle(), getPluginModel());
    }
    else {
      super.prepareMenuAction(menu);
    }
  }

  public IPluginModelBase getPluginModel() {
    return m_pluginModel;
  }
}
