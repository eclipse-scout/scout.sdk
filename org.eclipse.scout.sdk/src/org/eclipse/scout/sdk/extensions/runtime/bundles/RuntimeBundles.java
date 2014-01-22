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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link RuntimeBundles}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 31.01.2013
 */
public final class RuntimeBundles {
  private static final String EXTENSION_POINT_NAME = "runtimeBundles";
  private static final String TAG_NAME = "bundle";
  private static final String ATTRIB_NAME = "symbolicName";
  private static final String ATTRIB_TYPE = "type";
  private static final String ATTRIB_ORDER = "order";

  private static final Object lock = new Object();
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
                if (StringUtility.hasText(symbolicName)) {
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
                  ScoutSdk.logWarning("No symbolic name defined for extension '" + element.getNamespaceIdentifier() + "'.");
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

  /**
   * Gets the bundle symbolic name that defines the given bundle type.
   * 
   * @param bundleType
   *          one of the bundle types. Bundle types can be defined using the
   *          <code>org.eclipse.scout.sdk.runtimeBundles</code> extension point. The predefined types are available in
   *          the {@link IScoutBundle}.TYPE* constants.
   * @return the symbolic name of the bundle that defines the given type.
   */
  public static String getBundleSymbolicName(String bundleType) {
    ensureCached();
    return typeToBundleMap.get(bundleType);
  }

  /**
   * Gets the bundle type for the given plug-in. This is the type of first type-defining-bundle that is found in the
   * dependencies of the given project.
   * 
   * @param symbolicName
   *          The bundle symbolic name. There must be a plug-in with the given name in the registry.
   * @return The type of the type-defining bundle with the lowest (first) order number. If no type-defining-bundle is in
   *         the dependencies of the given bundle, this method returns null. types can be added using the
   *         <code>org.eclipse.scout.sdk.runtimeBundles</code> extension point. The predefined types are available in
   *         the {@link IScoutBundle}.TYPE* constants.
   */
  public static String getBundleType(String symbolicName) {
    IPluginModelBase model = PluginRegistry.findModel(symbolicName);
    return getBundleType(model);
  }

  /**
   * Gets the bundle type for the given plug-in project. This is the type of first type-defining-bundle that is found
   * in the dependencies of the given project.
   * 
   * @param p
   *          The project to get the bundle type for. Must be a plug-in project.
   * @return The type of the type-defining bundle with the lowest (first) order number. If no type-defining-bundle is in
   *         the dependencies of the given bundle, this method returns null. types can be added using the
   *         <code>org.eclipse.scout.sdk.runtimeBundles</code> extension point. The predefined types are available in
   *         the {@link IScoutBundle}.TYPE* constants.
   */
  public static String getBundleType(IProject p) {
    if (p == null) {
      return null;
    }
    IPluginModelBase model = PluginRegistry.findModel(p);
    return getBundleType(model);
  }

  /**
   * Gets the bundle type for the given bundle. This is the type of first type-defining-bundle that is found in the
   * dependencies of the given bundle.
   * 
   * @param bundle
   *          The bundle to get the type for.
   * @return The type of the type-defining bundle with the lowest (first) order number. If no type-defining-bundle is in
   *         the dependencies of the given bundle, this method returns null. types can be added using the
   *         <code>org.eclipse.scout.sdk.runtimeBundles</code> extension point. The predefined types are available in
   *         the {@link IScoutBundle}.TYPE* constants.
   */
  public static String getBundleType(IPluginModelBase bundle) {
    if (bundle == null) {
      return null;
    }
    ScoutBundle tmp = new ScoutBundle(bundle, new NullProgressMonitor()); // temporary instance to get all dependencies
    return getBundleType(tmp);
  }

  /**
   * Gets the bundle type for the given scout bundle. This is the type of first type-defining-bundle that is found
   * in the dependencies of the given bundle.
   * 
   * @param bundle
   *          The bundle to get the type for.
   * @return The type of the type-defining bundle with the lowest (first) order number. If no type-defining-bundle is in
   *         the dependencies of the given bundle, this method returns null. types can be added using the
   *         <code>org.eclipse.scout.sdk.runtimeBundles</code> extension point. The predefined types are available in
   *         the {@link IScoutBundle}.TYPE* constants.
   */
  public static String getBundleType(ScoutBundle bundle) {
    if (bundle == null) {
      return null;
    }
    Set<IPluginModelBase> allDependencies = bundle.getAllDependencies();
    String[] symbolicNames = new String[allDependencies.size()];
    int i = 0;
    for (IPluginModelBase dependency : allDependencies) {
      symbolicNames[i] = dependency.getBundleDescription().getSymbolicName();
      i++;
    }
    return getBundleType(symbolicNames);
  }

  /**
   * Gets the bundle type for the given symbolic name list. This is the type of first type-defining-bundle that is found
   * in the given list
   * 
   * @param symbolicNames
   *          The list of bundle symbolic names for which the type should be evaluated.
   * @return The type of the type-defining bundle with the lowest (first) order number. if no type-defining-bundle is in
   *         the given list, this method returns null. types can be added using the
   *         <code>org.eclipse.scout.sdk.runtimeBundles</code> extension point. The predefined types are available in
   *         the {@link IScoutBundle}.TYPE* constants.
   */
  public static String getBundleType(String[] symbolicNames) {
    if (symbolicNames == null || symbolicNames.length < 1) {
      return null;
    }
    ensureCached();
    for (Entry<String, String> entry : bundleToTypeMap.entrySet()) {
      for (String bundle : symbolicNames) {
        if (CompareUtility.equals(bundle, entry.getKey())) {
          return entry.getValue();
        }
      }
    }
    return null;
  }

  /**
   * @return Gets all bundle types registered.
   */
  public static String[] getTypes() {
    ensureCached();
    return bundleToTypeMap.values().toArray(new String[bundleToTypeMap.size()]);
  }

  /**
   * Checks if the given bundle is a type defining bundle.
   * 
   * @param symbolicName
   * @return true if it is a type-defining-bundle. false otherwise.
   */
  public static boolean containsTypeDefiningBundle(String symbolicName) {
    ensureCached();
    return bundleToTypeMap.containsKey(symbolicName);
  }

  /**
   * Checks if the given bundle is a type defining bundle.
   * 
   * @param b
   * @return true if it is a type-defining-bundle. false otherwise.
   */
  public static boolean containsTypeDefiningBundle(BundleDescription b) {
    if (b == null) {
      return false;
    }
    return containsTypeDefiningBundle(b.getSymbolicName());
  }

  /**
   * @param b
   *          the bundle to evaluate
   * @return true if the given bundle is a scout runtime bundle.
   */
  public static boolean contains(BundleDescription b) {
    if (b == null) {
      return false;
    }
    return contains(b.getSymbolicName());
  }

  /**
   * @param symbolicName
   *          the symbolic name to evaluate
   * @return true if the given symbolic name is a scout runtime bundle.
   */
  public static boolean contains(String symbolicName) {
    ensureCached();
    return allScoutRtBundles.contains(symbolicName);
  }
}
