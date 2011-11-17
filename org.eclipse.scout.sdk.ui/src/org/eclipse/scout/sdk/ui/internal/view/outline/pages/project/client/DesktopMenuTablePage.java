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
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.DesktopMenuNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * <h3>DesktopMenuTablePage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 30.06.2010
 */
public class DesktopMenuTablePage extends MenuTablePage {

  /**
   * @param parentPage
   * @param menuDeclaringType
   */
  public DesktopMenuTablePage(IPage parentPage, IType menuDeclaringType) {
    super(parentPage, menuDeclaringType);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Menus));
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    ((DesktopMenuNewAction) menu).setType(getDeclaringType());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{DesktopMenuNewAction.class};
  }
}
