/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;

/**
 * <h3>{@link ColumnProposal}</h3>
 *
 * @since 5.2.0
 */
public class ColumnProposal extends AbstractTypeProposal {

  public ColumnProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, relevance, imageId, cu, context);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    var columnType = createColumnType(superType);

    List<BodyDeclaration> bodyDeclarations = columnType.bodyDeclarations();
    setEndPosition(getRewrite().track(bodyDeclarations.get(bodyDeclarations.size() - 1)));
  }

  protected TypeDeclaration createColumnType(Type superType) {
    return getFactory().newColumn(getProposalContext().getDefaultName())
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .withSuperType(superType)
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert()
        .get();
  }
}
