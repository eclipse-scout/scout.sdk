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
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

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

    addGetConfiguredAutoFit("getConfiguredAutoFit", get());

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
    AST ast = getFactory().getAst();

    BooleanLiteral literal = ast.newBooleanLiteral(false);
    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(ast.newPrimitiveType(PrimitiveType.BOOLEAN))
        .withBody(body)
        .in(newFormField)
        .insert();

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(literal), true, AstNodeFactory.AUTO_FIT_GROUP);
      links.addLinkedPositionProposalsBoolean(AstNodeFactory.AUTO_FIT_GROUP);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IImageField);
    }
  }
}
