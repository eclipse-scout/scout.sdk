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
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.scout.sdk.core.java.model.api.IImport;
import org.eclipse.scout.sdk.core.java.model.api.internal.ImportImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.ImportSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class DeclarationImportWithEcj extends AbstractJavaElementWithEcj<IImport> implements ImportSpi {
  private final ImportReference m_astNode;
  private final DeclarationCompilationUnitWithEcj m_cu;
  private final FinalValue<SourceRange> m_source;
  private final FinalValue<String> m_fqName;
  private final FinalValue<String> m_simpleName;
  private final FinalValue<String> m_qualifier;

  protected DeclarationImportWithEcj(AbstractJavaEnvironment env, DeclarationCompilationUnitWithEcj owner, ImportReference astNode) {
    super(env);
    m_astNode = astNode;
    m_cu = owner;
    m_source = new FinalValue<>();
    m_fqName = new FinalValue<>();
    m_simpleName = new FinalValue<>();
    m_qualifier = new FinalValue<>();
  }

  @Override
  public ImportSpi internalFindNewElement() {
    var newCu = getCompilationUnit().internalFindNewElement();
    if (newCu == null) {
      return null;
    }
    return newCu.getImports().stream()
        .filter(newImport -> newImport.getName().equals(getName()))
        .filter(newImport -> newImport.isStatic() == isStatic())
        .findFirst()
        .orElse(null);
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
    return m_fqName.computeIfAbsentAndGet(() -> {
      var name = CharOperation.toString(m_astNode.getImportName());
      if (m_astNode.trailingStarPosition > 0) {
        name += ".*";
      }
      return name;
    });
  }

  @Override
  public String getElementName() {
    return m_simpleName.computeIfAbsentAndGet(() -> new String(m_astNode.tokens[m_astNode.tokens.length - 1]));
  }

  @Override
  public String getQualifier() {
    return m_qualifier.computeIfAbsentAndGet(() -> {
      var importName = m_astNode.tokens;
      var qualifier = CharOperation.subarray(importName, 0, importName.length - 1);
      return CharOperation.toString(qualifier);
    });
  }

  @Override
  public DeclarationCompilationUnitWithEcj getCompilationUnit() {
    return m_cu;
  }

  @Override
  public boolean isStatic() {
    return m_astNode.isStatic();
  }

  @Override
  public SourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> javaEnvWithEcj().getSource(m_cu, m_astNode.declarationSourceStart, m_astNode.declarationSourceEnd));
  }
}
