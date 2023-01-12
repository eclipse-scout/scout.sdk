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
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

/**
 * <h3>{@link AstExtensionBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstExtensionBuilder extends AstTypeBuilder<AstExtensionBuilder> {

  protected AstExtensionBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  @SuppressWarnings("unchecked")
  public AstExtensionBuilder insert() {
    var ast = getFactory().getAst();

    var typeArgName = "OWNER";
    var parameterizedType = ast.newParameterizedType(getSuperType());
    var typeArg = ast.newSimpleType(ast.newName(typeArgName));
    parameterizedType.typeArguments().add(typeArg);
    withSuperType(parameterizedType);

    super.insert();

    // add constructor
    var argName = "owner";
    var constructor = ast.newMethodDeclaration();
    constructor.setConstructor(true);
    var constrName = ast.newSimpleName(getTypeName() + getReadOnlySuffix());
    constructor.setName(constrName);
    constructor.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    var ownerArg = ast.newSingleVariableDeclaration();
    var constrArg = ast.newSimpleType(ast.newName(typeArgName));
    ownerArg.setType(constrArg);
    ownerArg.setName(ast.newSimpleName(argName));
    constructor.parameters().add(ownerArg);

    var superConstructorInvocation = ast.newSuperConstructorInvocation();
    superConstructorInvocation.arguments().add(ast.newSimpleName(argName));

    var body = ast.newBlock();
    body.statements().add(superConstructorInvocation);

    constructor.setBody(body);

    get().bodyDeclarations().add(constructor);

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {

      ITrackedNodePosition typeNamePos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(constrName), 0, -getReadOnlySuffix().length());
      links.addLinkedPosition(typeNamePos, true, AstNodeFactory.TYPE_NAME_GROUP);

      links.addLinkedPosition(getFactory().getRewrite().track(typeArg), true, AstNodeFactory.VALUE_TYPE_GROUP);
      links.addLinkedPosition(getFactory().getRewrite().track(constrArg), false, AstNodeFactory.VALUE_TYPE_GROUP);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().IExtension().fqn());
    }

    return this;
  }
}
