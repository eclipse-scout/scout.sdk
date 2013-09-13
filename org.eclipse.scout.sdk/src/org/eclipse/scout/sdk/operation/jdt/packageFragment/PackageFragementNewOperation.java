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
package org.eclipse.scout.sdk.operation.jdt.packageFragment;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link PackageFragementNewOperation}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 06.02.2013
 */
public class PackageFragementNewOperation implements IOperation {

  private boolean m_exportPackage;
  private final IJavaProject m_javaProject;
  private final String m_packageName;
  private ExportPolicy m_exportPackagePolicy;
  private boolean m_noErrorWhenPackageAlreadyExist;

  private IPackageFragment m_createdPackageFragment;

  public PackageFragementNewOperation(String packageName, IJavaProject javaProject) {
    m_packageName = packageName;
    m_javaProject = javaProject;
  }

  @Override
  public String getOperationName() {
    return "Create package '" + getPackageName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!isNoErrorWhenPackageAlreadyExist()) {
      try {
        if (TypeUtility.exists(TypeUtility.getPackage(getJavaProject(), getPackageName()))) {
          throw new IllegalArgumentException("Package '" + getPackageName() + "' does already exist!");
        }
      }
      catch (JavaModelException ex) {
        throw new IllegalArgumentException("Could not resolve package '" + getPackageName() + "'!", ex);
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    boolean checkPoint = false;
    IPackageFragmentRoot root = TypeUtility.getSrcPackageFragmentRoot(getJavaProject());
    IPackageFragment packageFragment = root.getPackageFragment(getPackageName());
    if (TypeUtility.exists(packageFragment)) {
      setCreatedPackageFragment(packageFragment);
    }
    else {
      setCreatedPackageFragment(root.createPackageFragment(getPackageName(), true, monitor));
      checkPoint = true;
    }
    if (getExportPackagePolicy() != null) {
      ManifestExportPackageOperation exportOp = new ManifestExportPackageOperation(getExportPackagePolicy(), new IPackageFragment[]{m_createdPackageFragment}, false);
      exportOp.validate();
      exportOp.run(monitor, workingCopyManager);
    }
    if (checkPoint) {
      ResourcesPlugin.getWorkspace().checkpoint(false);
    }

  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public void setExportPackagePolicy(ExportPolicy exportPackagePolicy) {
    m_exportPackagePolicy = exportPackagePolicy;
  }

  public ExportPolicy getExportPackagePolicy() {
    return m_exportPackagePolicy;
  }

  public void setNoErrorWhenPackageAlreadyExist(boolean noErrorWhenPackageAlreadyExist) {
    m_noErrorWhenPackageAlreadyExist = noErrorWhenPackageAlreadyExist;
  }

  public boolean isNoErrorWhenPackageAlreadyExist() {
    return m_noErrorWhenPackageAlreadyExist;
  }

  protected void setCreatedPackageFragment(IPackageFragment createdPackageFragment) {
    m_createdPackageFragment = createdPackageFragment;
  }

  public IPackageFragment getCreatedPackageFragment() {
    return m_createdPackageFragment;
  }

}
