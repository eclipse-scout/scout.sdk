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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.signature.Signature;

/**
 * <h3>{@link AstBigDecimalFieldBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AstBigDecimalFieldBuilder extends AstTypeBuilder<AstBigDecimalFieldBuilder> {

  protected AstBigDecimalFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstBigDecimalFieldBuilder insert() {
    super.insert();

    addGetConfigured("getConfiguredMinValue", "-9999999999999999999", AstNodeFactory.MIN_GROUP, get());
    addGetConfigured("getConfiguredMaxValue", "9999999999999999999", AstNodeFactory.MAX_GROUP, get());

    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfigured(String name, String value, String group, TypeDeclaration newFormField) {
    AST ast = getFactory().getAst();

    Type bigDecimalType = getFactory().newTypeReference(IJavaRuntimeTypes.java_math_BigDecimal);

    StringLiteral constrArg = ast.newStringLiteral();
    constrArg.setLiteralValue(value);

    ClassInstanceCreation classInstanceCreation = ast.newClassInstanceCreation();
    classInstanceCreation.setType(ast.newSimpleType(ast.newSimpleName(Signature.getSimpleName(IJavaRuntimeTypes.java_math_BigDecimal))));
    classInstanceCreation.arguments().add(constrArg);

    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(classInstanceCreation);

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(bigDecimalType)
        .withBody(body)
        .in(newFormField)
        .insert();

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition importPos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(constrArg), 1, -2);
      links.addLinkedPosition(importPos, true, group);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IBigDecimalField);
    }
  }
}
