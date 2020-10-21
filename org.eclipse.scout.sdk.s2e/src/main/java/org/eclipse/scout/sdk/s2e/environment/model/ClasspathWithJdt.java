/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.environment.model;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.scout.sdk.core.model.ecj.ClasspathWithEcj;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link ClasspathWithJdt}</h3>
 *
 * @since 7.0.0
 */
public class ClasspathWithJdt extends ClasspathWithEcj {

  private final IPackageFragmentRoot m_cpRoot;
  private final FinalValue<Boolean> m_isSourceFolder;
  private final IJavaProject m_javaProject;

  protected ClasspathWithJdt(ClasspathEntryWithJdt entry, AbstractJavaEnvironment env) {
    super(entry, env);
    m_cpRoot = entry.getRoot();
    m_isSourceFolder = new FinalValue<>();
    m_javaProject = m_cpRoot.getJavaProject();
  }

  @Override
  public JavaEnvironmentWithJdt getJavaEnvironment() {
    return (JavaEnvironmentWithJdt) super.getJavaEnvironment();
  }

  @Override
  public boolean isSourceFolder() {
    return m_isSourceFolder.computeIfAbsentAndGet(() -> getJavaProject().equals(getJavaEnvironment().javaProject()) && isJavaSourceFolder(getRoot()));
  }

  public static boolean isJavaSourceFolder(IPackageFragmentRoot root) {
    try {
      if (root.getKind() != IPackageFragmentRoot.K_SOURCE || root.isArchive() || root.isExternal()) {
        return false;
      }
      var resource = root.getResource();
      if (resource == null || !resource.exists() || resource.isDerived()) {
        return false;
      }
      return isJavaFileIncludedIn(root.getRawClasspathEntry());
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  protected static boolean isJavaFileIncludedIn(IClasspathEntry rawClasspathEntry) {
    if (rawClasspathEntry == null) {
      return true;
    }
    var exclusionPatterns = rawClasspathEntry.getExclusionPatterns();
    if (exclusionPatterns != null && exclusionPatterns.length > 0) {
      var javaSample = ("Whatever" + JavaTypes.JAVA_FILE_SUFFIX).toCharArray();
      for (var excludedPath : exclusionPatterns) {
        var pattern = excludedPath.toString().toCharArray();
        var javaFilesExcluded = CharOperation.pathMatch(pattern, javaSample, true, '/');
        if (javaFilesExcluded) {
          return false;
        }
      }
    }
    return true;
  }

  public IPackageFragmentRoot getRoot() {
    return m_cpRoot;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  @Override
  public String toString() {
    return new StringBuilder().append("ClasspathWithJdt [").append(getRoot()).append(']').toString();
  }
}
