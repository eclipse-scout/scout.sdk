/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.ZeroLenWrappedTrackedNodePosition;

/**
 * <h3>{@link ButtonProposal}</h3>
 *
 * @since 5.2.0
 */
public class ButtonProposal extends FormFieldProposal {

  private MethodDeclaration m_execClickAction;

  public ButtonProposal(String name, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(name, relevance, imageId, cu, context);
  }

  @Override
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    super.fillRewrite(factory, superType);
    setEndPosition(new ZeroLenWrappedTrackedNodePosition(getRewrite().track(m_execClickAction.getBody()), 1));
  }

  @Override
  protected TypeDeclaration createFormFieldType(Type superType) {
    var buttonBuilder = getFactory().newButton(getProposalContext().getDefaultName())
        .withNlsMethod(getNlsMethodName())
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .withSuperType(superType)
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert();
    m_execClickAction = buttonBuilder.getExecClickAction();
    return buttonBuilder.get();
  }
}
