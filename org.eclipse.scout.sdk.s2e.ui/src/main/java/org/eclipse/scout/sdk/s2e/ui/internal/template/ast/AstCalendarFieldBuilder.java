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
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.ISdkConstants;

/**
 * <h3>{@link AstCalendarFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstCalendarFieldBuilder extends AstTypeBuilder<AstCalendarFieldBuilder> {

  private TypeDeclaration m_providerDeclaration;

  protected AstCalendarFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  @SuppressWarnings("unchecked")
  public AstCalendarFieldBuilder insert() {
    AST ast = getFactory().getAst();

    // calc super type
    ParameterizedType parameterizedType = ast.newParameterizedType(getSuperType());
    SimpleType selfQualifier = ast.newSimpleType(ast.newSimpleName(getTypeName() + getReadOnlySuffix()));
    QualifiedType calTypeArg = ast.newQualifiedType(selfQualifier, ast.newSimpleName(ISdkConstants.INNER_CALENDAR_TYPE_NAME));
    parameterizedType.typeArguments().add(calTypeArg);
    withSuperType(parameterizedType);

    super.insert();

    TypeDeclaration createdCalendarField = get();

    // getConfiguredGridH
    getFactory().newGetConfiguredGridH(10)
        .in(get())
        .insert();

    // getConfiguredLabelVisible
    getFactory().newGetConfiguredLabelVisible()
        .in(get())
        .insert();

    // inner calendar
    Type calSuperType = getFactory().newTypeReference(getFactory().getScoutApi().AbstractCalendar().fqn());
    TypeDeclaration calDeclaration = getFactory().newType(ISdkConstants.INNER_CALENDAR_TYPE_NAME)
        .withCalculatedOrder(false)
        .withCreateLinks(false)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withNlsMethod(null)
        .withOrder(false)
        .withOrderDefinitionType(null)
        .withReadOnlyNameSuffix(null)
        .withSuperType(calSuperType)
        .in(createdCalendarField)
        .insert()
        .get();

    // inner provider
    Type providerSuperType = getFactory().newTypeReference(getFactory().getScoutApi().AbstractCalendarItemProvider().fqn());
    m_providerDeclaration = getFactory().newType("MyCalendarItem")
        .withCalculatedOrder(false)
        .withCreateLinks(false)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withNlsMethod(null)
        .withOrder(true)
        .withOrderDefinitionType(null)
        .withReadOnlyNameSuffix(ISdkConstants.SUFFIX_CALENDAR_ITEM_PROVIDER)
        .withSuperType(providerSuperType)
        .in(calDeclaration)
        .insert()
        .get();

    calDeclaration.bodyDeclarations().add(m_providerDeclaration);
    createdCalendarField.bodyDeclarations().add(calDeclaration);

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition providerTypeNameTracker = new WrappedTrackedNodePosition(getFactory().getRewrite().track(m_providerDeclaration.getName()), 0, -ISdkConstants.SUFFIX_CALENDAR_ITEM_PROVIDER.length());
      links.addLinkedPosition(providerTypeNameTracker, true, AstNodeFactory.CALENDAR_ITEM_PROVIDER_NAME_GROUP);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ICalendarField().fqn());

      ITrackedNodePosition typeNamePosInGeneric = new WrappedTrackedNodePosition(getFactory().getRewrite().track(selfQualifier), 0, -getReadOnlySuffix().length());
      links.addLinkedPosition(typeNamePosInGeneric, false, AstNodeFactory.TYPE_NAME_GROUP);
    }

    return this;
  }

  public TypeDeclaration getProviderTypeDeclaration() {
    return m_providerDeclaration;
  }
}
