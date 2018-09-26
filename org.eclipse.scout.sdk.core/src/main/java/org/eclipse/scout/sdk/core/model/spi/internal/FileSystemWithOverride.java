/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

/**
 * <h3>{@link FileSystemWithOverride}</h3>
 *
 * @since 7.0.100
 */
public class FileSystemWithOverride extends FileSystem {

  private final JreInfo m_jreInfo;
  private final CompilationUnitOverrideSupport m_overrideSupport;
  private final Set<ClasspathEntry> m_cpEntries;

  protected FileSystemWithOverride(final ClasspathBuilder cp) {
    super(cp.fullClasspath(), null, false);
    m_jreInfo = cp.jreInfo();
    m_cpEntries = cp.entries();
    m_overrideSupport = new CompilationUnitOverrideSupport();
  }

  @Override
  public NameEnvironmentAnswer findType(final char[][] compoundName) {
    final NameEnvironmentAnswer answer = searchInOverrideSupport(compoundName);
    if (answer != null) {
      return answer;
    }
    return super.findType(compoundName);
  }

  private NameEnvironmentAnswer searchInOverrideSupport(final char[] typeName, final char[][] packageName) {
    final char[] fqnWithSlash = CharOperation.concatWith(packageName, typeName, CompilationUnitOverrideSupport.SEPARATOR);
    final ICompilationUnit overrideCu = overrideSupport().get(fqnWithSlash);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }
    return null;
  }

  private NameEnvironmentAnswer searchInOverrideSupport(final char[][] compoundName) {
    final char[] fqnWithSlash = CharOperation.concatWith(compoundName, CompilationUnitOverrideSupport.SEPARATOR);
    final ICompilationUnit overrideCu = overrideSupport().get(fqnWithSlash);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }
    return null;
  }

  @Override
  public NameEnvironmentAnswer findType(final char[] typeName, final char[][] packageName) {
    final NameEnvironmentAnswer answer = searchInOverrideSupport(typeName, packageName);
    if (answer != null) {
      return answer;
    }
    return super.findType(typeName, packageName);
  }

  @Override
  public boolean isPackage(final char[][] compoundName, final char[] packageName) {
    final char[] fqnWithSlash = CharOperation.concatWith(compoundName, packageName, '/');
    return overrideSupport().containsPackage(fqnWithSlash)
        || super.isPackage(compoundName, packageName);
  }

  @Override
  public NameEnvironmentAnswer findType(final char[][] compoundName, final char[] moduleName) {
    final NameEnvironmentAnswer answer = searchInOverrideSupport(compoundName);
    if (answer != null) {
      return answer;
    }
    return super.findType(compoundName, moduleName);
  }

  @Override
  public NameEnvironmentAnswer findType(final char[] typeName, final char[][] packageName, final char[] moduleName) {
    final NameEnvironmentAnswer answer = searchInOverrideSupport(typeName, packageName);
    if (answer != null) {
      return answer;
    }
    return super.findType(typeName, packageName, moduleName);
  }

  @Override
  public boolean hasCompilationUnit(final char[][] qualifiedPackageName, final char[] moduleName, final boolean checkCUs) {
    for (final ICompilationUnit icu : overrideSupport().getCompilationUnits()) {
      if (CharOperation.equals(icu.getPackageName(), qualifiedPackageName)) {
        return true;
      }
    }
    return super.hasCompilationUnit(qualifiedPackageName, moduleName, checkCUs);
  }

  public CompilationUnitOverrideSupport overrideSupport() {
    return m_overrideSupport;
  }

  public Stream<ClasspathEntry> classpath() {
    return m_cpEntries.stream();
  }

  public Path jreHome() {
    return m_jreInfo.jreHome();
  }

  @Override
  public void cleanup() {
    m_overrideSupport.clear();
    super.cleanup();
  }
}
