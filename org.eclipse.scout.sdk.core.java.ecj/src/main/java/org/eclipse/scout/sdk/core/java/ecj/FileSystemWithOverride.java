/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
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
    var fqnWithSlash = CharOperation.concatWith(packageName, typeName, CompilationUnitOverrideSupport.SEPARATOR);
    var overrideCu = overrideSupport().get(fqnWithSlash);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }
    return null;
  }

  private NameEnvironmentAnswer searchInOverrideSupport(char[][] compoundName) {
    var fqnWithSlash = CharOperation.concatWith(compoundName, CompilationUnitOverrideSupport.SEPARATOR);
    var overrideCu = overrideSupport().get(fqnWithSlash);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }
    return null;
  }

  @Override
  public char[][] getModulesDeclaringPackage(char[][] packageName, char[] moduleName) {
    var classpathModules = super.getModulesDeclaringPackage(packageName, moduleName);
    if (hasModule(moduleName)) {
      return classpathModules; // no need to check in override as Scout does not support the module system
    }
    if (containsUnnamedModuleName(classpathModules)) {
      return classpathModules; // unnamed module already in the list: cannot add anything more
    }

    var fqnWithSlash = CharOperation.concatWith(packageName, '/');
    if (!overrideSupport().containsPackage(fqnWithSlash)) {
      return classpathModules; // nothing found in overrides
    }

    // An override was found and the unnamed module is not yet in the list: Add to existing modules
    return withUnnamedModule(classpathModules);
  }

  protected static char[][] withUnnamedModule(char[][] existing) {
    if (existing == null) {
      // no other modules: only the unnamed module is the result
      return new char[][]{ModuleBinding.UNNAMED};
    }

    // add unnamed to existing list
    var result = new char[existing.length + 1][];
    System.arraycopy(existing, 0, result, 0, existing.length);
    result[existing.length] = ModuleBinding.UNNAMED;
    return result;
  }

  protected static boolean containsUnnamedModuleName(char[][] moduleNames) {
    if (moduleNames == null || moduleNames.length < 1) {
      return false;
    }
    //noinspection Convert2streamapi
    for (var module : moduleNames) {
      if (CharOperation.equals(module, ModuleBinding.UNNAMED)) {
        return true; // unnamed module already in the list
      }
    }
    return false;
  }

  @Override
  public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
    if (!hasModule(moduleName)) {
      var answer = searchInOverrideSupport(compoundName);
      if (answer != null) {
        return answer;
      }
    }
    return super.findType(compoundName, moduleName);
  }

  @Override
  public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
    if (!hasModule(moduleName)) {
      var answer = searchInOverrideSupport(typeName, packageName);
      if (answer != null) {
        return answer;
      }
    }
    return super.findType(typeName, packageName, moduleName);
  }

  @Override
  public boolean isPackage(char[][] compoundName, char[] packageName) {
    var fqnWithSlash = CharOperation.concatWith(compoundName, packageName, '/');
    return overrideSupport().containsPackage(fqnWithSlash)
        || super.isPackage(compoundName, packageName);
  }

  @Override
  public boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName, boolean checkCUs) {
    if (!hasModule(moduleName)) {
      for (var icu : overrideSupport().getCompilationUnits()) {
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

  public JreInfo jreInfo() {
    return m_jreInfo;
  }

  @Override
  public void cleanup() {
    m_overrideSupport.clear();
    m_cpEntries.clear();
    super.cleanup();
  }
}
