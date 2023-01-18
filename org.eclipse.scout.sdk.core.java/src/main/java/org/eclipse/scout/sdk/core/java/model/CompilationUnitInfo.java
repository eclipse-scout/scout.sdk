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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Helper class to compute the path segments of a compilation unit
 */
public class CompilationUnitInfo {

  private static final Path EMPTY_PATH = Paths.get("");

  private final String m_fileName;
  private final Path m_targetDirectory;
  private final Path m_targetFile;
  private final Path m_sourceFolder;
  private final String m_mainTypeName;
  private final String m_package;

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
  public CompilationUnitInfo(Path sourceFolder, Path sourceFolderRelPath) {
    if (sourceFolder == null) {
      sourceFolder = EMPTY_PATH;
    }
    m_sourceFolder = sourceFolder;
    m_fileName = sourceFolderRelPath.getFileName().toString();
    m_targetFile = sourceFolder.resolve(sourceFolderRelPath);
    m_targetDirectory = m_targetFile.getParent();
    m_mainTypeName = computeMainClassName(m_fileName);

    var packagePath = sourceFolderRelPath.getParent();
    if (packagePath == null) {
      m_package = null;
    }
    else {
      m_package = packagePath.toString()
          .replace('/', JavaTypes.C_DOT)
          .replace('\\', JavaTypes.C_DOT);
    }
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
  public CompilationUnitInfo(Path sourceFolder, String packageName, String fileName) {
    if (sourceFolder == null) {
      sourceFolder = EMPTY_PATH;
    }
    m_sourceFolder = sourceFolder;
    m_fileName = Ensure.notBlank(fileName);
    m_mainTypeName = computeMainClassName(m_fileName);
    var isDefaultPackage = Strings.isBlank(packageName);
    if (isDefaultPackage) {
      m_package = null;
      m_targetDirectory = sourceFolder;
    }
    else {
      m_package = packageName;
      m_targetDirectory = sourceFolder.resolve(packageName.replace(JavaTypes.C_DOT, File.separatorChar));
    }
    m_targetFile = m_targetDirectory.resolve(m_fileName);
  }

  private static String computeMainClassName(String fileName) {
    return Strings.removeSuffix(fileName, JavaTypes.JAVA_FILE_SUFFIX);
  }

  private static String pathToClasspathString(Path p) {
    return p.toString().replace('\\', '/');
  }

  /**
   * @return The filename of the compilation unit. E.g. {@code MyClass.java}. Never returns {@code null}.
   */
  public String fileName() {
    return m_fileName;
  }

  /**
   * @return The directory in which the compilation unit will be stored. Never returns {@code null}.
   */
  public Path targetDirectory() {
    return m_targetDirectory;
  }

  /**
   * @return The directory in which the compilation unit will be stored as classpath {@link String}. Never returns
   *         {@code null}.
   */
  public String targetDirectoryAsString() {
    return pathToClasspathString(targetDirectory());
  }

  /**
   * @return The path in which the compilation unit will be stored. Never returns {@code null}.
   */
  public Path targetFile() {
    return m_targetFile;
  }

  /**
   * @return The target file of the compilation unit as classpath {@link String}. Never returns {@code null}.
   */
  public String targetFileAsString() {
    return pathToClasspathString(targetFile());
  }

  /**
   * @return The source folder part of the path of the compilation unit. Never returns {@code null}.
   */
  public Path sourceFolder() {
    return m_sourceFolder;
  }

  /**
   * @return The source folder part of the compilation unit path as {@link String}. Never returns {@code null}.
   */
  public String sourceDirectoryAsString() {
    return pathToClasspathString(sourceFolder());
  }

  /**
   * @return The simple name of the main type of the compilation unit. Never returns {@code null}.
   */
  public String mainTypeSimpleName() {
    return m_mainTypeName;
  }

  /**
   * @return The fully qualified name of the main type of the compilation unit. Never returns {@code null}.
   */
  public String mainTypeFullyQualifiedName() {
    var fqn = new StringBuilder();
    var pck = packageName();
    if (Strings.hasText(pck)) {
      fqn.append(pck).append(JavaTypes.C_DOT);
    }
    fqn.append(mainTypeSimpleName());
    return fqn.toString();
  }

  /**
   * @return The package name or {@code null} for the default package.
   */
  public String packageName() {
    return m_package;
  }

  @Override
  public String toString() {
    return CompilationUnitInfo.class.getSimpleName() + " [" + targetFileAsString() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    var that = (CompilationUnitInfo) o;
    return m_targetFile.equals(that.m_targetFile)
        && Objects.equals(m_sourceFolder, that.m_sourceFolder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_targetFile, m_sourceFolder);
  }
}
