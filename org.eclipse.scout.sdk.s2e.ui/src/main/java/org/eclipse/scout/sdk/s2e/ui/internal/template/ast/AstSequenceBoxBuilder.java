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
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;

/**
 * <h3>{@link AstSequenceBoxBuilder}</h3>
 *
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

    getFactory().newMethod(getFactory().getScoutApi().AbstractSequenceBox().getConfiguredAutoCheckFromToMethodName())
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
      links.addLinkedPositionProposalsBoolean(AstNodeFactory.AUTO_CHECK_GROUP);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ISequenceBox().fqn());
    }
  }
}
