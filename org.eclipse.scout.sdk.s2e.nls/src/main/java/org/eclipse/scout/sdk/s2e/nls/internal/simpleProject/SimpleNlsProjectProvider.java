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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleProject;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.INlsProjectProvider;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

public class SimpleNlsProjectProvider implements INlsProjectProvider {

  private final Map<String, INlsProject> m_projects = new HashMap<>();
  private static final Object LOCK = new Object();

  public SimpleNlsProjectProvider() {
  }

  protected INlsProject getNlsProject(IType type) throws CoreException {
    if (type == null) {
      NlsCore.logError("NLS type cannot be null.");
      return null;
    }
    if (!type.exists()) {
      NlsCore.logError("NLS type '" + type.getFullyQualifiedName() + "' does not exist.");
      return null;
    }

    synchronized (LOCK) {
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
            if (nlsFile.getNlsTypeName() != null) {
              IJavaProject jp = JavaCore.create(nlsFile.getProject());
              if (jp != null) {
                IType type = jp.findType(nlsFile.getNlsTypeName());
                if (JdtUtils.exists(type)) {
                  INlsProject simpleProj = getNlsProject(type);
                  if (simpleProj != null) {
                    // fast pre-check: is it directly a simple project?
                    return simpleProj;
                  }
                  // also give the other providers a chance to parse
                  return NlsCore.getNlsWorkspace().getNlsProject(new Object[]{type});
                }
                else {
                  NlsCore.logWarning("Could not find type '" + nlsFile.getNlsTypeName() + "'.");
                }
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
