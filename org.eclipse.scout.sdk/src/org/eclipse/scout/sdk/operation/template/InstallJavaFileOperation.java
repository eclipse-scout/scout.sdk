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
package org.eclipse.scout.sdk.operation.template;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.osgi.framework.Bundle;

/**
 * Install java file from template
 */
public class InstallJavaFileOperation extends InstallTextFileOperation {
  public InstallJavaFileOperation(String srcPath, String rootPackageRelativeDestPath, IScoutBundle scoutBundle, Map<String, String> properties) {
    this(srcPath, TypeUtility.DEFAULT_SOURCE_FOLDER_NAME + "/" + scoutBundle.getSymbolicName().replace('.', '/') + "/" + rootPackageRelativeDestPath, scoutBundle.getProject(), properties);
  }

  public InstallJavaFileOperation(String srcPath, String destPath, IProject project, Map<String, String> properties) {
    this(srcPath, destPath, Platform.getBundle(ScoutSdk.PLUGIN_ID), project, properties);
  }

  public InstallJavaFileOperation(String srcPath, String destPath, Bundle sourceBundle, IProject project, Map<String, String> properties) {
    super(srcPath, destPath, sourceBundle, project, properties);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    // create java element from created resource
    IJavaElement e = JavaCore.create(getCreatedFile());
    if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
      ICompilationUnit cu = (ICompilationUnit) e;
      workingCopyManager.register(cu, monitor);
      IJavaElement pck = cu.getParent();
      if (pck.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
        // add package to exported packages
        IPackageFragment frag = (IPackageFragment) pck;
        ResourcesPlugin.getWorkspace().checkpoint(false);
        ManifestExportPackageOperation op = new ManifestExportPackageOperation(ExportPolicy.ADD_PACKAGE, new IPackageFragment[]{frag}, true);
        op.validate();
        op.run(monitor, workingCopyManager);
      }
    }
  }
}