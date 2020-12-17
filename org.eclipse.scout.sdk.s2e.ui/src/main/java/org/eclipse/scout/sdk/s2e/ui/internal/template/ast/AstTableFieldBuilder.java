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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.ISdkConstants;

/**
 * <h3>{@link AstTableFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstTableFieldBuilder extends AstTypeBuilder<AstTableFieldBuilder> {

  private TypeDeclaration m_tableDeclaration;

  protected AstTableFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  @SuppressWarnings("unchecked")
  public AstTableFieldBuilder insert() {
    var ast = getFactory().getAst();

    // super type
    var parameterizedType = ast.newParameterizedType(getSuperType());
    var selfQualifier = ast.newSimpleType(ast.newSimpleName(getTypeName() + getReadOnlySuffix()));
    var tableTypeArg = ast.newQualifiedType(selfQualifier, ast.newSimpleName(ISdkConstants.INNER_TABLE_TYPE_NAME));
    parameterizedType.typeArguments().add(tableTypeArg);
    withSuperType(parameterizedType);

    super.insert();

    getFactory().newGetConfiguredGridH(6)
        .in(get())
        .insert();

    // inner table
    var tableSuperType = getFactory().newTypeReference(getFactory().getScoutApi().AbstractTable().fqn());
    m_tableDeclaration = getFactory().newType(ISdkConstants.INNER_TABLE_TYPE_NAME)
        .withCalculatedOrder(false)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withNlsMethod(null)
        .withOrder(false)
        .withCreateLinks(false)
        .withOrderDefinitionType(null)
        .withReadOnlyNameSuffix(null)
        .withSuperType(tableSuperType)
        .in(get())
        .insert()
        .get();

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition typeNamePosInGeneric = new WrappedTrackedNodePosition(getFactory().getRewrite().track(selfQualifier), 0, -getReadOnlySuffix().length());
      links.addLinkedPosition(typeNamePosInGeneric, false, AstNodeFactory.TYPE_NAME_GROUP);
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ITableField().fqn());
    }

    return this;
  }

  public TypeDeclaration getCreatedTable() {
    return m_tableDeclaration;
  }
}
