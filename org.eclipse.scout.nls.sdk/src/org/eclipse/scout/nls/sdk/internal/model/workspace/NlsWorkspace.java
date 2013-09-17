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
package org.eclipse.scout.nls.sdk.internal.model.workspace;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.nls.sdk.extension.INlsProjectProvider;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.INlsWorkspace;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * <h4>NlsWorkspace</h4> access this class by {@link NlsCore#getNlsWorkspace()}.
 */
public final class NlsWorkspace implements INlsWorkspace {

  public NlsWorkspace() {
  }

  @Override
  public INlsProject getNlsProject(final Object[] args) throws CoreException {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(NlsCore.PLUGIN_ID, "nlsProvider");
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if ("provider".equals(element.getName())) {
          INlsProjectProvider p = (INlsProjectProvider) element.createExecutableExtension("class");
          if (p != null) {
            INlsProject proj = p.getProject(args);
            if (proj != null) {
              return proj;
            }
          }
        }
      }
    }
    return null;
  }
}
