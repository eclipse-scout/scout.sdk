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
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;

/**
 * <h3>{@link AstTableFieldBuilder}</h3>
 *
 * @author Matthias Villiger
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
    AST ast = getFactory().getAst();
    SimpleType selfQualifier = null;

    // super type
    ParameterizedType parameterizedType = ast.newParameterizedType(getSuperType());
    selfQualifier = ast.newSimpleType(ast.newSimpleName(getTypeName() + getReadOnlySuffix()));
    QualifiedType tableTypeArg = ast.newQualifiedType(selfQualifier, ast.newSimpleName(ISdkProperties.INNER_TABLE_TYPE_NAME));
    parameterizedType.typeArguments().add(tableTypeArg);
    withSuperType(parameterizedType);

    super.insert();

    getFactory().newGetConfiguredGridH(6)
        .in(get())
        .insert();

    // inner calendar
    Type tableSuperType = getFactory().newTypeReference(IScoutRuntimeTypes.AbstractTable);
    m_tableDeclaration = getFactory().newType(ISdkProperties.INNER_TABLE_TYPE_NAME)
        .withCalculatedOrder(false)
        .withClassId(false)
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

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      if (selfQualifier != null) {
        ITrackedNodePosition typeNamePosInGeneric = new WrappedTrackedNodePosition(getFactory().getRewrite().track(selfQualifier), 0, -getReadOnlySuffix().length());
        links.addLinkedPosition(typeNamePosInGeneric, false, AstNodeFactory.TYPE_NAME_GROUP);
      }
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.ITableField);
    }

    return this;
  }

  public TypeDeclaration getCreatedTable() {
    return m_tableDeclaration;
  }
}
