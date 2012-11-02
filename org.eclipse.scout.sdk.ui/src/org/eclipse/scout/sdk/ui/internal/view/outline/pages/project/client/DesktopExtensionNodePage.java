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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

public class DesktopExtensionNodePage extends DesktopNodePage {

  public DesktopExtensionNodePage(IPage parentPage, IType desktopType) {
    super(parentPage, desktopType);
    setName(Texts.get("DesktopExtensionNodePage"));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.DESKTOP_EXTENSION_NODE_PAGE;
  }
}
