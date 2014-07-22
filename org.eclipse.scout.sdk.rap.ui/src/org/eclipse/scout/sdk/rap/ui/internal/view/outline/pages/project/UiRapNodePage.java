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
package org.eclipse.scout.sdk.rap.ui.internal.view.outline.pages.project;

import org.eclipse.scout.sdk.rap.ui.internal.action.RapTargetNewAction;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutBundleNode;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * <h3>UiRapNodePage</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 20.10.2011
 */
public class UiRapNodePage extends AbstractBundleNodeTablePage {

  public UiRapNodePage(IPage parentPage, ScoutBundleNode node) {
    super(parentPage, node);
  }

  @Override
  public String getPageId() {
    return "org.eclipse.scout.sdk.page.UiRapNodePage";
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{RapTargetNewAction.class};
  }
}
