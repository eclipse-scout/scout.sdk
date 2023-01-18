/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link StringBasedCompilationUnitWithEcj}</h3>
 *
 * @since 5.1.0
 */
public class StringBasedCompilationUnitWithEcj implements ICompilationUnit {
  private final char[][] m_packageName;
  private final char[] m_fileName;
  private final char[] m_src;
  private final char[] m_mainTypeName;
  private final String m_fqn;
  private final char[] m_moduleName;
  private final String m_destinationPath;

  public StringBasedCompilationUnitWithEcj(CompilationUnitInfo cuInfo, char[] src, String moduleName) {
    m_destinationPath = cuInfo.targetFileAsString();
    var pck = cuInfo.packageName();
    m_packageName = pck != null ? CharOperation.splitOn(JavaTypes.C_DOT, pck.toCharArray()) : null;
    m_fileName = m_destinationPath.toCharArray(); // must be the full file name
    m_mainTypeName = cuInfo.mainTypeSimpleName().toCharArray();
    m_fqn = cuInfo.mainTypeFullyQualifiedName();
    m_src = src;
    if (Strings.isEmpty(moduleName)) {
      m_moduleName = ModuleBinding.UNNAMED;
    }
    else {
      m_moduleName = moduleName.toCharArray();
    }
  }

  @Override
  public char[] getFileName() {
    return m_fileName;
  }

  @Override
  public char[] getContents() {
    return m_src;
  }

  @Override
  public char[] getMainTypeName() {
    return m_mainTypeName;
  }

  @Override
  public char[][] getPackageName() {
    return m_packageName;
  }

  @Override
  public boolean ignoreOptionalProblems() {
    return true;
  }

  public String getFullyQualifiedName() {
    return m_fqn;
  }

  @Override
  public char[] getModuleName() {
    return m_moduleName;
  }

  @Override
  public String getDestinationPath() {
    return m_destinationPath;
  }
}
