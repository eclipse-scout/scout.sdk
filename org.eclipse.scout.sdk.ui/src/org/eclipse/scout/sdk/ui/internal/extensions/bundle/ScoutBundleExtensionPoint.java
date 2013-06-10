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
package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.bundle.INewScoutBundleHandler;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 *
 */
public final class ScoutBundleExtensionPoint {

  private final static String extensionId = "scoutBundle";
  private final static String attributeBundleId = "id";
  private final static String attributeBundleName = "bundleName";
  private final static String attributeBundleType = "bundleType";
  private final static String attributeOrderNumber = "orderNumber";
  private final static String attributeClass = "bundleNewHandler";
  private final static String attributeBundlePage = "bundlePage";
  private final static String attributeIcon = "icon";
  private final static String attributeLauncherIcon = "launcherIcon";

  private final static Object lock = new Object();
  private static Map<String /* bundle type */, ScoutBundleUiExtension> allExtensions = null;

  private ScoutBundleExtensionPoint() {
  }

  private static Map<String, ScoutBundleUiExtension> getAllExtensions() {
    if (allExtensions == null) {
      synchronized (lock) {
        if (allExtensions == null) {
          Map<String, ScoutBundleUiExtension> tmp = new HashMap<String, ScoutBundleUiExtension>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, extensionId);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              ScoutBundleUiExtension extensionPoint = new ScoutBundleUiExtension();
              extensionPoint.setBundleID(element.getAttribute(attributeBundleId));
              extensionPoint.setBundleName(element.getAttribute(attributeBundleName));
              try {
                String contribPlugin = element.getContributor().getName();
                extensionPoint.setNewScoutBundleHandler((INewScoutBundleHandler) element.createExecutableExtension(attributeClass));

                String bundlePage = StringUtility.trim(element.getAttribute(attributeBundlePage));
                if (!StringUtility.isNullOrEmpty(bundlePage)) {
                  try {
                    @SuppressWarnings("unchecked")
                    Class<? extends IPage> loadClass = (Class<? extends IPage>) Platform.getBundle(contribPlugin).loadClass(bundlePage);
                    extensionPoint.setBundlePageClass(loadClass);
                  }
                  catch (Exception e) {
                    ScoutSdkUi.logError("Unable to load bundle page '" + bundlePage + "'.", e);
                  }
                }

                String icon = element.getAttribute(attributeIcon);
                if (StringUtility.hasText(icon)) {
                  extensionPoint.setIcon(AbstractUIPlugin.imageDescriptorFromPlugin(contribPlugin, icon));
                }

                String launcherIcon = element.getAttribute(attributeLauncherIcon);
                if (StringUtility.hasText(launcherIcon)) {
                  extensionPoint.setLauncherIconPath(AbstractUIPlugin.imageDescriptorFromPlugin(contribPlugin, launcherIcon));
                }
                String attOrderNr = element.getAttribute(attributeOrderNumber);
                try {
                  extensionPoint.setOrderNumber(Integer.valueOf(attOrderNr).intValue());
                }
                catch (NumberFormatException e) {
                  ScoutSdkUi.logError("could not parse order number '" + attOrderNr + "' of extension '" + element.getNamespaceIdentifier() + "'.", e);
                }
                extensionPoint.setBundleType(element.getAttribute(attributeBundleType));
                if (extensionPoint.isValidConfiguration()) {
                  tmp.put(extensionPoint.getBundleType(), extensionPoint);
                }
              }
              catch (CoreException e) {
                ScoutSdkUi.logError("could not load class '" + element.getAttribute(attributeClass) + "'.", e);
              }
            }
          }
          allExtensions = tmp;
        }
      }
    }
    return allExtensions;
  }

  public static final ScoutBundleUiExtension[] getExtensions() {
    Collection<ScoutBundleUiExtension> all = getAllExtensions().values();
    ScoutBundleUiExtension[] array = all.toArray(new ScoutBundleUiExtension[all.size()]);
    Arrays.sort(array, new Comparator<ScoutBundleUiExtension>() {
      @Override
      public int compare(ScoutBundleUiExtension o1, ScoutBundleUiExtension o2) {
        return o1.getOrderNumber() - o2.getOrderNumber();
      }
    });
    return array;
  }

  public static final ScoutBundleUiExtension getExtension(String type) {
    return getAllExtensions().get(type);
  }
}
