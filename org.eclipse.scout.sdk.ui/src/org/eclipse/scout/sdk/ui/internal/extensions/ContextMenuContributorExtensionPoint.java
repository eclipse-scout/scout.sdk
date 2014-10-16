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
package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.osgi.framework.Bundle;

public class ContextMenuContributorExtensionPoint {

  private static final Object LOCK = new Object();

  private static volatile List<IScoutHandler> contextMenuExtensions;
  private static volatile List<MenuContributionInfo> contextMenuContributorExtensions;
  private static volatile Map<IScoutHandler.Category, List<IScoutHandler>> contextMenuByCat;

  private static class MenuContributionInfo {
    private IContextMenuContributor contributor;
    private Class<? extends IPage> pageClassFilter;

    public MenuContributionInfo(IContextMenuContributor c, Class<? extends IPage> p) {
      contributor = c;
      pageClassFilter = p;
    }
  }

  private interface IExtensionVisitor {
    boolean visit(IConfigurationElement element);
  }

  public static List<IContextMenuContributor> getContextMenuContributors(Object o) {
    List<MenuContributionInfo> contributors = getContributors();
    ArrayList<IContextMenuContributor> ret = new ArrayList<IContextMenuContributor>(contributors.size());
    for (MenuContributionInfo i : contributors) {
      if (o == null || i.pageClassFilter == null || i.pageClassFilter.isAssignableFrom(o.getClass())) {
        ret.add(i.contributor);
      }
    }
    return ret;
  }

  private static List<MenuContributionInfo> getContributors() {
    if (contextMenuContributorExtensions == null) {
      synchronized (LOCK) {
        if (contextMenuContributorExtensions == null) {
          final List<MenuContributionInfo> list = new ArrayList<MenuContributionInfo>();
          visitExtensions("contextMenuContributor", "contributor", new IExtensionVisitor() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean visit(IConfigurationElement element) {
              try {
                IContextMenuContributor ext = (IContextMenuContributor) element.createExecutableExtension("class");

                // page filter
                String pageClassName = StringUtility.trim(element.getAttribute("page"));
                Class<? extends IPage> clazz = null;
                if (!StringUtility.isNullOrEmpty(pageClassName)) {
                  String contribPlugin = element.getContributor().getName();
                  Bundle bundle = Platform.getBundle(contribPlugin);
                  if (bundle != null) {
                    clazz = (Class<? extends IPage>) bundle.loadClass(pageClassName);
                  }
                }

                MenuContributionInfo info = new MenuContributionInfo(ext, clazz);
                list.add(info);
              }
              catch (Exception t) {
                ScoutSdkUi.logError("create context menu contributor: " + element.getAttribute("class"), t);
              }
              return true;
            }
          });

          // sort by class name
          Collections.sort(list, new Comparator<MenuContributionInfo>() {
            @Override
            public int compare(MenuContributionInfo o1, MenuContributionInfo o2) {
              return o1.contributor.getClass().getSimpleName().compareTo(o2.contributor.getClass().getSimpleName());
            }
          });

          contextMenuContributorExtensions = CollectionUtility.arrayList(list);
        }
      }
    }
    return contextMenuContributorExtensions;
  }

  private static void visitExtensions(String extensionPointName, String elementName, IExtensionVisitor v) {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, extensionPointName);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if (elementName.equals(element.getName())) {
          if (!v.visit(element)) {
            return; // cancel when requested
          }
        }
      }
    }
  }

  public static List<IScoutHandler> getAllRegisteredContextMenus() {
    if (contextMenuExtensions == null) {
      synchronized (LOCK) {
        if (contextMenuExtensions == null) {
          final ArrayList<IScoutHandler> list = new ArrayList<IScoutHandler>();
          visitExtensions("contextMenu", "contextMenu", new IExtensionVisitor() {
            @Override
            public boolean visit(IConfigurationElement element) {
              try {
                IScoutHandler ext = (IScoutHandler) element.createExecutableExtension("class");
                list.add(ext);
              }
              catch (Exception t) {
                ScoutSdkUi.logError("Unable to create context menu: " + element.getAttribute("class"), t);
              }
              return true;
            }
          });
          contextMenuExtensions = CollectionUtility.arrayList(list);
        }
      }
    }
    return CollectionUtility.arrayList(contextMenuExtensions);
  }

  public static Map<IScoutHandler.Category, List<IScoutHandler>> getAllRegisteredContextMenusByCategory() {
    if (contextMenuByCat == null) {
      synchronized (LOCK) {
        if (contextMenuByCat == null) {
          TreeMap<IScoutHandler.Category, List<IScoutHandler>> sorted =
              new TreeMap<IScoutHandler.Category, List<IScoutHandler>>(new Comparator<IScoutHandler.Category>() {
                @Override
                public int compare(IScoutHandler.Category o1, IScoutHandler.Category o2) {
                  return Integer.valueOf(o1.getOrder()).compareTo(o2.getOrder());
                }
              });

          // group and sort all actions by category
          for (IScoutHandler a : getAllRegisteredContextMenus()) {
            List<IScoutHandler> listOfCurCat = sorted.get(a.getCategory());
            if (listOfCurCat == null) {
              listOfCurCat = new LinkedList<IScoutHandler>();
              sorted.put(a.getCategory(), listOfCurCat);
            }
            listOfCurCat.add(a);
          }

          contextMenuByCat = sorted;
        }
      }
    }

    return new TreeMap<IScoutHandler.Category, List<IScoutHandler>>(contextMenuByCat);
  }
}
