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
package org.eclipse.scout.sdk.core.parser;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * <h3>{@link StringCompilationUnit}</h3>
 *
 * @author mvi
 * @since 5.1.0
 */
class StringCompilationUnit implements ICompilationUnit {

  private final char[] m_src;
  private final char[] m_mainTypeName;
  private final char[] m_fileName;

  StringCompilationUnit(char[] src, char[] mainTypeName, char[] fileName) {
    m_src = Validate.notNull(src);
    m_mainTypeName = Validate.notNull(mainTypeName);
    m_fileName = Validate.notNull(fileName);
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
    // ignore package consistency checks
    return null;
  }

  @Override
  public boolean ignoreOptionalProblems() {
    return false;
  }
}
