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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * <h3>{@link StringBasedJdtCompilationUnit}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
class StringBasedJdtCompilationUnit implements ICompilationUnit {
  private final char[][] m_packageName;
  private final char[] m_fileName;
  private final char[] m_src;
  private final char[] m_mainTypeName;

  StringBasedJdtCompilationUnit(String packageName, String fileName, char[] src) {
    int dot = fileName.indexOf('.');
    String mainTypeName = dot >= 0 ? fileName.substring(0, dot) : fileName;
    m_packageName = packageName != null ? CharOperation.splitOn('.', packageName.toCharArray()) : null;
    m_fileName = fileName.toCharArray();
    m_src = src;
    m_mainTypeName = mainTypeName.toCharArray();
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
}
