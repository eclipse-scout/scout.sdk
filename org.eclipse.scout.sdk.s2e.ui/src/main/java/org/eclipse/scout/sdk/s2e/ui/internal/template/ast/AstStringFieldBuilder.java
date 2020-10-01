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
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;

/**
 * <h3>{@link AstStringFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstStringFieldBuilder extends AstTypeBuilder<AstStringFieldBuilder> {

  protected AstStringFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstStringFieldBuilder insert() {
    super.insert();

    addGetConfiguredMaxLength();

    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfiguredMaxLength() {
    AST ast = getFactory().getAst();

    NumberLiteral literal = ast.newNumberLiteral("128");
    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(getFactory().getScoutApi().AbstractStringField().getConfiguredMaxLengthMethodName())
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(ast.newPrimitiveType(PrimitiveType.INT))
        .withBody(body)
        .in(get())
        .insert();

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(literal), true, AstNodeFactory.MAX_LEN_GROUP);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().IStringField().fqn());
    }
  }
}
