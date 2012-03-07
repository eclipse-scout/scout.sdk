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
package @@BUNDLE_SWT_NAME@@.application.menu;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.ui.actions.CompoundContributionItem;
import @@BUNDLE_SWT_NAME@@.Activator;

public class DesktopMenuBar extends CompoundContributionItem {

  @Override
  protected IContributionItem[] getContributionItems() {
    ISwtEnvironment env = Activator.getDefault().getEnvironment();
    if (env.isInitialized()) {
      IMenu[] menus = env.getClientSession().getDesktop().getMenus();
      return SwtMenuUtility.getMenuContribution(menus, env);
    }
    else {
      return new IContributionItem[0];
    }
  }
}
