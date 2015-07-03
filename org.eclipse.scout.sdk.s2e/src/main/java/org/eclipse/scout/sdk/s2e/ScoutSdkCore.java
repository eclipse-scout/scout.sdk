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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.parser.JavaParser;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateManager;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.internal.WorkingCopyManager;

/**
 * Main class to access Scout2Eclipse core components.
 */
public final class ScoutSdkCore {
  private ScoutSdkCore() {
  }

  /**
   * Gets the {@link IDtoAutoUpdateManager} responsible for automatically update Scout DTOs in the Eclipse IDE.
   *
   * @return
   */
  public static IDtoAutoUpdateManager getDtoAutoUpdateManager() {
    return S2ESdkActivator.getDefault().getAutoUpdateManager();
  }

  /**
   * Creates an {@link ILookupEnvironment} based on an Eclipse {@link IJavaProject} and its classpath.
   *
   * @param javaProject
   *          The {@link IJavaProject} to create the {@link ILookupEnvironment} for.
   * @param allowErrors
   *          <code>true</code> if the resulting environment should be lenient with compile errors. If
   *          <code>false</code> the lookup environment will throw exceptions as soon as compile errors are found.
   * @return The new created {@link ILookupEnvironment}.
   * @throws CoreException
   */
  public static ILookupEnvironment createLookupEnvironment(IJavaProject javaProject, boolean allowErrors) throws CoreException {
    Validate.notNull(javaProject);
    IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
    List<File> cp = new ArrayList<>(allPackageFragmentRoots.length);
    for (IPackageFragmentRoot cpRoot : allPackageFragmentRoots) {
      if (cpRoot instanceof JarPackageFragmentRoot) {
        cp.add(((JarPackageFragmentRoot) cpRoot).internalPath().toFile());
      }
      else {
        cp.add(cpRoot.getResource().getLocation().toFile());
      }
    }
    return JavaParser.create(cp, allowErrors);
  }

  /**
   * @return A new created {@link IWorkingCopyManager}.
   */
  public static IWorkingCopyManager createWorkingCopyManager() {
    return new WorkingCopyManager();
  }
}
