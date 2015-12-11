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
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.s2e.ui.internal.util.ast.AstNodeFactory;

/**
 * <h3>{@link CodeProposal}</h3>
 *
 * @author Matthias Villiger
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
    TypeDeclaration createdCode = factory.newCode(getProposalContext().getDefaultName())
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
