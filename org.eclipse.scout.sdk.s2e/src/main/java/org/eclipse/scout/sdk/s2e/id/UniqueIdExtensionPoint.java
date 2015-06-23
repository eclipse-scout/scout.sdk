/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.id;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;

public final class UniqueIdExtensionPoint {

  private static final String EXTENSION_POINT_NAME = "uniqueId";
  private static final String CODE_ID_PROVIDER_EXT_NAME = "uniqueIdProvider";
  private static final String ATTRIB_CLASS = "class";
  private static final String ATTRIB_PRIO = "priority";

  private static final Object CODE_ID_PROV_LOCK = new Object();
  private static volatile List<ICodeIdProvider> codeIdProviderExtensions;

  private UniqueIdExtensionPoint() {
  }

  /**
   * @return all extensions in the prioritized order.
   */
  private static List<ICodeIdProvider> getCodeIdProviderExtensions() {
    if (codeIdProviderExtensions == null) {
      synchronized (CODE_ID_PROV_LOCK) {
        if (codeIdProviderExtensions == null) {
          Map<CompositeObject, ICodeIdProvider> providers = new TreeMap<>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(S2ESdkActivator.PLUGIN_ID, EXTENSION_POINT_NAME);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] providerElememts = extension.getConfigurationElements();
            for (IConfigurationElement providerElememt : providerElememts) {
              if (CODE_ID_PROVIDER_EXT_NAME.equals(providerElememt.getName())) {
                try {
                  ICodeIdProvider provider = (ICodeIdProvider) providerElememt.createExecutableExtension(ATTRIB_CLASS);
                  providers.put(new CompositeObject(getPriority(providerElememt), provider.getClass().getName()), provider);
                }
                catch (Exception t) {
                  S2ESdkActivator.logError("Error registering code id provider '" + providerElememt.getNamespaceIdentifier() + "'.", t);
                }
              }
            }
          }
          codeIdProviderExtensions = new ArrayList<>(providers.values());
        }
      }
    }
    return codeIdProviderExtensions;
  }

  private static int getPriority(IConfigurationElement element) {
    int priority = 0;
    try {
      String prio = element.getAttribute(ATTRIB_PRIO);
      priority = Integer.MAX_VALUE - Integer.parseInt(prio); /* descending order: highest prio first */
    }
    catch (Exception e) {
      S2ESdkActivator.logWarning("could not parse priority of " + EXTENSION_POINT_NAME + " extension '" + element.getName() + "'", e);
    }
    return priority;
  }

  public static String getNextUniqueId(PropertyMap context, String genericSignature) {
    for (ICodeIdProvider p : getCodeIdProviderExtensions()) {
      try {
        String value = p.getNextId(context, genericSignature);
        if (value != null) {
          return value;
        }
      }
      catch (Exception e) {
        S2ESdkActivator.logWarning("Exception in codeIdExtension '" + p.getClass().getName() + "'.", e);
      }
    }
    return null;
  }
}
