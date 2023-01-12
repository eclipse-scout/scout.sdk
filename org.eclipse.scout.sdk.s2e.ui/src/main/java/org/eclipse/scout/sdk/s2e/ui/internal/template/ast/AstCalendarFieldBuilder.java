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
    var ast = getFactory().getAst();

    // calc super type
    var parameterizedType = ast.newParameterizedType(getSuperType());
    var selfQualifier = ast.newSimpleType(ast.newSimpleName(getTypeName() + getReadOnlySuffix()));
    var calTypeArg = ast.newQualifiedType(selfQualifier, ast.newSimpleName(ISdkConstants.INNER_CALENDAR_TYPE_NAME));
    parameterizedType.typeArguments().add(calTypeArg);
    withSuperType(parameterizedType);

    super.insert();

    var createdCalendarField = get();

    // getConfiguredGridH
    getFactory().newGetConfiguredGridH(10)
        .in(get())
        .insert();

    // getConfiguredLabelVisible
    getFactory().newGetConfiguredLabelVisible()
        .in(get())
        .insert();

    // inner calendar
    var calSuperType = getFactory().newTypeReference(getFactory().getScoutApi().AbstractCalendar().fqn());
    var calDeclaration = getFactory().newType(ISdkConstants.INNER_CALENDAR_TYPE_NAME)
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
    var providerSuperType = getFactory().newTypeReference(getFactory().getScoutApi().AbstractCalendarItemProvider().fqn());
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

    var links = getFactory().getLinkedPositionHolder();
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
