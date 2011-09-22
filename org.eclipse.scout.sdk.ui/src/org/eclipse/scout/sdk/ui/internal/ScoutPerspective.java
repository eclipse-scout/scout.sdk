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
package org.eclipse.scout.sdk.ui.internal;

import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * For an example see JavaPerspectiveFactory
 */
public class ScoutPerspective implements IPerspectiveFactory {

  public ScoutPerspective() {
  }

  @Override
  public void createInitialLayout(IPageLayout layout) {
    defineActions(layout);
    defineLayout(layout);
  }

  public void defineActions(IPageLayout layout) {
  }

  public void defineLayout(IPageLayout layout) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible(true);

    layout.addView(IScoutConstants.SCOUT_EXPLORER_VIEW, IPageLayout.LEFT, 0.5f, editorArea);
    //
    IFolderLayout outlineRight = layout.createFolder("outlineRight", IPageLayout.RIGHT, 0.5f, IScoutConstants.SCOUT_EXPLORER_VIEW);
    outlineRight.addView(IScoutConstants.SCOUT_PROPERTY_VIEW);
    outlineRight.addView(IPageLayout.ID_OUTLINE);
    //
    IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.8f, editorArea);
    bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
    bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
    bottom.addView("org.eclipse.pde.runtime.LogView");

  }
}
