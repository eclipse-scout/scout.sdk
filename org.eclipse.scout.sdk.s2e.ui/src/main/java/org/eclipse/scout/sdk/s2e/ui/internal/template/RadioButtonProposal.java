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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.scout.sdk.s2e.ui.internal.util.ast.AstNodeFactory;

/**
 * <h3>{@link RadioButtonProposal}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class RadioButtonProposal extends FormFieldProposal {

  public RadioButtonProposal(String displayName, int relevance, String imageId, ICompilationUnit cu, TypeProposalContext context) {
    super(displayName, relevance, imageId, cu, context);
  }

  @Override
  protected void fillRewrite(AstNodeFactory factory, Type superType) throws CoreException {
    factory.newRadioButton(getProposalContext().getDefaultName())
        .withSuperType(superType) // type parameter is calculated and added in the factory
        .withReadOnlyNameSuffix(getProposalContext().getSuffix())
        .in(getProposalContext().getDeclaringType())
        .atPosition(getProposalContext().getInsertPosition())
        .insert();
  }
}
