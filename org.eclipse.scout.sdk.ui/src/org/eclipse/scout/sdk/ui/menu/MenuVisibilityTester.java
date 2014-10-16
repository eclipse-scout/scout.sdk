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
package org.eclipse.scout.sdk.ui.menu;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.ContextMenuContributorExtensionPoint;
import org.eclipse.ui.services.IServiceLocator;

public class MenuVisibilityTester extends PropertyTester {

  private static final int MENU_PREPARATION_MAX_DURATION = 15; // ms

  private static IContextMenuContributor getMenuContributor(Class<? extends IScoutHandler> menu, Object o) {
    for (IContextMenuContributor c : ContextMenuContributorExtensionPoint.getContextMenuContributors(o)) {
      Set<Class<? extends IScoutHandler>> supportedMenuActions = c.getSupportedMenuActionsFor(o);
      if (supportedMenuActions != null && supportedMenuActions.contains(menu)) {
        return c;
      }
    }
    return null;
  }

  private static boolean isMenuSupportedForSelection(Class<? extends IScoutHandler> menu, Collection<?> selection) {
    for (Object o : selection) {
      if (getMenuContributor(menu, o) == null) {
        return false;
      }
    }
    return true;
  }

  private static boolean areArgumentsValid(Object[] args) {
    return args != null && args.length == 3 && args[0] instanceof Class && args[1] instanceof Command && args[2] instanceof IServiceLocator && IScoutHandler.class.isAssignableFrom((Class<?>) args[0]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (!areArgumentsValid(args)) {
      return false;
    }

    Collection<?> col = null;
    if (receiver instanceof Collection) {
      col = (Collection<?>) receiver;
    }
    else if (receiver instanceof IStructuredSelection) {
      col = ((IStructuredSelection) receiver).toList();
    }

    if (col == null) {
      return false;
    }
    if (col.isEmpty()) {
      return false;
    }

    try {
      // get class of current menu
      Class<? extends IScoutHandler> currentMenuClass = (Class<? extends IScoutHandler>) args[0];
      Command cmd = (Command) args[1];

      // check if each selected object supports the current menu
      if (!isMenuSupportedForSelection(currentMenuClass, col)) {
        return false;
      }

      IScoutHandler currentMenu = ScoutMenuContributionItemFactory.getMenuInstance(currentMenuClass);
      if (currentMenu == null) {
        return false; // no instance could be created -> do not show the menu
      }

      cmd.setHandler(currentMenu);

      // check for multi select
      if (!currentMenu.isMultiSelectSupported() && col.size() > 1) {
        return false;
      }

      IStructuredSelection curSelection = new StructuredSelection(col.toArray(new Object[col.size()]));

      long menuPreparationStartTime = System.currentTimeMillis();

      // check if menu is visible
      if (!currentMenu.isVisible(curSelection)) {
        return false;
      }

      // evaluate the enabled state after the menu has been prepared
      currentMenu.setEnabled(new BooleanHolder(currentMenu.isActive(curSelection)));
      long duration = System.currentTimeMillis() - menuPreparationStartTime;
      if (duration > MENU_PREPARATION_MAX_DURATION) {
        // preparation of a menu should not take longer than 10ms
        ScoutSdkUi.logWarning("Context menu '" + currentMenuClass.getName() + "' took longer than " + MENU_PREPARATION_MAX_DURATION + "ms to calculate its state (" + duration + "ms).");
      }

      // if we come here, all selected rows support the current menu and the menu is visible and prepared: enable the handler
      IServiceLocator serviceLocator = (IServiceLocator) args[2];
      ScoutMenuContributionItemFactory.activateHandler(serviceLocator, currentMenu);

      // register the key stroke
      ScoutMenuContributionItemFactory.registerKeyStroke(currentMenu.getKeyStroke(), cmd);
      return true;
    }
    catch (Exception e) {
      ScoutSdkUi.logError(e);
      return false;
    }
  }
}
