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
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.ImportImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.ImportSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 *
 */
public class DeclarationImportWithJdt extends AbstractJavaElementWithJdt<IImport> implements ImportSpi {
  private final ImportReference m_astNode;
  private final DeclarationCompilationUnitWithJdt m_cu;
  private ISourceRange m_source;

  DeclarationImportWithJdt(JavaEnvironmentWithJdt env, DeclarationCompilationUnitWithJdt owner, ImportReference astNode) {
    super(env);
    m_astNode = astNode;
    m_cu = owner;
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    CompilationUnitSpi newCu = (CompilationUnitSpi) getCompilationUnit().internalFindNewElement(newEnv);
    if (newCu != null) {
      for (ImportSpi i : newCu.getImports()) {
        if (i.getName().equals(this.getName()) && i.isStatic() == this.isStatic()) {
          return i;
        }
      }
    }
    return null;
  }

  @Override
  protected IImport internalCreateApi() {
    return new ImportImplementor(this);
  }

  public ImportReference getInternalImportReference() {
    return m_astNode;
  }

  @Override
  public String getName() {
    String name = CharOperation.toString(m_astNode.getImportName());
    if (m_astNode.trailingStarPosition > 0) {
      name += ".*";
    }
    return name;
  }

  @Override
  public String getSimpleName() {
    char[][] importName = m_astNode.tokens;
    return new String(importName[importName.length - 1]);
  }

  @Override
  public String getQualifier() {
    char[][] importName = m_astNode.tokens;
    char[][] qualifier = CharOperation.subarray(importName, 0, importName.length - 1);
    return CharOperation.toString(qualifier);
  }

  @Override
  public String getElementName() {
    return getName();
  }

  @Override
  public DeclarationCompilationUnitWithJdt getCompilationUnit() {
    return m_cu;
  }

  @Override
  public boolean isStatic() {
    return m_astNode.isStatic();
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      ImportReference decl = m_astNode;
      m_source = m_env.getSource(m_cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    }
    return m_source;
  }

}
