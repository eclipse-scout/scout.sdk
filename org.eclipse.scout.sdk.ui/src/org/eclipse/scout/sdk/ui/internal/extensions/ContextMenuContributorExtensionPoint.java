package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IContextMenuContributor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class ContextMenuContributorExtensionPoint extends SdkExtensionPointHelper {

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
          SdkExtensionPointHelper.visitExtensions("contextMenuContributor", "contributor", new IExtensionVisitor() {
            @SuppressWarnings("unchecked")
            @Override
            public void visit(IConfigurationElement element) {
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
            }
          });
          contextMenuContributorExtensions = list.toArray(new MenuContributionInfo[list.size()]);
        }
      }
    }
    return contextMenuContributorExtensions;
  }

  public static AbstractScoutHandler[] getAllRegisteredContextMenus() {
    if (contextMenuExtensions == null) {
      synchronized (contextMenuExtensionsCacheLock) {
        if (contextMenuExtensions == null) {
          final ArrayList<AbstractScoutHandler> list = new ArrayList<AbstractScoutHandler>();
          SdkExtensionPointHelper.visitExtensions("contextMenu", "contextMenu", new IExtensionVisitor() {
            @Override
            public void visit(IConfigurationElement element) {
              try {
                AbstractScoutHandler ext = (AbstractScoutHandler) element.createExecutableExtension("class");
                list.add(ext);
              }
              catch (Throwable t) {
                ScoutSdkUi.logError("create context menu: " + element.getAttribute("class"), t);
              }
            }
          });
          contextMenuExtensions = list.toArray(new AbstractScoutHandler[list.size()]);
        }
      }
    }
    return contextMenuExtensions;
  }
}
