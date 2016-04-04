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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstRadioButtonBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public class AstRadioButtonBuilder extends AstTypeBuilder<AstRadioButtonBuilder> {

  public static final Set<String> PROPOSAL_RADIO_DATA_TYPES = Collections.synchronizedSortedSet(new TreeSet<String>());

  static {
    PROPOSAL_RADIO_DATA_TYPES.add(IJavaRuntimeTypes.Long);
    PROPOSAL_RADIO_DATA_TYPES.add(IJavaRuntimeTypes.Boolean);
    PROPOSAL_RADIO_DATA_TYPES.add(IJavaRuntimeTypes.BigDecimal);
  }

  protected AstRadioButtonBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstRadioButtonBuilder insert() {

    // calc value type from surrounding group
    String genericFromRadioButtonGroupFqn = parseValueTypeTypeFromGroup();
    Type genericFromRadioButtonGroupType = getFactory().newTypeReference(genericFromRadioButtonGroupFqn);

    // set type arg
    ParameterizedType parameterizedType = getFactory().getAst().newParameterizedType(getSuperType());
    parameterizedType.typeArguments().add(genericFromRadioButtonGroupType);
    withSuperType(parameterizedType);

    super.insert();

    addGetConfiguredRadioValue(genericFromRadioButtonGroupFqn);

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition dataTypeTracker = getFactory().getRewrite().track(genericFromRadioButtonGroupType);
      links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.RADIO_VALUE_TYPE_GROUP);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IRadioButton);

      String[] proposalTypes = PROPOSAL_RADIO_DATA_TYPES.toArray(new String[PROPOSAL_RADIO_DATA_TYPES.size()]);
      for (String fqn : proposalTypes) {
        ITypeBinding typeBinding = getFactory().resolveTypeBinding(fqn);
        if (typeBinding != null) {
          links.addLinkedPositionProposal(AstNodeFactory.RADIO_VALUE_TYPE_GROUP, typeBinding);
        }
      }
    }

    return this;
  }

  protected String parseValueTypeTypeFromGroup() {
    IType typeBinding = Validate.notNull(AstUtils.getTypeBinding(getDeclaringType()));
    org.eclipse.scout.sdk.core.model.api.IType scoutType = S2eUtils.jdtTypeToScoutType(typeBinding, getFactory().getJavaEnvironment());
    List<org.eclipse.scout.sdk.core.model.api.IType> valueSignatures = CoreUtils.getResolvedTypeParamValue(scoutType, IScoutRuntimeTypes.IValueField, IScoutRuntimeTypes.TYPE_PARAM_VALUEFIELD__VALUE);
    if (valueSignatures.isEmpty()) {
      return IJavaRuntimeTypes.Object;
    }
    return valueSignatures.get(0).name();
  }

  protected void addGetConfiguredRadioValue(String valueTypeFqn) {
    AST ast = getFactory().getAst();
    Expression initValue = getFactory().newDefaultValueExpression(valueTypeFqn, false);
    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(initValue);

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    Type simpleDataType = getFactory().newTypeReference(valueTypeFqn);
    getFactory().newMethod("getConfiguredRadioValue")
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(simpleDataType)
        .withBody(body)
        .in(get())
        .insert();

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPosition(getFactory().getRewrite().track(simpleDataType), false, AstNodeFactory.RADIO_VALUE_TYPE_GROUP);
      links.addLinkedPosition(getFactory().getRewrite().track(initValue), true, AstNodeFactory.RADIO_VALUE_GROUP);
    }
  }
}
