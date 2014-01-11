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
package org.eclipse.scout.nls.sdk.internal.jdt;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.internal.NlsCore;

public final class NlsJdtUtility {

  private NlsJdtUtility() {
  }

  public static List<IClasspathEntry> getSourceLocations(IJavaProject project) throws JavaModelException {
    List<IClasspathEntry> sourceLocations = new LinkedList<IClasspathEntry>();
    IClasspathEntry[] clEntries = project.getRawClasspath();
    for (IClasspathEntry entry : clEntries) {
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        sourceLocations.add(entry);
      }
    }
    return sourceLocations;
  }

  public static boolean createFolder(IContainer folder, boolean recursively, IProgressMonitor monitor) throws CoreException {
    if (!folder.exists()) {
      createFolder(folder.getParent(), recursively, monitor);
      if (folder instanceof IFolder) {
        ((IFolder) folder).create(true, false, monitor);
      }
    }
    return true;
  }

  public static IType getITypeForFile(IFile file) {
    try {
      IProject project = file.getProject();
      if (project.hasNature(JavaCore.NATURE_ID)) {
        IJavaProject jp = JavaCore.create(project);
        List<IClasspathEntry> sourceLocs = getSourceLocations(jp);
        String filePath = file.getProjectRelativePath().toString();
        for (IClasspathEntry entry : sourceLocs) {
          String pref = entry.getPath().lastSegment();
          if (filePath.startsWith(pref)) {
            filePath = filePath.substring(pref.length() + 1);
            continue;
          }
        }
        String javaFileSuffix = ".java";
        filePath = filePath.replace("/", ".");
        if (filePath.toLowerCase().endsWith(javaFileSuffix)) {
          filePath = filePath.substring(0, filePath.length() - javaFileSuffix.length());
        }
        IType type = jp.findType(filePath);

        if (type == null) {
          NlsCore.logWarning("could not find an IType for: " + file.getName());
        }
        return type;
      }
    }
    catch (Exception e) {
      NlsCore.logWarning(e);
    }
    return null;
  }

  public static List<IPackageFragment> getPluginPackages(IProject project) {
    IJavaProject jp = JavaCore.create(project);
    return getPluginPackages(jp);
  }

  /**
   * Returns all packages in any of the project's source folders.
   *
   * @param jProject
   * @return
   */
  public static List<IPackageFragment> getPluginPackages(IJavaProject jProject) {
    List<IPackageFragment> proposals = new LinkedList<IPackageFragment>();
    try {
      for (IClasspathEntry entry : jProject.getRawClasspath()) {
        if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          for (IPackageFragmentRoot fragRoot : jProject.findPackageFragmentRoots(entry)) {
            for (IJavaElement ele : fragRoot.getChildren()) {
              if (ele instanceof IPackageFragment) {
                proposals.add((IPackageFragment) ele);
              }
            }
          }
        }
      }
      return proposals;
    }
    catch (JavaModelException e) {
      NlsCore.logWarning(e);
      return new LinkedList<IPackageFragment>();
    }
  }
}
