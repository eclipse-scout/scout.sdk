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
package org.eclipse.scout.sdk.ui.extensions.bundle;

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
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 *
 */
public final class ScoutBundleExtensionPoint {

  public static final String EXTENSION_ID = "scoutBundle";
  public static final String ATTRIBUTE_BUNDLE_ID = "id";
  public static final String ATTRIBUTE_BUNDLE_NAME = "bundleName";
  public static final String ATTRIBUTE_BUNDLE_TYPE = "bundleType";
  public static final String ATTRIBUTE_ORDER_NUMBER = "orderNumber";
  public static final String ATTRIBUTE_CLASS = "bundleNewHandler";
  public static final String ATTRIBUTE_PRODUCT_LAUNCHER_CONTRIB = "productLauncherContributor";
  public static final String ATTRIBUTE_BUNDLE_PAGE = "bundlePage";
  public static final String ATTRIBUTE_ICON = "icon";
  public static final String ATTRIBUTE_LAUNCHER_ICON = "launcherIcon";

  private static final Object LOCK = new Object();

  private static volatile Map<String /* bundle type */, ScoutBundleUiExtension> allExtensions = null;

  private ScoutBundleExtensionPoint() {
  }

  private static Map<String, ScoutBundleUiExtension> getAllExtensions() {
    if (allExtensions == null) {
      synchronized (LOCK) {
        if (allExtensions == null) {
          Map<String, ScoutBundleUiExtension> tmp = new HashMap<String, ScoutBundleUiExtension>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, EXTENSION_ID);
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              ScoutBundleUiExtension extensionPoint = new ScoutBundleUiExtension();
              extensionPoint.setBundleId(element.getAttribute(ATTRIBUTE_BUNDLE_ID));
              extensionPoint.setBundleName(element.getAttribute(ATTRIBUTE_BUNDLE_NAME));
              try {
                String contribPlugin = element.getContributor().getName();
                extensionPoint.setNewScoutBundleHandler((INewScoutBundleHandler) element.createExecutableExtension(ATTRIBUTE_CLASS));

                String productLauncherContributor = element.getAttribute(ATTRIBUTE_PRODUCT_LAUNCHER_CONTRIB);
                if (StringUtility.hasText(productLauncherContributor)) {
                  extensionPoint.setProductLauncherContributor((IProductLauncherContributor) element.createExecutableExtension(ATTRIBUTE_PRODUCT_LAUNCHER_CONTRIB));
                }

                String bundlePage = StringUtility.trim(element.getAttribute(ATTRIBUTE_BUNDLE_PAGE));
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

                String icon = element.getAttribute(ATTRIBUTE_ICON);
                if (StringUtility.hasText(icon)) {
                  extensionPoint.setIcon(AbstractUIPlugin.imageDescriptorFromPlugin(contribPlugin, icon));
                }

                String launcherIcon = element.getAttribute(ATTRIBUTE_LAUNCHER_ICON);
                if (StringUtility.hasText(launcherIcon)) {
                  extensionPoint.setLauncherIconPath(AbstractUIPlugin.imageDescriptorFromPlugin(contribPlugin, launcherIcon));
                }
                String attOrderNr = element.getAttribute(ATTRIBUTE_ORDER_NUMBER);
                try {
                  extensionPoint.setOrderNumber(Integer.parseInt(attOrderNr));
                }
                catch (NumberFormatException e) {
                  ScoutSdkUi.logError("could not parse order number '" + attOrderNr + "' of extension '" + element.getNamespaceIdentifier() + "'.", e);
                }
                extensionPoint.setBundleType(element.getAttribute(ATTRIBUTE_BUNDLE_TYPE));
                if (extensionPoint.isValidConfiguration()) {
                  tmp.put(extensionPoint.getBundleType(), extensionPoint);
                }
              }
              catch (CoreException e) {
                ScoutSdkUi.logError("could not load class '" + element.getAttribute(ATTRIBUTE_CLASS) + "'.", e);
              }
            }
          }
          allExtensions = tmp;
        }
      }
    }
    return allExtensions;
  }

  public static ScoutBundleUiExtension[] getExtensions() {
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

  public static ScoutBundleUiExtension getExtension(String type) {
    return getAllExtensions().get(type);
  }
}
