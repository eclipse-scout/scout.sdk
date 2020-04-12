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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

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
    AST ast = getFactory().getAst();

    String typeArgName = "OWNER";
    ParameterizedType parameterizedType = ast.newParameterizedType(getSuperType());
    SimpleType typeArg = ast.newSimpleType(ast.newName(typeArgName));
    parameterizedType.typeArguments().add(typeArg);
    withSuperType(parameterizedType);

    super.insert();

    // add constructor
    String argName = "owner";
    MethodDeclaration constructor = ast.newMethodDeclaration();
    constructor.setConstructor(true);
    SimpleName constrName = ast.newSimpleName(getTypeName() + getReadOnlySuffix());
    constructor.setName(constrName);
    constructor.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    SingleVariableDeclaration ownerArg = ast.newSingleVariableDeclaration();
    SimpleType constrArg = ast.newSimpleType(ast.newName(typeArgName));
    ownerArg.setType(constrArg);
    ownerArg.setName(ast.newSimpleName(argName));
    constructor.parameters().add(ownerArg);

    SuperConstructorInvocation superConstructorInvocation = ast.newSuperConstructorInvocation();
    superConstructorInvocation.arguments().add(ast.newSimpleName(argName));

    Block body = ast.newBlock();
    body.statements().add(superConstructorInvocation);

    constructor.setBody(body);

    get().bodyDeclarations().add(constructor);

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {

      ITrackedNodePosition typeNamePos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(constrName), 0, -getReadOnlySuffix().length());
      links.addLinkedPosition(typeNamePos, true, AstNodeFactory.TYPE_NAME_GROUP);

      links.addLinkedPosition(getFactory().getRewrite().track(typeArg), true, AstNodeFactory.VALUE_TYPE_GROUP);
      links.addLinkedPosition(getFactory().getRewrite().track(constrArg), false, AstNodeFactory.VALUE_TYPE_GROUP);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IExtension);
    }

    return this;
  }
}
