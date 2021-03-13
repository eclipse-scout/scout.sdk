/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.compilationunit;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Helper class to compute the path segments of a compilation unit
 */
public class CompilationUnitInfo {

  private final String m_fileName;
  private final Path m_targetDirectory;
  private final Path m_targetFile;
  private final IClasspathEntry m_sourceFolder;
  private final String m_mainTypeName;
  private final String m_package;

  /**
   * @param sourceFolder
   *          The target source folder in which the compilation unit resides. Must not be {@code null}.
   * @param sourceFolderRelPath
   *          The {@link Path} to the compilation unit relative to the given sourceFolder. Must not be {@code null}.
   *          E.g. {@code 'org/eclipse/scout/MyClass.java'} or {@code 'MyClassInDefaultPackage.java'}.
   */
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public CompilationUnitInfo(IClasspathEntry sourceFolder, Path sourceFolderRelPath) {
    m_sourceFolder = sourceFolder;
    m_fileName = sourceFolderRelPath.getFileName().toString();
    m_targetFile = sourceFolder.path().resolve(sourceFolderRelPath);
    m_targetDirectory = m_targetFile.getParent();
    var mainTypeName = m_fileName;
    if (mainTypeName.endsWith(JavaTypes.JAVA_FILE_SUFFIX)) {
      mainTypeName = mainTypeName.substring(0, mainTypeName.length() - JavaTypes.JAVA_FILE_SUFFIX.length());
    }
    m_mainTypeName = mainTypeName;

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
   *          The target source folder in which the compilation unit resides. Must not be {@code null}.
   * @param packageName
   *          The package of the compilation unit or {@code null} or an empty string if the default package.
   * @param classSimpleName
   *          The simple name of the main class of the compilation unit without any file suffixes. Must not be
   *          {@code null} or empty.
   */
  public CompilationUnitInfo(IClasspathEntry sourceFolder, String packageName, String classSimpleName) {
    m_mainTypeName = Ensure.notBlank(classSimpleName);
    m_fileName = classSimpleName + JavaTypes.JAVA_FILE_SUFFIX;
    var isDefaultPackage = Strings.isBlank(packageName);
    if (isDefaultPackage) {
      m_package = null;
      m_targetDirectory = sourceFolder.path();
    }
    else {
      m_package = packageName;
      m_targetDirectory = sourceFolder.path().resolve(packageName.replace(JavaTypes.C_DOT, File.separatorChar));
    }
    m_targetFile = m_targetDirectory.resolve(m_fileName);
    m_sourceFolder = sourceFolder;
  }

  /**
   * @param generator
   *          The {@link ICompilationUnitGenerator} that defines name and package. Must not be {@code null}.
   * @param sourceFolder
   *          The source folder in which the compilation unit will be created. Must not be {@code null}.
   */
  public CompilationUnitInfo(ICompilationUnitGenerator<?> generator, IClasspathEntry sourceFolder) {
    this(sourceFolder, generator.packageName().orElse(null),
        generator.elementName().orElseThrow(() -> Ensure.newFail("File name missing in generator")));
  }

  /**
   * @return The filename of the compilation unit. E.g. {@code MyClass.java}. Never returns {@code null}.
   */
  public String fileName() {
    return m_fileName;
  }

  /**
   * @return The absolute directory in which the compilation unit will be stored. Never returns {@code null}.
   */
  public Path targetDirectory() {
    return m_targetDirectory;
  }

  /**
   * @return The absolute path in which the compilation unit will be stored. Never returns {@code null}.
   */
  public Path targetFile() {
    return m_targetFile;
  }

  /**
   * @return The source folder part of the absolute path of the compilation unit. Never returns {@code null}.
   */
  public IClasspathEntry sourceFolder() {
    return m_sourceFolder;
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
    return CompilationUnitInfo.class.getSimpleName() + " [" + targetFile().toString().replace('\\', '/') + "]";
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
        && Objects.equals(m_package, that.m_package);
  }

  @Override
  public int hashCode() {
    var result = m_targetFile.hashCode();
    return 31 * result + (m_package != null ? m_package.hashCode() : 0);
  }
}
