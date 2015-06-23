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
package org.eclipse.scout.sdk.core.model;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

/**
 *
 */
public class ImportDeclaration implements IImportDeclaration {
  private final ImportReference m_ref;
  private final ICompilationUnit m_icu;

  ImportDeclaration(ImportReference ref, ICompilationUnit owner) {
    m_ref = ref;
    m_icu = owner;
  }

  @Override
  public String getName() {
    return CharOperation.toString(m_ref.tokens);
  }

  @Override
  public String getSimpleName() {
    char[][] importName = m_ref.tokens;
    return new String(importName[importName.length - 1]);
  }

  @Override
  public String getQualifier() {
    char[][] importName = m_ref.tokens;
    char[][] qualifier = CharOperation.subarray(importName, 0, importName.length - 1);
    return CharOperation.toString(qualifier);
  }

  @Override
  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }
}
