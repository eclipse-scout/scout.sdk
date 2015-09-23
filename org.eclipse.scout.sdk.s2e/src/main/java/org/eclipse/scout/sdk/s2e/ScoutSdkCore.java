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
package org.eclipse.scout.sdk.s2e;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.internal.JavaEnvironmentWithJdt;
import org.eclipse.scout.sdk.core.model.spi.internal.WorkspaceFileSystem;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.internal.WorkingCopyManager;
import org.eclipse.scout.sdk.s2e.trigger.ITypeChangedManager;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;

/**
 * Main class to access Scout2Eclipse core components.
 */
public final class ScoutSdkCore {
  private ScoutSdkCore() {
  }

  /**
   * Gets the {@link ITypeChangedManager} responsible for automatically update Scout DTOs in the Eclipse IDE.
   *
   * @return
   */
  public static ITypeChangedManager getTypeChangedManager() {
    return S2ESdkActivator.getDefault().getTypeChangedManager();
  }

  /**
   * @return A new created {@link IWorkingCopyManager}.
   */
  public static IWorkingCopyManager createWorkingCopyManager() {
    return new WorkingCopyManager();
  }

  /**
   * Creates an {@link IJavaEnvironment} based on an Eclipse {@link IJavaProject} and its classpath.
   *
   * @param javaProject
   *          the {@link IJavaProject} used to create the {@link IJavaEnvironment} for.
   * @param allowErrors
   *          <code>true</code> if the resulting environment should be lenient with compile errors. If
   *          <code>false</code> the lookup environment will throw exceptions as soon as compile errors are found.
   * @return The new created {@link IJavaEnvironment}.
   * @throws CoreException
   */
  public static IJavaEnvironment createJavaEnvironment(IJavaProject javaProject) throws CoreException {
    Validate.notNull(javaProject);
    return new JavaEnvironmentWithJdt(createClasspaths(javaProject)).wrap();
  }

  private static FileSystem.Classpath[] createClasspaths(IJavaProject... projects) throws JavaModelException {
    LinkedHashSet<Classpath> paths = new LinkedHashSet<>();
    for (IJavaProject project : projects) {
      appendPaths(paths, project);
    }
    return paths.toArray(new Classpath[0]);
  }

  private static void appendPaths(Collection<Classpath> paths, IJavaProject javaProject) throws JavaModelException {
    IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
    for (IPackageFragmentRoot cpRoot : allPackageFragmentRoots) {
      if (cpRoot.getSourceAttachmentPath() != null) {
        appendPath(paths, cpRoot.getSourceAttachmentPath().toFile(), true);
      }
      if (cpRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
        appendPath(paths, cpRoot.getResource().getLocation().toFile(), true);
      }
      else {
        appendPath(paths, cpRoot.getPath().toFile(), true);//add also as source in case the .java files are in the same jar
        appendPath(paths, cpRoot.getPath().toFile(), false);//and binary
      }
    }
  }

  private static void appendPath(Collection<Classpath> paths, File f, boolean source) {
    Classpath classpath = WorkspaceFileSystem.createClasspath(f, source);
    if (classpath != null) {
      paths.add(classpath);
    }
  }

}
