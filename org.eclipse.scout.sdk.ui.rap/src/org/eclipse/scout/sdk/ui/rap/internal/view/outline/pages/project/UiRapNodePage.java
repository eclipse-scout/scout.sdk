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
package org.eclipse.scout.sdk.ui.rap.internal.view.outline.pages.project;

import org.eclipse.scout.sdk.ui.rap.ScoutSdkRapUI;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>UiRapNodePage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 20.10.2011
 */
public class UiRapNodePage extends AbstractPage {

  private final IScoutBundle m_scoutBundle;

  public UiRapNodePage(IPage parentPage, IScoutBundle uiRapBundle) {
    setParent(parentPage);
    m_scoutBundle = uiRapBundle;
    setName(getScoutResource().getSimpleName());
    setImageDescriptor(ScoutSdkRapUI.getImageDescriptor(ScoutSdkRapUI.RapBundle));
  }

  @Override
  public String getPageId() {
    return UiRapNodePage.class.getName();
  }

  @Override
  public int getOrder() {
    return 20;
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
    return m_scoutBundle;
  }

}
