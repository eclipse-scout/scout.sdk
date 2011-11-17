package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class ContextMenuContributorExtensionPoint {

  private static Object contextMenuExtensionsCacheLock = new Object();
  private static AbstractScoutHandler[] contextMenuExtensions;

  private static Object contextMenuContributorExtensionsCacheLock = new Object();
  private static MenuContributionInfo[] contextMenuContributorExtensions;

  private static class MenuContributionInfo {
    private IContextMenuContributor contributor;
    private Class<? extends IPage> pageClassFilter;

    public MenuContributionInfo(IContextMenuContributor c, Class<? extends IPage> p) {
      contributor = c;
      pageClassFilter = p;
    }
  }

  interface IExtensionVisitor {
    boolean visit(IConfigurationElement element);
  }

  public static IContextMenuContributor[] getContextMenuContributors(IPage page) {
    ArrayList<IContextMenuContributor> ret = new ArrayList<IContextMenuContributor>();
    for (MenuContributionInfo i : getContributors()) {
      if (page == null || i.pageClassFilter == null || i.pageClassFilter.isAssignableFrom(page.getClass())) {
        ret.add(i.contributor);
      }
    }
    return ret.toArray(new IContextMenuContributor[ret.size()]);
  }

  private static MenuContributionInfo[] getContributors() {
    if (contextMenuContributorExtensions == null) {
      synchronized (contextMenuContributorExtensionsCacheLock) {
        if (contextMenuContributorExtensions == null) {
          final ArrayList<MenuContributionInfo> list = new ArrayList<MenuContributionInfo>();
          visitExtensions("contextMenuContributor", "contributor", new IExtensionVisitor() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean visit(IConfigurationElement element) {
              try {
                String pageClassName = element.getAttribute("page");
                IContextMenuContributor ext = (IContextMenuContributor) element.createExecutableExtension("class");
                Class<? extends IPage> clazz = null;
                if (pageClassName != null) {
                  clazz = (Class<? extends IPage>) Class.forName(pageClassName.trim());
                }

                MenuContributionInfo info = new MenuContributionInfo(ext, clazz);
                list.add(info);
              }
              catch (Throwable t) {
                ScoutSdkUi.logError("create context menu contributor: " + element.getAttribute("class"), t);
              }
              return true;
            }
          });
          contextMenuContributorExtensions = list.toArray(new MenuContributionInfo[list.size()]);
        }
      }
    }
    return contextMenuContributorExtensions;
  }

  public static void visitExtensions(String extensionPointName, String elementName, IExtensionVisitor v) {
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

  public static AbstractScoutHandler[] getAllRegisteredContextMenus() {
    if (contextMenuExtensions == null) {
      synchronized (contextMenuExtensionsCacheLock) {
        if (contextMenuExtensions == null) {
          final ArrayList<AbstractScoutHandler> list = new ArrayList<AbstractScoutHandler>();
          visitExtensions("contextMenu", "contextMenu", new IExtensionVisitor() {
            @Override
            public boolean visit(IConfigurationElement element) {
              try {
                AbstractScoutHandler ext = (AbstractScoutHandler) element.createExecutableExtension("class");
                list.add(ext);
              }
              catch (Throwable t) {
                ScoutSdkUi.logError("create context menu: " + element.getAttribute("class"), t);
              }
              return true;
            }
          });
          contextMenuExtensions = list.toArray(new AbstractScoutHandler[list.size()]);
        }
      }
    }
    return contextMenuExtensions;
  }
}
