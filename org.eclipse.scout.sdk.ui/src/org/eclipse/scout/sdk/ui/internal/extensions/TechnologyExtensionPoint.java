package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.Technology;

public final class TechnologyExtensionPoint {

  public final static String EXTENSION_ID = "technology";
  public final static String TAG_TECH = "technology";
  public final static String TAG_HANDLER = "handler";
  public final static String ATTR_NAME = "name";
  public final static String ATTR_ID = "id";
  public final static String ATTR_TECH = "technology";
  public final static String ATTR_CLASS = "class";
  public final static String ATTR_CATEGORY = "category";

  private static Technology[] technologies;

  private TechnologyExtensionPoint() {
  }

  private static Map<String /* tech id */, ArrayList<IScoutTechnologyHandler>> getHandlers() {
    HashMap<String, ArrayList<IScoutTechnologyHandler>> techHandlers = new HashMap<String, ArrayList<IScoutTechnologyHandler>>();
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_ID);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if (TAG_HANDLER.equals(element.getName())) {
          try {
            String techId = element.getAttribute(ATTR_TECH);
            ArrayList<IScoutTechnologyHandler> handlers = techHandlers.get(techId);
            if (handlers == null) {
              handlers = new ArrayList<IScoutTechnologyHandler>();
              techHandlers.put(techId, handlers);
            }
            IScoutTechnologyHandler handler = (IScoutTechnologyHandler) element.createExecutableExtension(ATTR_CLASS);
            handlers.add(handler);
          }
          catch (CoreException e) {
            ScoutSdkUi.logError("Unable to load technology handler class '" + element.getAttribute(ATTR_CLASS) + "'", e);
          }
        }
      }
    }
    return techHandlers;
  }

  public static Technology[] getTechnologyExtensions() {
    if (technologies == null) {
      Map<String, ArrayList<IScoutTechnologyHandler>> handlers = getHandlers();
      HashSet<Technology> techs = new HashSet<Technology>();
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_ID);
      IExtension[] extensions = xp.getExtensions();
      for (IExtension extension : extensions) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (IConfigurationElement element : elements) {
          if (TAG_TECH.equals(element.getName())) {
            String id = element.getAttribute(ATTR_ID);
            String name = element.getAttribute(ATTR_NAME);
            String category = element.getAttribute(ATTR_CATEGORY);
            if (category != null && category.trim().length() < 1) {
              category = null;
            }
            if (name != null && id != null && name.trim().length() > 0 && id.trim().length() > 0) {
              Technology t = new Technology(id, name, category);
              t.addAllHandlers(handlers.get(id));
              techs.add(t);
            }
          }
        }
      }
      technologies = techs.toArray(new Technology[techs.size()]);
    }
    return technologies;
  }
}
