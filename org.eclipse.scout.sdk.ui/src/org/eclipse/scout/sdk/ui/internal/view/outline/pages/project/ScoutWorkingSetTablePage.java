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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.ui.IWorkingSet;

/**
 * <h3>{@link ScoutWorkingSetTablePage}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 05.04.2013
 */
public class ScoutWorkingSetTablePage extends AbstractPage {

  private final IWorkingSet m_workingSet;

  public ScoutWorkingSetTablePage(IPage parentPage, IWorkingSet s) {
    setParent(parentPage);
    setName(s.getLabel());
    setImageDescriptor(s.getImageDescriptor());
    m_workingSet = s;
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    for (IAdaptable bundle : m_workingSet.getElements()) {
      if (bundle instanceof IScoutBundle) {
        ProjectsTablePage.createBundlePage(this, (IScoutBundle) bundle);
      }
    }
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SCOUT_WORKING_SET_TABLE_PAGE;
  }
}
