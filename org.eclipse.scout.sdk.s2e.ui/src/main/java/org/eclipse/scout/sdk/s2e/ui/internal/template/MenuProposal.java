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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.ZeroLenWrappedTrackedNodePosition;

/**
 * <h3>{@link MenuProposal}</h3>
 *
 * @since 5.2.0
 */
public class MenuProposal extends AbstractTypeProposal {

  public MenuProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, relevance, imageId, cu, context);
  }

  @Override
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    var menuBuilder = factory.newMenu(getProposalContext().getDefaultName())
        .withSuperType(superType)
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert();

    setEndPosition(new ZeroLenWrappedTrackedNodePosition(getRewrite().track(menuBuilder.getExecAction().getBody()), 1));
  }
}
