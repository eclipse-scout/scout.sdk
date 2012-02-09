package org.eclipse.scout.sdk.ui.internal.extensions.ear;

import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ui.extensions.ear.IEarEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

public final class EarEntryExtensionPoint {

  public final static String EXTENSION_ID = "earEntry";
  public final static String ATTR_ICON = "icon";
  public final static String ATTR_ID = "id";
  public final static String ATTR_NAME = "name";
  public final static String ATTR_ORDER = "order";
  public final static String ATTR_CLASS = "class";

  private EarEntryExtensionPoint() {
  }

  public static EarEntry[] getEarEntries() {
    TreeSet<EarEntry> ret = new TreeSet<EarEntry>();
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_ID);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        try {
          String id = element.getAttribute(ATTR_ID);
          String name = element.getAttribute(ATTR_NAME);
          int order = getOrder(element.getAttribute(ATTR_ORDER));
          String icon = element.getAttribute(ATTR_ICON);
          IEarEntryHandler provider = (IEarEntryHandler) element.createExecutableExtension(ATTR_CLASS);
          ret.add(new EarEntry(id, name, order, icon, provider));
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("could not load class '" + element.getAttribute(ATTR_CLASS) + "'.", e);
        }
      }
    }
    return ret.toArray(new EarEntry[ret.size()]);
  }

  private static int getOrder(String order) {
    if (order == null || order.trim().length() == 0) {
      return 0;
    }
    else if (order.matches("[0-9]*")) {
      return Integer.parseInt(order);
    }
    else {
      return 0;
    }
  }
}
