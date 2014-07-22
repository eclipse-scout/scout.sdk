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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutBundleNode;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

/**
 * <h3>UiSwtNodePage</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class UiSwtNodePage extends AbstractBundleNodeTablePage {

  public UiSwtNodePage(IPage parentPage, ScoutBundleNode node) {
    super(parentPage, node);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.UI_SWT_NODE_PAGE;
  }
}
