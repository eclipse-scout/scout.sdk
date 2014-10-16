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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link RuntimeBundles}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 31.01.2013
 */
public final class RuntimeBundles {
  public static final String EXTENSION_POINT_NAME = "runtimeBundles";
  public static final String TAG_NAME_BUNDLE = "bundle";
  public static final String ATTRIB_NAME = "symbolicName";
  public static final String ATTRIB_TYPE = "type";
  public static final String ATTRIB_ORDER = "order";
  public static final String TAG_NAME_REF = "ref";
  public static final String ATTRIB_REF_TYPE = "type";

  private static final Object LOCK = new Object();
  private static volatile Set<String /* symbolic name */> allScoutRtBundles = null;
  private static volatile Map<String /* symbolic name */, String /* type */> bundleToTypeMap = null;
  private static volatile Map<String /* type */, String /* symbolic name */> typeToBundleMap = null;
  private static volatile Map<String /* type */, Set<String /* referenced types */>> referencedBundles = null;

  private RuntimeBundles() {
  }

  private static void ensureCached() {
    if (allScoutRtBundles == null || bundleToTypeMap == null || typeToBundleMap == null) {
      synchronized (LOCK) {
        if (allScoutRtBundles == null || bundleToTypeMap == null || typeToBundleMap == null) {
          Set<String> all = new HashSet<String>();
          Map<Integer, String[]> typeDefOrdered = new TreeMap<Integer, String[]>();
          Map<String, Set<String>> refBundles = new HashMap<String, Set<String>>();

          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME_BUNDLE.equals(element.getName())) {
                String symbolicName = element.getAttribute(ATTRIB_NAME);
                if (StringUtility.hasText(symbolicName)) {
                  all.add(symbolicName);

                  String order = element.getAttribute(ATTRIB_ORDER);
                  String type = StringUtility.trim(element.getAttribute(ATTRIB_TYPE));

                  if (StringUtility.hasText(order) && StringUtility.hasText(type)) {
                    Integer o = parseOrder(order);
                    if (o != null) {
                      typeDefOrdered.put(o, new String[]{symbolicName, type});

                      // search for referenced types
                      for (IConfigurationElement ref : element.getChildren(TAG_NAME_REF)) {
                        String refType = ref.getAttribute(ATTRIB_REF_TYPE);
                        if (StringUtility.hasText(refType)) {
                          Set<String> list = refBundles.get(type);
                          if (list == null) {
                            list = new HashSet<String>(2);
                            refBundles.put(type, list);
                          }
                          list.add(refType);
                        }
                      }
                    }
                  }
                }
                else {
                  ScoutSdk.logWarning("No symbolic name defined for extension '" + element.getNamespaceIdentifier() + "'.");
                }
              }
            }
          }

          LinkedHashMap<String, String> tmpBundleToTypeMap = new LinkedHashMap<String, String>(typeDefOrdered.size());
          HashMap<String, String> tmpTypeToBundleMap = new HashMap<String, String>(typeDefOrdered.size());
          for (String[] items : typeDefOrdered.values()) {
            tmpBundleToTypeMap.put(items[0], items[1]);
            tmpTypeToBundleMap.put(items[1], items[0]);
          }

          typeToBundleMap = tmpTypeToBundleMap;
          bundleToTypeMap = tmpBundleToTypeMap;
          allScoutRtBundles = CollectionUtility.hashSet(all);
          referencedBundles = CollectionUtility.copyMap(refBundles);
        }
      }
    }
  }

  public static Integer parseOrder(String order) {
    try {
      return Integer.parseInt(order);
    }
    catch (NumberFormatException e) {
      ScoutSdk.logWarning("Unable to process RuntimeBundle extension. The order must be a valid integer.", e);
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
  public static String getBundleType(IScoutBundle bundle) {
    if (bundle == null) {
      return null;
    }
    Set<String> symbolicNames = bundle.getAllDependencies().keySet();
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
  public static String getBundleType(Collection<String> symbolicNames) {
    if (symbolicNames == null || symbolicNames.isEmpty()) {
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
   * Gets all referenced types of the given start type.
   *
   * @param startType
   *          The bundle start type.
   * @return The referenced types. The startType itself is not part of the resulting {@link Set}.
   */
  public static Set<String> getReferencedTypes(String startType) {
    return CollectionUtility.hashSet(referencedBundles.get(startType));
  }

  /**
   * Checks if the given search-bundle-type is in the referenced types list of the given bundle-start-type.
   *
   * @param startType
   *          The bundle-type for which the referenced types should be searched
   * @param typeToSearch
   *          The referenced type that is searched.
   * @return <code>true</code> if typeToSearch is in the referenced types list of startType, <code>false</code>
   *         otherwise.
   * @see #getReferencedTypes(String)
   */
  public static boolean hasReferencedType(String startType, String typeToSearch) {
    ensureCached();
    Set<String> set = referencedBundles.get(startType);
    return set != null && set.contains(typeToSearch);
  }

  /**
   * @return Gets all bundle types registered.
   */
  public static List<String> getTypes() {
    ensureCached();
    return CollectionUtility.arrayList(bundleToTypeMap.values());
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
