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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.ContextMenuContributorExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.ui.services.IServiceLocator;

public class MenuVisibilityTester extends PropertyTester {

  private static final int MENU_PREPARATION_MAX_DURATION = 15; // ms

  private static <T> boolean contains(T[] list, T toSearch) {
    if (list == null) return false;
    for (T t : list) {
      if (t != null && t.equals(toSearch)) return true;
    }
    return false;
  }

  private static IContextMenuContributor getMenuContributor(Class<? extends IScoutHandler> menu, IPage p) {
    for (IContextMenuContributor c : ContextMenuContributorExtensionPoint.getContextMenuContributors(p)) {
      try {
        Class<? extends IScoutHandler>[] menus = c.getSupportedMenuActionsFor(p);
        if (contains(menus, menu)) {
          return c;
        }
      }
      catch (Exception t) {
        ScoutSdkUi.logError("Could not get supported menues for extension '" + c.toString() + "'!");
      }
    }

    return null;
  }

  private static HashMap<IPage, IContextMenuContributor> getMenuContributorsForAllPages(Class<? extends IScoutHandler> menu, List<IPage> pages) {
    HashMap<IPage, IContextMenuContributor> mapping = new HashMap<IPage, IContextMenuContributor>();
    for (IPage p : pages) {
      IContextMenuContributor c = getMenuContributor(menu, p);
      mapping.put(p, c);
    }
    return mapping;
  }

  private static void prepareMenu(IScoutHandler menu, HashMap<IPage, IContextMenuContributor> contributors) {
    for (Entry<IPage, IContextMenuContributor> entry : contributors.entrySet()) {
      entry.getValue().prepareMenuAction(entry.getKey(), menu);
    }
  }

  private static boolean isMenuAvailableInAllPages(HashMap<IPage, IContextMenuContributor> contributors) {
    for (IContextMenuContributor c : contributors.values()) {
      if (c == null) return false;
    }
    return true;
  }

  private static List<IPage> getFilteredSelection(Collection<?> c) {
    ArrayList<IPage> ret = new ArrayList<IPage>(c.size());
    for (Object o : c) {
      if (o instanceof IPage) {
        ret.add((IPage) o);
      }
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (receiver instanceof StructuredSelection) {
      receiver = ((StructuredSelection) receiver).toList();
    }
    if (receiver instanceof Collection && args != null && args.length == 3 && args[0] instanceof Class && args[1] instanceof Command && args[2] instanceof IServiceLocator) {
      if (IScoutHandler.class.isAssignableFrom((Class<?>) args[0])) {
        List<IPage> selectedNodes = getFilteredSelection((Collection<?>) receiver);
        if (selectedNodes.size() == 0) {
          return false;
        }
        else {
          try {
            // get class of current menu
            Class<? extends IScoutHandler> currentMenuClass = (Class<? extends IScoutHandler>) args[0];
            Command cmd = (Command) args[1];

            // for each page in the selection: get a contributor that supports the menu
            HashMap<IPage, IContextMenuContributor> contributors = getMenuContributorsForAllPages(currentMenuClass, selectedNodes);

            // check if a contributor is available for each selected page (if not: the menu is not supported by each selected node)
            if (!isMenuAvailableInAllPages(contributors)) return false;

            IScoutHandler currentMenu = ScoutMenuContributionItemFactory.getMenuInstance(currentMenuClass);
            if (currentMenu == null) return false; // no instance could be created -> do not show the menu

            cmd.setHandler(currentMenu);

            // check for multi select
            if (!currentMenu.isMultiSelectSupported() && selectedNodes.size() > 1) return false;

            // prepare the menu
            prepareMenu(currentMenu, contributors);

            long menuPreparationStartTime = System.currentTimeMillis();
            // check if menu is visible
            if (!currentMenu.isVisible()) return false;

            // evaluate the enabled state after the menu has been prepared
            currentMenu.setEnabled(new BooleanHolder(currentMenu.isActive()));
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
          }
        }
      }
    }
    return false;
  }
}
