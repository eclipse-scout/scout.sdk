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

import java.math.BigDecimal;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

/**
 * <h3>{@link AstBigDecimalFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstBigDecimalFieldBuilder extends AstTypeBuilder<AstBigDecimalFieldBuilder> {

  protected AstBigDecimalFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstBigDecimalFieldBuilder insert() {
    super.insert();
    var abstractNumberFieldApi = getFactory().getScoutApi().AbstractNumberField();
    addGetConfigured(abstractNumberFieldApi.getConfiguredMinValueMethodName(), "-9999999999999999999", AstNodeFactory.MIN_GROUP, get());
    addGetConfigured(abstractNumberFieldApi.getConfiguredMaxValueMethodName(), "9999999999999999999", AstNodeFactory.MAX_GROUP, get());
    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfigured(String name, String value, String group, TypeDeclaration newFormField) {
    var ast = getFactory().getAst();

    var bigDecimalType = getFactory().newTypeReference(BigDecimal.class.getName());

    var constrArg = ast.newStringLiteral();
    constrArg.setLiteralValue(value);

    var classInstanceCreation = ast.newClassInstanceCreation();
    classInstanceCreation.setType(ast.newSimpleType(ast.newSimpleName(BigDecimal.class.getSimpleName())));
    classInstanceCreation.arguments().add(constrArg);

    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(classInstanceCreation);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(bigDecimalType)
        .withBody(body)
        .in(newFormField)
        .insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition importPos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(constrArg), 1, -2);
      links.addLinkedPosition(importPos, true, group);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().IBigDecimalField().fqn());
    }
  }
}
