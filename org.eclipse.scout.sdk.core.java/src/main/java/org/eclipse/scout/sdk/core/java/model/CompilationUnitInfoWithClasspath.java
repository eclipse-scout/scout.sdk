/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model;

import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.util.Ensure;

public class CompilationUnitInfoWithClasspath extends CompilationUnitInfo {
  private final IClasspathEntry m_cpEntry;

  /**
   * @param sourceFolder
   *          The target source folder in which the compilation unit resides. May be {@code null} in case the folder is
   *          not known. Then {@link #targetFile()} and {@link #targetDirectory()} only return source folder relative
   *          paths.
   * @param sourceFolderRelPath
   *          The {@link Path} to the compilation unit relative to the given sourceFolder. Must not be {@code null}.
   *          E.g. {@code 'org/eclipse/scout/MyClass.java'} or {@code 'MyClassInDefaultPackage.java'}.
   */
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public CompilationUnitInfoWithClasspath(IClasspathEntry sourceFolder, Path sourceFolderRelPath) {
    super(sourceFolder == null ? null : sourceFolder.path(), sourceFolderRelPath);
    m_cpEntry = sourceFolder;
  }

  /**
   * @param sourceFolder
   *          The target source folder in which the compilation unit resides. May be {@code null} in case the folder is
   *          not known. Then {@link #targetFile()} and {@link #targetDirectory()} only return source folder relative
   *          paths.
   * @param packageName
   *          The package of the compilation unit or {@code null} or an empty string if the default package.
   * @param fileName
   *          The compilation unit file name wit the java file suffix ({@code .java}). Must not be {@code null} or
   *          empty.
   */
  public CompilationUnitInfoWithClasspath(IClasspathEntry sourceFolder, String packageName, String fileName) {
    super(sourceFolder == null ? null : sourceFolder.path(), packageName, fileName);
    m_cpEntry = sourceFolder;
  }

  /**
   * @param sourceFolder
   *          The source folder in which the compilation unit will be created. May be {@code null} in case the folder is
   *          not known. Then {@link #targetFile()} and {@link #targetDirectory()} only return source folder relative
   *          paths.
   * @param generator
   *          The {@link ICompilationUnitGenerator} that defines name and package. Must not be {@code null}.
   */
  public CompilationUnitInfoWithClasspath(IClasspathEntry sourceFolder, ICompilationUnitGenerator<?> generator) {
    this(sourceFolder, generator.packageName().orElse(null),
        generator.fileName().orElseThrow(() -> Ensure.newFail("File name missing in generator")));
  }

  /**
   * @return The {@link #sourceFolder()} as {@link IClasspathEntry} or {@code null} if unknown.
   */
  public IClasspathEntry classpathEntry() {
    return m_cpEntry;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    var that = (CompilationUnitInfoWithClasspath) o;
    return Objects.equals(m_cpEntry, that.m_cpEntry);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), m_cpEntry);
  }
}
