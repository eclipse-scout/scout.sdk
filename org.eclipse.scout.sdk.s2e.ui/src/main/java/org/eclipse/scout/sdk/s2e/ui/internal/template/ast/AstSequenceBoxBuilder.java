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
    var ast = getFactory().getAst();
    var literal = ast.newBooleanLiteral(false);
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(getFactory().getScoutApi().AbstractSequenceBox().getConfiguredAutoCheckFromToMethodName())
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(ast.newPrimitiveType(PrimitiveType.BOOLEAN))
        .withBody(body)
        .in(get())
        .insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(literal), true, AstNodeFactory.AUTO_CHECK_GROUP);
      links.addLinkedPositionProposalsBoolean(AstNodeFactory.AUTO_CHECK_GROUP);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ISequenceBox().fqn());
    }
  }
}
