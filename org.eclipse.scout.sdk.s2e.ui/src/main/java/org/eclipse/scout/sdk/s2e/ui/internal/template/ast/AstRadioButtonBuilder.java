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

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstRadioButtonBuilder}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public class AstRadioButtonBuilder extends AstTypeBuilder<AstRadioButtonBuilder> {

  @SuppressWarnings("PublicStaticCollectionField")
  public static final Set<String> PROPOSAL_RADIO_DATA_TYPES = new ConcurrentSkipListSet<>();

  static {
    PROPOSAL_RADIO_DATA_TYPES.add(JavaTypes.Long);
    PROPOSAL_RADIO_DATA_TYPES.add(JavaTypes.Boolean);
    PROPOSAL_RADIO_DATA_TYPES.add(BigDecimal.class.getName());
  }

  protected AstRadioButtonBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstRadioButtonBuilder insert() {

    // calc value type from surrounding group
    var genericFromRadioButtonGroupFqn = parseValueTypeTypeFromGroup();
    var genericFromRadioButtonGroupType = getFactory().newTypeReference(genericFromRadioButtonGroupFqn);

    // set type arg
    var parameterizedType = getFactory().getAst().newParameterizedType(getSuperType());
    parameterizedType.typeArguments().add(genericFromRadioButtonGroupType);
    withSuperType(parameterizedType);

    super.insert();

    addGetConfiguredRadioValue(genericFromRadioButtonGroupFqn);

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      var dataTypeTracker = getFactory().getRewrite().track(genericFromRadioButtonGroupType);
      links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.RADIO_VALUE_TYPE_GROUP);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().IRadioButton().fqn());

      var proposalTypes = PROPOSAL_RADIO_DATA_TYPES.toArray(new String[0]);
      for (var fqn : proposalTypes) {
        var typeBinding = getFactory().resolveTypeBinding(fqn);
        if (typeBinding != null) {
          links.addLinkedPositionProposal(AstNodeFactory.RADIO_VALUE_TYPE_GROUP, typeBinding);
        }
      }
    }

    return this;
  }

  protected String parseValueTypeTypeFromGroup() {
    var typeBinding = Ensure.notNull(AstUtils.getTypeBinding(getDeclaringType()));
    //noinspection resource
    var scoutType = getFactory().getScoutElementProvider().toScoutType(typeBinding);
    var iValueField = getFactory().getScoutApi().IValueField();
    return scoutType.resolveTypeParamValue(iValueField.valueTypeParamIndex(), iValueField.fqn())
        .flatMap(Stream::findFirst)
        .map(org.eclipse.scout.sdk.core.model.api.IType::name)
        .orElse(Object.class.getName());
  }

  protected void addGetConfiguredRadioValue(String valueTypeFqn) {
    var ast = getFactory().getAst();
    var initValue = getFactory().newDefaultValueExpression(valueTypeFqn, false);
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(initValue);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    var simpleDataType = getFactory().newTypeReference(valueTypeFqn);
    getFactory().newMethod(getFactory().getScoutApi().AbstractRadioButton().getConfiguredRadioValueMethodName())
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(simpleDataType)
        .withBody(body)
        .in(get())
        .insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(simpleDataType), false, AstNodeFactory.RADIO_VALUE_TYPE_GROUP);
      links.addLinkedPosition(getFactory().getRewrite().track(initValue), true, AstNodeFactory.RADIO_VALUE_GROUP);
    }
  }
}
