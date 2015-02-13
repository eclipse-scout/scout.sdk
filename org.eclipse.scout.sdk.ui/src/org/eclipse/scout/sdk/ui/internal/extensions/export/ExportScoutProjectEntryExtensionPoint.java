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
package org.eclipse.scout.sdk.ui.internal.extensions.export;

import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.export.IExportScoutProjectEntryHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

public final class ExportScoutProjectEntryExtensionPoint {

  public static final String EXTENSION_ID = "exportScoutProjectEntry";
  public static final String ATTR_ICON = "icon";
  public static final String ATTR_ID = "id";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_ORDER = "order";
  public static final String ATTR_CLASS = "class";

  private ExportScoutProjectEntryExtensionPoint() {
  }

  public static ExportScoutProjectEntry[] getEntries() {
    TreeSet<ExportScoutProjectEntry> ret = new TreeSet<>();
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
          IExportScoutProjectEntryHandler provider = (IExportScoutProjectEntryHandler) element.createExecutableExtension(ATTR_CLASS);
          ret.add(new ExportScoutProjectEntry(id, name, order, icon, provider));
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("could not load class '" + element.getAttribute(ATTR_CLASS) + "'.", e);
        }
      }
    }
    return ret.toArray(new ExportScoutProjectEntry[ret.size()]);
  }

  private static int getOrder(String order) {
    if (!StringUtility.hasText(order)) {
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
