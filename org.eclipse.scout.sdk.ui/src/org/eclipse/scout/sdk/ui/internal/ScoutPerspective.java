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

import org.eclipse.jdt.ui.JavaUI;
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

    String id_topLeft = "topLeft";
    String id_bottomLeft = "bottomLeft";
    String id_bottom = "bottom";

    // Top left: Scout Explorer and JDT package explorer
    IFolderLayout topLeft = layout.createFolder(id_topLeft, IPageLayout.LEFT, 0.25f, editorArea);
    topLeft.addView(IScoutConstants.SCOUT_EXPLORER_VIEW);
    topLeft.addView("org.eclipse.jdt.ui.PackageExplorer");

    // Bottom left: Scout Property view and Outline View
    IFolderLayout bottomLeft = layout.createFolder(id_bottomLeft, IPageLayout.BOTTOM, 0.5f, id_topLeft);
    bottomLeft.addView(IScoutConstants.SCOUT_PROPERTY_VIEW);
    bottomLeft.addView(IPageLayout.ID_OUTLINE);

    // Bottom: Problems, Progress, Tasks, Console, Javadoc, Error Log
    IFolderLayout bottom = layout.createFolder(id_bottom, IPageLayout.BOTTOM, 0.8f, editorArea);
    bottom.addView(IPageLayout.ID_TASK_LIST);
    bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
    bottom.addView(JavaUI.ID_JAVADOC_VIEW);
    bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
    bottom.addView(IPageLayout.ID_PROGRESS_VIEW);
    bottom.addView("org.eclipse.pde.runtime.LogView");
  }
}
