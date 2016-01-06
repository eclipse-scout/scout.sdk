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
package org.eclipse.scout.sdk.s2e.ui.internal.util.ast;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link AstSequenceBoxBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AstSequenceBoxBuilder extends AstTypeBuilder<AstSequenceBoxBuilder> {

  protected AstSequenceBoxBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstSequenceBoxBuilder insert() {
    super.insert();

    addGetConfiguredAutoCheckFromTo();

    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfiguredAutoCheckFromTo() {
    AST ast = getFactory().getAst();
    BooleanLiteral literal = ast.newBooleanLiteral(false);
    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod("getConfiguredAutoCheckFromTo")
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(ast.newPrimitiveType(PrimitiveType.BOOLEAN))
        .withBody(body)
        .in(get())
        .insert();

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(literal), true, AstNodeFactory.AUTO_CHECK_GROUP);
      links.addLinkedPositionProposal(AstNodeFactory.AUTO_CHECK_GROUP, Boolean.FALSE.toString());
      links.addLinkedPositionProposal(AstNodeFactory.AUTO_CHECK_GROUP, Boolean.TRUE.toString());
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.ISequenceBox);
    }
  }
}