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
package org.eclipse.scout.sdk.operation.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class PackageNewOperation implements IOperation {
  private final IJavaProject m_project;
  private String m_srcPath;
  private String m_packageName;
  // result
  private IPackageFragment m_packageFragment;

  public PackageNewOperation(IJavaProject project, String srcPath, String packageName) {
    m_project = project;
    m_srcPath = srcPath;
    m_packageName = packageName;
  }

  public String getOperationName() {
    return "Create package " + m_packageName;
  }

  public IPackageFragment getCreatedPackageFragment() {
    return m_packageFragment;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("project can not be null");
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    IPackageFragmentRoot root = getProject().findPackageFragmentRoot(new Path("/" + getProject().getElementName() + "/" + m_srcPath));
    m_packageFragment = root.getPackageFragment(m_packageName);
    if (m_packageFragment == null || !m_packageFragment.exists()) {
      m_packageFragment = root.createPackageFragment(m_packageName, true, monitor);
    }
  }

  public IJavaProject getProject() {
    return m_project;
  }

}
