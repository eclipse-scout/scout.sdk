/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;

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
    var ast = getFactory().getAst();

    var literal = ast.newNumberLiteral("128");
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(getFactory().getScoutApi().AbstractStringField().getConfiguredMaxLengthMethodName())
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(ast.newPrimitiveType(PrimitiveType.INT))
        .withBody(body)
        .in(get())
        .insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(literal), true, AstNodeFactory.MAX_LEN_GROUP);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().IStringField().fqn());
    }
  }
}
