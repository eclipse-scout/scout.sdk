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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.scout.sdk.s2e.ui.internal.template.ast.AstNodeFactory;

/**
 * <h3>{@link RadioButtonProposal}</h3>
 *
 * @since 5.2.0
 */
public class RadioButtonProposal extends FormFieldProposal {

  public RadioButtonProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, relevance, imageId, cu, context);
  }

  @Override
  protected void fillRewrite(AstNodeFactory factory, Type superType) {
    factory.newRadioButton(getProposalContext().getDefaultName())
        .withSuperType(superType) // type parameter is calculated and added in the factory
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert();
  }
}
