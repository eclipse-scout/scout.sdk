/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.WrappedTrackedNodePosition;

/**
 * <h3>{@link TableFieldProposal}</h3>
 *
 * @since 5.2.0
 */
public class TableFieldProposal extends FormFieldProposal {

  private TypeDeclaration m_createdTable;

  public TableFieldProposal(String name, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(name, relevance, imageId, cu, context);
  }

  @Override
  protected TypeDeclaration createFormFieldType(Type superType) {
    var fieldBuilder = getFactory().newTableField(getProposalContext().getDefaultName())
        .withNlsMethod(getNlsMethodName())
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .withSuperType(superType)
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert();
    m_createdTable = fieldBuilder.getCreatedTable();
    return fieldBuilder.get();
  }

  @Override
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    super.fillRewrite(factory, superType);
    setEndPosition(new WrappedTrackedNodePosition(getRewrite().track(m_createdTable.getSuperclassType()), 2, 0));
  }
}
