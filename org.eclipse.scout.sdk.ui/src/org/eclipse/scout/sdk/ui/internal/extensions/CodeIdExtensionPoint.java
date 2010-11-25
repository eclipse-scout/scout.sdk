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

import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.ICodeIdProvider;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public final class CodeIdExtensionPoint {

  private static Object bsiCaseExtensionsCacheLock = new Object();
  private static Object codeIdProviderExtensionsCacheLock = new Object();
  private static ICodeIdProvider[] codeIdProviderExtensions;

  private CodeIdExtensionPoint() {
  }

  /**
   * @return all extensions in the priorized order.
   */
  public static ICodeIdProvider[] getCodeIdProviderExtensions() {
    synchronized (codeIdProviderExtensionsCacheLock) {
      // if(codeIdProviderExtensions==null){
      TreeMap<CompositeObject, ICodeIdProvider> providers = new TreeMap<CompositeObject, ICodeIdProvider>();
      IExtensionRegistry reg = Platform.getExtensionRegistry();
      IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, "scoutCodeIdProvider");
      IExtension[] extensions = xp.getExtensions();
      for (IExtension extension : extensions) {
        IConfigurationElement[] serviceElememts = extension.getConfigurationElements();

        for (int i = 0; i < serviceElememts.length; i++) {
          IConfigurationElement serviceElememt = serviceElememts[i];
          String className = serviceElememt.getAttribute("class");
          int priority = 0;
          try {
            String prio = serviceElememt.getAttribute("priority");
            priority = Integer.parseInt(prio);
          }
          catch (Exception e) {
            ScoutSdkUi.logWarning("could not parse priority of CodeIdProvider extension '" + serviceElememt.getName() + "'", e);
          }
          ScoutSdkUi.logInfo("found service: " + className);
          try {
            ICodeIdProvider service = (ICodeIdProvider) serviceElememt.createExecutableExtension("class");
            providers.put(new CompositeObject(priority, i, service), service);
          }
          catch (Throwable t) {
            ScoutSdkUi.logError("register service: " + className, t);
          }
        }
      }
      codeIdProviderExtensions = providers.values().toArray(new ICodeIdProvider[providers.size()]);
    }
    // }
    return codeIdProviderExtensions;
  }

  public static String getNextCodeId(IScoutProject projectGroup, String genericSignature) {
    String value = null;
    ICodeIdProvider[] providers = getCodeIdProviderExtensions();
    for (ICodeIdProvider p : providers) {
      try {
        value = p.getNextId(projectGroup, genericSignature);
        if (value != null) {
          break;
        }
      }
      catch (Exception e) {
        ScoutSdkUi.logWarning("Exception in codeIdExtension '" + p.getClass().getName() + "'", e);
      }
    }
    return value;
  }
}
