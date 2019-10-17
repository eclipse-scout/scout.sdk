/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import java.nio.file.Path;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;

/**
 * <h3>{@link FileSystemWithOverride}</h3>
 *
 * @since 7.0.100
 */
public class FileSystemWithOverride extends FileSystem {

  private final JreInfo m_jreInfo;
  private final CompilationUnitOverrideSupport m_overrideSupport;
  private final Set<ClasspathEntry> m_cpEntries;

  protected FileSystemWithOverride(ClasspathBuilder cp) {
    super(cp.fullClasspath().toArray(new Classpath[0]), null, false);
    m_jreInfo = cp.jreInfo();
    m_cpEntries = cp.userClasspathEntries();
    m_overrideSupport = new CompilationUnitOverrideSupport();
  }

  private NameEnvironmentAnswer searchInOverrideSupport(char[] typeName, char[][] packageName) {
    char[] fqnWithSlash = CharOperation.concatWith(packageName, typeName, CompilationUnitOverrideSupport.SEPARATOR);
    ICompilationUnit overrideCu = overrideSupport().get(fqnWithSlash);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }
    return null;
  }

  private NameEnvironmentAnswer searchInOverrideSupport(char[][] compoundName) {
    char[] fqnWithSlash = CharOperation.concatWith(compoundName, CompilationUnitOverrideSupport.SEPARATOR);
    ICompilationUnit overrideCu = overrideSupport().get(fqnWithSlash);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }
    return null;
  }

  @Override
  public char[][] getModulesDeclaringPackage(char[][] packageName, char[] moduleName) {
    if (!hasModule(moduleName)) {
      char[] fqnWithSlash = CharOperation.concatWith(packageName, '/');
      if (overrideSupport().containsPackage(fqnWithSlash)) {
        return new char[][]{ModuleBinding.UNNAMED};
      }
    }
    return super.getModulesDeclaringPackage(packageName, moduleName);
  }

  @Override
  public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
    if (!hasModule(moduleName)) {
      NameEnvironmentAnswer answer = searchInOverrideSupport(compoundName);
      if (answer != null) {
        return answer;
      }
    }
    return super.findType(compoundName, moduleName);
  }

  @Override
  public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
    if (!hasModule(moduleName)) {
      NameEnvironmentAnswer answer = searchInOverrideSupport(typeName, packageName);
      if (answer != null) {
        return answer;
      }
    }
    return super.findType(typeName, packageName, moduleName);
  }

  @Override
  public boolean isPackage(char[][] compoundName, char[] packageName) {
    char[] fqnWithSlash = CharOperation.concatWith(compoundName, packageName, '/');
    return overrideSupport().containsPackage(fqnWithSlash)
        || super.isPackage(compoundName, packageName);
  }

  @Override
  public boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName, boolean checkCUs) {
    if (!hasModule(moduleName)) {
      for (ICompilationUnit icu : overrideSupport().getCompilationUnits()) {
        if (CharOperation.equals(icu.getPackageName(), qualifiedPackageName)) {
          return true;
        }
      }
    }
    return super.hasCompilationUnit(qualifiedPackageName, moduleName, checkCUs);
  }

  protected static boolean hasModule(char[] moduleName) {
    return moduleName != null && moduleName.length > 0;
  }

  public CompilationUnitOverrideSupport overrideSupport() {
    return m_overrideSupport;
  }

  public Set<? extends ClasspathEntry> classpath() {
    return m_cpEntries;
  }

  public Path jreHome() {
    return m_jreInfo.jreHome();
  }

  @Override
  public void cleanup() {
    m_overrideSupport.clear();
    m_cpEntries.clear();
    super.cleanup();
  }
}
