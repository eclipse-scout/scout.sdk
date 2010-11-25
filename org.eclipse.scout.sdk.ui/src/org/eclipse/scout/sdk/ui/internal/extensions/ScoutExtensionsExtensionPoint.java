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
package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.ICodeIdProvider;
import org.eclipse.scout.sdk.ui.extensions.IScoutSdkExtension;

public final class ScoutExtensionsExtensionPoint {

  private static Object bsiCaseExtensionsCacheLock = new Object();
  private static IScoutSdkExtension[] bsiCaseExtensions;
  private static Object codeIdProviderExtensionsCacheLock = new Object();
  private static ICodeIdProvider[] codeIdProviderExtensions;

  private ScoutExtensionsExtensionPoint() {
  }

  public static IScoutSdkExtension[] getExtensions() {
    if (bsiCaseExtensions == null) {
      synchronized (bsiCaseExtensionsCacheLock) {
        if (bsiCaseExtensions == null) {
          ArrayList<IScoutSdkExtension> list = new ArrayList<IScoutSdkExtension>();
          IExtensionRegistry reg = Platform.getExtensionRegistry();
          IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, "scoutIdeExtensions");
          IExtension[] extensions = xp.getExtensions();
          for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            for (IConfigurationElement element : elements) {
              try {
                IScoutSdkExtension ext = (IScoutSdkExtension) element.createExecutableExtension("class");
                if (ext != null) {
                  list.add(ext);
                }
              }
              catch (Throwable t) {
                ScoutSdkUi.logError("create bsiCaseExtension: " + element.getAttribute("class"), t);
              }
            }
          }
          bsiCaseExtensions = list.toArray(new IScoutSdkExtension[list.size()]);
          for (IScoutSdkExtension ext : bsiCaseExtensions) {
            ScoutSdkUi.logInfo("Found contribution: " + ext.getClass().getName());
          }
        }
      }
    }
    return bsiCaseExtensions;
  }

}
