/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstKeyStrokeBuilder;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.ZeroLenWrappedTrackedNodePosition;

/**
 * <h3>{@link KeyStrokeProposal}</h3>
 *
 * @since 5.2.0
 */
public class KeyStrokeProposal extends AbstractTypeProposal {

  public KeyStrokeProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, relevance, imageId, cu, context);
  }

  @Override
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    AstKeyStrokeBuilder keyStrokeBuilder = factory.newKeyStroke(getProposalContext().getDefaultName())
        .withSuperType(superType)
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert();

    setEndPosition(new ZeroLenWrappedTrackedNodePosition(getRewrite().track(keyStrokeBuilder.getExecAction().getBody()), 1));
  }
}
