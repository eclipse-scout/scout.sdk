package org.eclipse.scout.sdk.ui.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.ContextMenuContributorExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class MenuVisibilityTester extends PropertyTester {

  private static <T> boolean contains(T[] list, T toSearch) {
    if (list == null) return false;
    for (T t : list) {
      if (t != null && t.equals(toSearch)) return true;
    }
    return false;
  }

  private static IContextMenuContributor getMenuContributor(Class<? extends AbstractScoutHandler> menu, IPage p) {
    for (IContextMenuContributor c : ContextMenuContributorExtensionPoint.getContextMenuContributors(p)) {
      Class<? extends AbstractScoutHandler>[] menus = c.getSupportedMenuActionsFor(p);
      if (contains(menus, menu)) return c;
    }
    return null;
  }

  private static HashMap<IPage, IContextMenuContributor> getMenuContributorsForAllPages(Class<? extends AbstractScoutHandler> menu, IPage[] pages) {
    HashMap<IPage, IContextMenuContributor> mapping = new HashMap<IPage, IContextMenuContributor>();
    for (IPage p : pages) {
      IContextMenuContributor c = getMenuContributor(menu, p);
      mapping.put(p, c);
    }
    return mapping;
  }

  private static void prepareMenu(AbstractScoutHandler menu, HashMap<IPage, IContextMenuContributor> contributors) {
    for (IPage p : contributors.keySet()) {
      contributors.get(p).prepareMenuAction(p, menu);
    }
  }

  private static boolean isMenuAvailableInAllPages(HashMap<IPage, IContextMenuContributor> contributors) {
    for (IContextMenuContributor c : contributors.values()) {
      if (c == null) return false;
    }
    return true;
  }

  private static IPage[] getFilteredSelection(Collection c) {
    ArrayList<IPage> ret = new ArrayList<IPage>(c.size());
    for (Object o : c) {
      if (o instanceof IPage) {
        ret.add((IPage) o);
      }
    }
    return ret.toArray(new IPage[ret.size()]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (receiver instanceof Collection && args != null && args.length == 2 && args[0] instanceof Class && args[1] instanceof Command) {
      if (AbstractScoutHandler.class.isAssignableFrom((Class<?>) args[0])) {
        IPage[] selectedNodes = getFilteredSelection((Collection) receiver);
        if (selectedNodes.length == 0) {
          return false;
        }
        else {
          try {
            // get class of current menu
            Class<? extends AbstractScoutHandler> currentMenuClass = (Class<? extends AbstractScoutHandler>) args[0];
            Command cmd = (Command) args[1];

            // for each page in the selection: get a contributor that supports the menu
            HashMap<IPage, IContextMenuContributor> contributors = getMenuContributorsForAllPages(currentMenuClass, selectedNodes);

            // check if a contributor is available for each selected page (if not: the menu is not supported by each selected node)
            if (!isMenuAvailableInAllPages(contributors)) return false;

            AbstractScoutHandler currentMenu = ScoutMenuContributionItemFactory.getMenuInstance(currentMenuClass);
            if (currentMenu == null) return false; // no instance could be created -> do not show the menu

            // check for multi select
            if (!currentMenu.isMultiSelectSupported() && selectedNodes.length > 1) return false;

            // prepare the menu
            prepareMenu(currentMenu, contributors);

            // check if menu is visible
            if (!currentMenu.isVisible()) return false;

            // if we come here all selected rows support the current menu and the menu is visible and prepared.
            cmd.setHandler(currentMenu);

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
