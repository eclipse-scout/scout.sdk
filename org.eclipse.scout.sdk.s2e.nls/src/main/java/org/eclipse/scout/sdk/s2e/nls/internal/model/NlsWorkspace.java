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
package org.eclipse.scout.sdk.s2e.nls.internal.model;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.INlsProjectProvider;
import org.eclipse.scout.sdk.s2e.nls.model.INlsWorkspace;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

/**
 * <h4>NlsWorkspace</h4> access this class by {@link NlsCore#getNlsWorkspace()}.
 */
public final class NlsWorkspace implements INlsWorkspace {

  private volatile Collection<INlsProjectProvider> m_providers;

  private Collection<INlsProjectProvider> getProviders() {
    Collection<INlsProjectProvider> providers = m_providers;
    if (providers != null) {
      return providers;
    }

    synchronized (this) {
      providers = m_providers;
      if (providers != null) {
        return providers;
      }

      providers = new ArrayList<>(2);
      final IExtensionPoint xp = Platform.getExtensionRegistry().getExtensionPoint(NlsCore.PLUGIN_ID, "nlsProvider");
      for (final IExtension extension : xp.getExtensions()) {
        final IConfigurationElement[] elements = extension.getConfigurationElements();
        for (final IConfigurationElement element : elements) {
          if ("provider".equals(element.getName())) {
            try {
              final INlsProjectProvider p = (INlsProjectProvider) element.createExecutableExtension("class");
              if (p != null) {
                providers.add(p);
              }
            }
            catch (final CoreException e) {
              SdkLog.error("Unable to create extension '{}'.", element.getNamespaceIdentifier(), e);
            }
          }
        }
      }
      m_providers = providers;
      return providers;
    }
  }

  @Override
  public INlsProject getNlsProject(final Object... args) {
    for (final INlsProjectProvider p : getProviders()) {
      final INlsProject proj = p.getProject(args);
      if (proj != null) {
        return proj;
      }
    }
    return null;
  }
}
