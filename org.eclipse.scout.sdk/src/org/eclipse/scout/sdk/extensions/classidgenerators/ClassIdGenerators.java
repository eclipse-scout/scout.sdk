/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.extensions.classidgenerators;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.sdk.internal.ScoutSdk;

/**
 * <h3>{@link ClassIdGenerators}</h3> Provides a single access point to get class id values.
 * 
 * @author Matthias Villiger
 * @since 3.10.0 02.01.2014
 * @see IClassIdGenerator
 */
public final class ClassIdGenerators {

  private final static String EXTENSION_POINT_NAME = "classIdGenerator";
  private final static String TAG_NAME = "generator";
  private final static String ATTRIB_CLASS = "class";
  private final static String ATTRIB_PRIO = "priority";

  private final static Object LOCK = new Object();
  private static volatile Collection<IClassIdGenerator> allGeneratorsOrdered = null;

  private static boolean automaticallyCreateClassIdAnnotation = false;
  public final static String PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION = ScoutSdk.PLUGIN_ID + ".propAutomaticallyCreateClassIdAnnotation";

  private ClassIdGenerators() {
  }

  private static Collection<IClassIdGenerator> getGeneratorsOrdered() {
    if (allGeneratorsOrdered == null) {
      synchronized (LOCK) {
        if (allGeneratorsOrdered == null) {
          Map<CompositeObject, IClassIdGenerator> tmp = new TreeMap<CompositeObject, IClassIdGenerator>();

          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdk.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              if (TAG_NAME.equals(element.getName())) {
                try {
                  IClassIdGenerator generator = (IClassIdGenerator) element.createExecutableExtension(ATTRIB_CLASS);

                  String prio = element.getAttribute(ATTRIB_PRIO);
                  Double priority = null;
                  if (StringUtility.hasText(prio)) {
                    priority = parseDouble(prio);
                  }
                  else {
                    ScoutSdk.logWarning("No priority found for extension '" + element.getNamespaceIdentifier() + "'. Using 0.0");
                    priority = Double.valueOf(0.0);
                  }

                  tmp.put(new CompositeObject(-priority, generator.getClass().getName(), generator), generator);
                }
                catch (Exception e) {
                  ScoutSdk.logWarning("Could not load classIdGenerator extension '" + element.getNamespaceIdentifier() + "'.", e);
                }
              }
            }
          }
          allGeneratorsOrdered = tmp.values();
        }
      }
    }
    return allGeneratorsOrdered;
  }

  private static Double parseDouble(String order) {
    try {
      return Double.valueOf(Double.parseDouble(order));
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * @return true if the {@link ClassId} annotation should be generated automatically, false otherwise.
   */
  public static boolean isAutomaticallyCreateClassIdAnnotation() {
    synchronized (LOCK) {
      return automaticallyCreateClassIdAnnotation;
    }
  }

  /**
   * Sets if the {@link ClassId} annotation should automatically be created.
   * 
   * @param newValue
   *          true if it should be created automatically, false otherwise.
   */
  public static void setAutomaticallyCreateClassIdAnnotation(boolean newValue) {
    synchronized (LOCK) {
      automaticallyCreateClassIdAnnotation = newValue;
    }
  }

  /**
   * Gets a new class id for the given type. All class id generators are considered until the first (according to the
   * priorities) provides a value.
   * 
   * @param type
   *          The type for which the new id should be generated.
   * @return The new id or null if no generator provided an id.
   */
  public static String generateNewId(ClassIdGenerationContext context) {
    for (IClassIdGenerator gen : getGeneratorsOrdered()) {
      String newId = gen.generate(context);
      if (newId != null) {
        return newId;
      }
    }
    return null;
  }
}
