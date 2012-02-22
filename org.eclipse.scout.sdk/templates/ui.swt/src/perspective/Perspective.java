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
package @@BUNDLE_SWT_NAME@@.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import @@BUNDLE_SWT_NAME@@.Activator;

/** <h3>Activator</h3>
 *  ...
*/
public class Perspective implements IPerspectiveFactory {

  @Override
	public void createInitialLayout(IPageLayout layout) {

    layout.setEditorAreaVisible(false);

    layout.setFixed(false);

    layout.addStandaloneViewPlaceholder(Activator.OUTLINE_VIEW_ID,IPageLayout.LEFT,0.2f, IPageLayout.ID_EDITOR_AREA, true);

    String folderId="@@BUNDLE_SWT_NAME@@.viewStack";
    IFolderLayout folderLayout = layout.createFolder(folderId,IPageLayout.RIGHT, 0.3f, Activator.TABLE_PAGE_VIEW_ID);
    folderLayout.addPlaceholder(Activator.TABLE_PAGE_VIEW_ID);
    folderLayout.addPlaceholder(Activator.CENTER_VIEW_ID);

    layout.addStandaloneViewPlaceholder(Activator.SEAECH_VIEW_ID,IPageLayout.BOTTOM,0.7f,  folderId, true);
    IViewLayout outlineSelectorLayout = layout.getViewLayout(Activator.OUTLINE_VIEW_ID);
    outlineSelectorLayout.setCloseable(false);
    outlineSelectorLayout.setMoveable(false);

    IViewLayout tablePageLayout = layout.getViewLayout(Activator.TABLE_PAGE_VIEW_ID);
    tablePageLayout.setCloseable(false);
    tablePageLayout.setMoveable(false);

    IViewLayout searchLayout = layout.getViewLayout(Activator.SEAECH_VIEW_ID);
    searchLayout.setCloseable(false);
    searchLayout.setMoveable(false);
  }

}
