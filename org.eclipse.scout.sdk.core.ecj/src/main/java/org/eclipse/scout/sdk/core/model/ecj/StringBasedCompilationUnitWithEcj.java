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
package org.eclipse.scout.sdk.core.model.ecj;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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

  public StringBasedCompilationUnitWithEcj(String packageName, String fileName, char[] src, String moduleName, String destinationPath) {
    m_destinationPath = destinationPath;
    int dot = fileName.indexOf(JavaTypes.C_DOT);
    String mainTypeName = dot >= 0 ? fileName.substring(0, dot) : fileName;
    m_packageName = packageName != null ? CharOperation.splitOn(JavaTypes.C_DOT, packageName.toCharArray()) : null;
    m_fileName = fileName.toCharArray();
    m_mainTypeName = mainTypeName.toCharArray();
    m_fqn = calcFqn(packageName, mainTypeName);
    m_src = src;
    if (Strings.isBlank(moduleName)) {
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

  protected static String calcFqn(String packageName, String mainTypeName) {
    StringBuilder fqnBuilder = new StringBuilder();
    if (Strings.hasText(packageName)) {
      fqnBuilder.append(packageName);
      fqnBuilder.append(JavaTypes.C_DOT);
    }
    return fqnBuilder.append(mainTypeName).toString();
  }
}
