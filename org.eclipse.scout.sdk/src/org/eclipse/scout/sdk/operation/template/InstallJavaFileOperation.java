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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.osgi.framework.Bundle;

/**
 * Install java file from template
 */
public class InstallJavaFileOperation extends InstallTextFileOperation {

  public InstallJavaFileOperation(String srcPath, String rootPackageRelativeDestPath, IScoutBundle scoutBundle) {
    this(srcPath, rootPackageRelativeDestPath, scoutBundle, createProperties(scoutBundle));
  }

  private static Map<String, String> createProperties(IScoutBundle scoutBundle) {
    HashMap<String, String> ret = new HashMap<String, String>();
    ret.put(IScoutProjectNewOperation.PROP_PROJECT_NAME, scoutBundle.getScoutProject().getProjectName());
    return ret;
  }

  public InstallJavaFileOperation(String srcPath, String rootPackageRelativeDestPath, IScoutBundle scoutBundle, Map<String, String> properties) {
    this(srcPath, "src/" + (scoutBundle.getRootPackageName().replace('.', '/')) + "/" + rootPackageRelativeDestPath, scoutBundle.getProject(), properties);
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
    // export package
    IJavaProject javaProject = JavaCore.create(m_dstProject);
    String pckName = "/" + m_dstProject.getName() + "/" + m_dstPath;
    int i = pckName.lastIndexOf("/");
    if (i >= 0) {
      pckName = pckName.substring(0, i);
    }
    IPackageFragment pck = javaProject.findPackageFragment(new Path(pckName));
    new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{pck}, true).run(monitor, workingCopyManager);
  }

}
