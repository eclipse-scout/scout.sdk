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

import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;

/**
 *
 */
public class ScoutBundleExtensionPoint {

  private static String extensionId = "scoutBundle";
  private static String attributeBundleId = "id";
  private static String attributeBundleName = "bundleName";
  private static String attributeBundleType = "bundleType";
  private static String attributeOrderNumber = "orderNumber";
  private static String attributeClass = "class";
  private static String attributeIcon = "icon";

  private static final ScoutBundleExtensionPoint instance = new ScoutBundleExtensionPoint();

  private TreeSet<ScoutBundleExtension> m_extensions;

  private ScoutBundleExtensionPoint() {
    m_extensions = new TreeSet<ScoutBundleExtension>(new P_ExtensionComparator());
    init();
  }

  private void init() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, extensionId);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        ScoutBundleExtension extensionPoint = new ScoutBundleExtension();
        extensionPoint.setBundleID(element.getAttribute(attributeBundleId));
        extensionPoint.setBundleName(element.getAttribute(attributeBundleName));
        try {
          extensionPoint.setBundleExtension((IScoutBundleProvider) element.createExecutableExtension(attributeClass));
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("could not load class '" + element.getAttribute(attributeClass) + "'.", e);
        }
        extensionPoint.setIconPath(element.getAttribute(attributeIcon));
        String attOrderNr = element.getAttribute(attributeOrderNumber);
        try {
          extensionPoint.setOrderNumber(new Integer(attOrderNr).intValue());
        }
        catch (NumberFormatException e) {
          ScoutSdkUi.logError("could not parse order number '" + attOrderNr + "' of extension '" + element.getNamespaceIdentifier() + "'.", e);
        }
        String bundleTypeString = element.getAttribute(attributeBundleType);
        BundleTypes bundleType = BundleTypes.valueOf(bundleTypeString);
        extensionPoint.setBundleType(bundleType);
        if (extensionPoint.isValidConfiguration()) {
          m_extensions.add(extensionPoint);
        }
      }
    }
  }

  public static final ScoutBundleExtension[] getExtensions() {
    return instance.getExtensionsImpl();
  }

  private final ScoutBundleExtension[] getExtensionsImpl() {
    return m_extensions.toArray(new ScoutBundleExtension[m_extensions.size()]);
  }

  private class P_ExtensionComparator implements Comparator<ScoutBundleExtension> {
    @Override
    public int compare(ScoutBundleExtension o1, ScoutBundleExtension o2) {

      if (o1.getOrderNumber() != o2.getOrderNumber()) {
        return o2.getOrderNumber() - o1.getOrderNumber();
      }
      return o1.getBundleName().compareTo(o2.getBundleName());
    }
  }
}
