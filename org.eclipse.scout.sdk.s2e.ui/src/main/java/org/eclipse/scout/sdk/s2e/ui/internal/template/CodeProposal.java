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
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;

/**
 * <h3>{@link CodeProposal}</h3>
 *
 * @since 5.2.0
 */
public class CodeProposal extends AbstractTypeProposal {

  public CodeProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, relevance, imageId, cu, context);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    // code
    var createdCode = factory.newCode(getProposalContext().getDefaultName())
        .withSuperType(superType) // type parameter is calculated and added in the factory
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert()
        .get();

    List<BodyDeclaration> bodyDeclarations = createdCode.bodyDeclarations();
    setEndPosition(getRewrite().track(bodyDeclarations.get(bodyDeclarations.size() - 1)));
  }
}
