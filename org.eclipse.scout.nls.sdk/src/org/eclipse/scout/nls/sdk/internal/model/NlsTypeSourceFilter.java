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
package org.eclipse.scout.nls.sdk.internal.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtHandler;

public class NlsTypeSourceFilter implements IJavaSearchScope {

  private IProject[] m_projects;

  public NlsTypeSourceFilter(IProject[] projects) {
    m_projects = projects;
  }

  public boolean encloses(String resourcePath) {
    for (IProject project : m_projects) {
      if (resourcePath != null && resourcePath.contains(project.getName())) {
        resourcePath = resourcePath.replace("/" + project.getName(), "");
        IFile file = project.getFile(new Path(resourcePath));
        if (file != null && file.exists()) {
          IType type = NlsJdtHandler.getITypeForFile(file);
          try {
            return NlsJdtHandler.isDescendant(type, NLS.class);
          }
          catch (JavaModelException e) {
            // TODO Auto-generated catch block
            NlsCore.logWarning(e);
          }
        }
      }
    }
    return false;
  }

  public boolean encloses(IJavaElement element) {
    if (element.getResource() instanceof IFile) {
      IType type = NlsJdtHandler.getITypeForFile((IFile) element.getResource());
      try {
        return NlsJdtHandler.isDescendant(type, NLS.class);
      }
      catch (JavaModelException e) {
        // TODO Auto-generated catch block
        NlsCore.logWarning(e);
        return false;
      }
    }
    return false;
  }

  public IPath[] enclosingProjectsAndJars() {
    IPath[] paths = new IPath[m_projects.length];
    for (int i = 0; i < m_projects.length; i++) {
      paths[i] = m_projects[i].getFullPath();
    }
    return paths;
  }

  @Deprecated
  public boolean includesBinaries() {
    // TODO Auto-generated method stub
    return false;
  }

  @Deprecated
  public boolean includesClasspaths() {
    // TODO Auto-generated method stub
    return false;
  }

  @Deprecated
  public void setIncludesBinaries(boolean includesBinaries) {
    // TODO Auto-generated method stub

  }

  @Deprecated
  public void setIncludesClasspaths(boolean includesClasspaths) {
    // TODO Auto-generated method stub

  }

}
