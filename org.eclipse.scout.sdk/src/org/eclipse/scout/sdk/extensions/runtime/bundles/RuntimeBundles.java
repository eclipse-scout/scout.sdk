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
package org.eclipse.scout.sdk.extensions.runtime.bundles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutBundle;

/**
 * <h3>{@link RuntimeBundles}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 31.01.2013
 */
public final class RuntimeBundles {
  private final static String EXTENSION_POINT_NAME = "runtimeBundles";
  private final static String TAG_NAME = "bundle";
  private final static String ATTRIB_NAME = "symbolicName";
  private final static String ATTRIB_TYPE = "type";
  private final static String ATTRIB_ORDER = "order";

  private final static Object lock = new Object();
  private static Set<String /* symbolic name */> allScoutRtBundles = null;
  private static Map<String /* symbolic name */, String /* type */> bundleToTypeMap = null;
  private static Map<String /* type */, String /* symbolic name */> typeToBundleMap = null;

  private RuntimeBundles() {
  }

  private static void ensureCached() {
    if (allScoutRtBundles == null || bundleToTypeMap == null || typeToBundleMap == null) {
      synchronized (lock) {
        if (allScoutRtBundles == null || bundleToTypeMap == null || typeToBundleMap == null) {
          Set<String> all = new HashSet<String>();
          TreeMap<Integer, String[]> typeDefOrdered = new TreeMap<Integer, String[]>();

          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME.equals(element.getName())) {
                String symbolicName = element.getAttribute(ATTRIB_NAME);
                if (StringUtility.hasText(symbolicName) && PluginRegistry.findModel(symbolicName) != null) {
                  all.add(symbolicName);

                  String order = element.getAttribute(ATTRIB_ORDER);
                  String type = StringUtility.trim(element.getAttribute(ATTRIB_TYPE));

                  if (StringUtility.hasText(order) && StringUtility.hasText(type)) {
                    Integer o = parseOrder(order);
                    if (o != null) {
                      typeDefOrdered.put(o, new String[]{symbolicName, type});
                    }
                    else {
                      ScoutSdk.logWarning("The order must be a valid integer.");
                    }
                  }
                }
                else {
                  // RT bundle could not be found.
                }
              }
            }
          }
          allScoutRtBundles = all;
          bundleToTypeMap = new LinkedHashMap<String, String>(typeDefOrdered.size());
          typeToBundleMap = new HashMap<String, String>(typeDefOrdered.size());
          for (String[] items : typeDefOrdered.values()) {
            bundleToTypeMap.put(items[0], items[1]);
            typeToBundleMap.put(items[1], items[0]);
          }
        }
      }
    }
  }

  private static Integer parseOrder(String order) {
    try {
      return Integer.parseInt(order);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  public static String getBundleSymbolicName(String bundleType) {
    ensureCached();
    return typeToBundleMap.get(bundleType);
  }

  public static String getBundleType(String symbolicName) {
    IPluginModelBase model = PluginRegistry.findModel(symbolicName);
    return getBundleType(model);
  }

  public static String getBundleType(IProject p) {
    if (p == null) {
      return null;
    }
    IPluginModelBase model = PluginRegistry.findModel(p);
    return getBundleType(model);
  }

  public static String getBundleType(IPluginModelBase bundle) {
    if (bundle == null) {
      return null;
    }
    ScoutBundle tmp = new ScoutBundle(bundle); // temporary instance to get all dependencies
    return getBundleType(tmp);
  }

  public static String getBundleType(ScoutBundle bundle) {
    if (bundle == null) {
      return null;
    }
    ensureCached();
    for (Entry<String, String> entry : bundleToTypeMap.entrySet()) {
      for (IPluginModelBase dependency : bundle.getAllDependencies()) {
        if (CompareUtility.equals(dependency.getBundleDescription().getSymbolicName(), entry.getKey())) {
          return entry.getValue();
        }
      }
    }
    return null;
  }

  public static String[] getTypes() {
    ensureCached();
    return bundleToTypeMap.values().toArray(new String[bundleToTypeMap.size()]);
  }

  public static boolean containsTypeDefiningBundle(String symbolicName) {
    ensureCached();
    return bundleToTypeMap.containsKey(symbolicName);
  }

  public static boolean contains(String symbolicName) {
    ensureCached();
    return allScoutRtBundles.contains(symbolicName);
  }

  public static boolean containsTypeDefiningBundle(BundleDescription b) {
    if (b == null) {
      return false;
    }
    return containsTypeDefiningBundle(b.getSymbolicName());
  }

  public static boolean contains(BundleDescription b) {
    if (b == null) {
      return false;
    }
    return contains(b.getSymbolicName());
  }
}
