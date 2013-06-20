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
package org.eclipse.scout.nls.sdk.simple.model.ws.project;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.nls.sdk.extension.INlsProjectProvider;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsTypeUtility;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.simple.model.ws.NlsType;
import org.eclipse.scout.nls.sdk.simple.model.ws.nlsfile.AbstractNlsFile;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class SimpleNlsProjectProvider implements INlsProjectProvider {

  private final HashMap<String, INlsProject> m_projects = new HashMap<String, INlsProject>();

  public SimpleNlsProjectProvider() {
  }

  protected INlsProject getNlsProject(IType type) throws CoreException {
    if (!TypeUtility.exists(type)) {
      NlsCore.logError("nls type '" + type.getFullyQualifiedName() + "' does not exist");
      return null;
    }

    INlsProject nlsProject = m_projects.get(type.getFullyQualifiedName());
    if (nlsProject == null) {
      NlsType t = new NlsType(type);
      if (t != null && t.getTranslationsFolderName() != null) {
        nlsProject = new SimpleNlsProject(t);
        m_projects.put(type.getFullyQualifiedName(), nlsProject);
      }
    }
    return nlsProject;
  }

  @Override
  public INlsProject getProject(Object[] args) {
    // this provider can handle:
    // - IType: the Texts class (containing the RESOURCE_BUNDLE_NAME)
    // - IFile: the .nls file (properties file containing the Nls-Class property pointing to another type)
    if (args != null && args.length == 1) {
      if (args[0] instanceof IType) {
        IType t = (IType) args[0];
        try {
          return getNlsProject(t);
        }
        catch (CoreException e) {
          NlsCore.logWarning("Could not load NlsType: " + t.getFullyQualifiedName(), e);
        }
      }
      else if (args[0] instanceof IFile) {
        IFile f = (IFile) args[0];
        if (f.getName().toLowerCase().endsWith(".nls")) {
          try {
            AbstractNlsFile nlsFile = AbstractNlsFile.loadNlsFile(f);
            if (PluginRegistry.findModel(nlsFile.getProject()) != null && nlsFile.getNlsTypeName() != null) {
              IType type = NlsTypeUtility.getType(nlsFile.getNlsTypeName());
              if (TypeUtility.exists(type)) {
                return getNlsProject(type);
              }
            }
          }
          catch (CoreException e) {
            NlsCore.logWarning("Could not load NlsFile: " + f.getFullPath().toString(), e);
          }
        }
      }
    }
    return null;
  }
}
