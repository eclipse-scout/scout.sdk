/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * <h3>{@link AstImageFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstImageFieldBuilder extends AstTypeBuilder<AstImageFieldBuilder> {

  protected AstImageFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstImageFieldBuilder insert() {
    super.insert();

    addGetConfiguredAutoFit(getFactory().getScoutApi().AbstractImageField().getConfiguredAutoFitMethodName(), get());

    getFactory().newGetConfiguredGridH(4)
        .in(get())
        .insert();

    getFactory().newGetConfiguredLabelVisible()
        .in(get())
        .insert();

    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfiguredAutoFit(String name, TypeDeclaration newFormField) {
    var ast = getFactory().getAst();

    var literal = ast.newBooleanLiteral(false);
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(ast.newPrimitiveType(PrimitiveType.BOOLEAN))
        .withBody(body)
        .in(newFormField)
        .insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(literal), true, AstNodeFactory.AUTO_FIT_GROUP);
      links.addLinkedPositionProposalsBoolean(AstNodeFactory.AUTO_FIT_GROUP);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().IImageField().fqn());
    }
  }
}
