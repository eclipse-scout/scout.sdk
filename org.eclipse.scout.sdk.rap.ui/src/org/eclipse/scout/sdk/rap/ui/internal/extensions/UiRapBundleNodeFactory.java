/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.rap.ui.internal.extensions;

import org.eclipse.scout.sdk.rap.ui.internal.view.outline.pages.project.UiRapNodePage;
import org.eclipse.scout.sdk.ui.extensions.IPageFactory;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.IProjectNodePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutElement;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class UiRapBundleNodeFactory implements IPageFactory {

  /**
   * @see IScoutElement
   */
  public final static int BUNDLE_UI_RAP = 8;

  public UiRapBundleNodeFactory() {
  }

  @Override
  public void createChildren(IPage parentPage) {
    IScoutProject project = ((IProjectNodePage) parentPage).getScoutResource();
    IScoutBundle[] rapBundles = project.getAllBundles(BUNDLE_UI_RAP);
    for (IScoutBundle b : rapBundles) {
      new UiRapNodePage(parentPage, b);
    }
  }

}
