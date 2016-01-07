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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.core.util.compat.CompatibilityLayer;

public class WorkspaceFileSystem implements INameEnvironment, SuffixConstants {
  private Classpath[] m_classpaths;
  private final Map<String, ICompilationUnit> m_overrideCompilationUnits = new HashMap<>();
  private final Set<String> m_additionalPackages = new HashSet<>();

  /**
   * @param paths
   * @param overrideCompilationUnits
   *          is a map of "a/b/c/JavaClass.class" to its content
   */
  public WorkspaceFileSystem(Classpath[] paths) {
    final int length = paths.length;
    this.m_classpaths = new FileSystem.Classpath[length];
    try {
      for (int i = 0; i < length; i++) {
        final Classpath classpath = paths[i];
        classpath.initialize();
        this.m_classpaths[i] = classpath;
      }
    }
    catch (IOException exception) {
      throw new SdkException("Unable to initialize classpath.", exception);
    }
  }

  public void addOverrideCompilationUnit(ICompilationUnit cu) {
    char[][] packageName0 = cu.getPackageName();

    //register compilation unit with normalized class name
    String fileName = new String(cu.getFileName());
    if (fileName.toLowerCase().endsWith(SuffixConstants.SUFFIX_STRING_java)) {
      fileName = fileName.substring(0, fileName.length() - SuffixConstants.SUFFIX_STRING_java.length()) + SuffixConstants.SUFFIX_STRING_class;
    }

    // build key
    StringBuilder keyBuilder = new StringBuilder();
    if (packageName0 != null && packageName0.length > 0) {
      char[] pck = CharOperation.concatWith(packageName0, '/');
      keyBuilder.append(pck);
      keyBuilder.append('/');
    }
    keyBuilder.append(fileName);

    m_overrideCompilationUnits.put(keyBuilder.toString(), cu);

    //register additional packages
    if (packageName0 != null && packageName0.length > 0) {
      for (int i = 1; i < packageName0.length; i++) {
        m_additionalPackages.add(new String(CharOperation.concatWith(CharOperation.subarray(packageName0, 0, i), '/')));
      }
    }
  }

  public Collection<ICompilationUnit> getOverrideCompilationUnits() {
    return Collections.unmodifiableCollection(m_overrideCompilationUnits.values());
  }

  @Override
  public void cleanup() {
  }

  @Override
  public boolean isPackage(char[][] compoundName, char[] packageName) {
    String qualifiedPackageName = new String(CharOperation.concatWith(compoundName, packageName, '/'));
    if (m_additionalPackages.contains(qualifiedPackageName)) {
      return true;
    }

    String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
    if (qualifiedPackageName.equals(qp2)) {
      for (int i = 0, length = this.m_classpaths.length; i < length; i++) {
        if (this.m_classpaths[i].isPackage(qualifiedPackageName)) {
          return true;
        }
      }
    }
    else {
      for (int i = 0, length = this.m_classpaths.length; i < length; i++) {
        Classpath p = this.m_classpaths[i];
        if ((p instanceof ClasspathJar) ? p.isPackage(qualifiedPackageName) : p.isPackage(qp2)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public NameEnvironmentAnswer findType(char[][] compoundName) {
    if (compoundName != null) {
      return findInternal(new String(CharOperation.concatWith(compoundName, '/')), compoundName[compoundName.length - 1]);
    }
    return null;
  }

  @Override
  public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
    if (typeName != null) {
      return findInternal(new String(CharOperation.concatWith(packageName, typeName, '/')), typeName);
    }
    return null;
  }

  /**
   * @return best answer where source types takes precedence over binary types
   */
  private NameEnvironmentAnswer findInternal(String qualifiedName, char[] simpleName) {
    String qualifiedFileName = qualifiedName + SUFFIX_STRING_class;
    String qualifiedPackageName = qualifiedName.length() == simpleName.length ? Util.EMPTY_STRING : qualifiedFileName.substring(0, qualifiedName.length() - simpleName.length - 1);

    ICompilationUnit overrideCu = m_overrideCompilationUnits.get(qualifiedFileName);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }

    String qualifiedFileNameLocalized;
    String qualifiedPackageNameLocalized;
    if (File.separatorChar == '/') {
      qualifiedFileNameLocalized = qualifiedFileName;
      qualifiedPackageNameLocalized = qualifiedPackageName;
    }
    else {
      qualifiedFileNameLocalized = qualifiedFileName.replace('/', File.separatorChar);
      qualifiedPackageNameLocalized = qualifiedPackageName.replace('/', File.separatorChar);
    }

    for (Classpath cp : m_classpaths) {
      NameEnvironmentAnswer answer;
      if (cp instanceof ClasspathJar) {
        answer = cp.findClass(simpleName, qualifiedPackageName, qualifiedFileName, false);
      }
      else {
        answer = cp.findClass(simpleName, qualifiedPackageNameLocalized, qualifiedFileNameLocalized, false);
      }
      if (answer != null) {
        //prio 1: source type
        if (answer.isSourceType()) {
          return answer;
        }
        //prio 2: compilation unit
        if (answer.isCompilationUnit()) {
          return answer;
        }
        //prio 3: binary type
        if (answer.isBinaryType()) {
          return answer;
        }
      }
    }
    return null;
  }

  public static Classpath createClasspath(File f, boolean source, String encoding) {
    if (f.exists()) {
      if (encoding == null) {
        encoding = StandardCharsets.UTF_8.name();
      }

      Classpath classpath = CompatibilityLayer.getFileSystemClasspath(f, source, encoding, new Callable<Map<?, ?>>() {
        @Override
        public Map<?, ?> call() throws Exception {
          return AstCompiler.createDefaultOptions().getMap();
        }
      });
      try {
        classpath.initialize();
        return classpath;
      }
      catch (IOException e) {
        SdkLog.warning("Unable to initialize classpath '{}' because: {} ", f.getAbsolutePath(), e.getMessage());
      }
    }
    return null;
  }

}
