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

import java.util.Deque;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstColumnBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AstColumnBuilder extends AstTypeBuilder<AstColumnBuilder> {

  protected AstColumnBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstColumnBuilder insert() {

    super.insert();

    // getConfiguredWidth
    getFactory().newGetConfiguredWidth(100)
        .in(get())
        .insert();

    // column getter
    addColumnGetter();

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IColumn);
    }

    return this;
  }

  protected static boolean hasTypeArgsInDeclaringTypes(Deque<TypeDeclaration> declaringTypes) {
    for (TypeDeclaration td : declaringTypes) {
      if (!td.typeParameters().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  protected void addColumnGetter() {
    MethodInvocation getColumnSet = getFactory().getAst().newMethodInvocation();
    getColumnSet.setName(getFactory().getAst().newSimpleName("getColumnSet"));

    if (AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), IScoutRuntimeTypes.IExtension)) {
      // column in table extension
      MethodInvocation getOwner = getFactory().getAst().newMethodInvocation();
      getOwner.setName(getFactory().getAst().newSimpleName("getOwner"));
      getColumnSet.setExpression(getOwner);
    }

    Type columnGetterReturnType = null;
    Deque<TypeDeclaration> declaringTypes = AstUtils.getDeclaringTypes(getDeclaringType());
    SimpleName columnSimpleName = getFactory().getAst().newSimpleName(getTypeName() + getReadOnlySuffix());

    // return type with type-parameters in the declaring types
    if (hasTypeArgsInDeclaringTypes(declaringTypes)) {
      Type type = null;
      Iterator<TypeDeclaration> topDownIterator = declaringTypes.descendingIterator();
      while (topDownIterator.hasNext()) {
        type = wrapParameterizedIfRequired(topDownIterator.next(), type);
      }
      if (type != null) {
        columnGetterReturnType = getFactory().getAst().newQualifiedType(type, columnSimpleName);
      }
    }

    // return type without type-parameters in the declaring types (default case)
    if (columnGetterReturnType == null) {
      columnGetterReturnType = getFactory().getAst().newSimpleType(columnSimpleName);
    }

    getFactory().newInnerTypeGetter()
        .withMethodNameToFindInnerType("getColumnByClass")
        .withMethodToFindInnerTypeExpression(getColumnSet)
        .withName(getTypeName())
        .withReadOnlySuffix(getReadOnlySuffix())
        .withReturnType(columnGetterReturnType)
        .in(getDeclaringType())
        .insert();
  }

  @SuppressWarnings("unchecked")
  protected Type wrapParameterizedIfRequired(TypeDeclaration template, Type type) {
    SimpleName sn = getFactory().getAst().newSimpleName(template.getName().getIdentifier());
    if (type == null) {
      type = getFactory().getAst().newSimpleType(sn);
    }
    else {
      type = getFactory().getAst().newQualifiedType(type, sn);
    }

    if (template.typeParameters().isEmpty()) {
      return type;
    }

    ParameterizedType parameterizedType = getFactory().getAst().newParameterizedType(type);
    for (int i = 0; i < template.typeParameters().size(); i++) {
      parameterizedType.typeArguments().add(getFactory().getAst().newWildcardType());
    }
    return parameterizedType;
  }
}
