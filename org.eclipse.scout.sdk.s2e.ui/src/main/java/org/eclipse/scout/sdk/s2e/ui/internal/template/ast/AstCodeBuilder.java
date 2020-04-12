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
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstCodeBuilder}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public class AstCodeBuilder extends AstTypeBuilder<AstCodeBuilder> {

  public static final String ID_FIELD_NAME = "ID";
  public static final Set<String> PROPOSAL_CODE_DATA_TYPES = new ConcurrentSkipListSet<>();

  static {
    PROPOSAL_CODE_DATA_TYPES.add(JavaTypes.Long);
    PROPOSAL_CODE_DATA_TYPES.add(JavaTypes.Integer);
    PROPOSAL_CODE_DATA_TYPES.add(String.class.getName());
    PROPOSAL_CODE_DATA_TYPES.add(JavaTypes.Boolean);
    PROPOSAL_CODE_DATA_TYPES.add(BigDecimal.class.getName());
  }

  protected AstCodeBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstCodeBuilder insert() {
    // calc code_id type
    String genericFromCodeTypeFqn = parseCodeIdTypeFromCodeType();
    Type genericFromCodeType = getFactory().newTypeReference(genericFromCodeTypeFqn);

    applyTypeArgToSuperType(genericFromCodeType);

    TypeDeclaration codeType = super.insert().get();

    // serialVersionUID
    codeType.bodyDeclarations().add(0, getFactory().newSerialVersionUid());

    // ID
    codeType.bodyDeclarations().add(1, createId(genericFromCodeTypeFqn));

    // getId
    MethodDeclaration getId = createGetId(genericFromCodeTypeFqn);
    codeType.bodyDeclarations().add(getId);

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition dataTypeTracker = getFactory().getRewrite().track(genericFromCodeType);
      links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.CODE_DATA_TYPE_GROUP);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.ICode);

      ITypeBinding typeBinding = getFactory().resolveTypeBinding(genericFromCodeTypeFqn);
      if (typeBinding != null) {
        links.addLinkedPositionProposal(AstNodeFactory.CODE_DATA_TYPE_GROUP, typeBinding);
      }
      String[] proposalTypes = PROPOSAL_CODE_DATA_TYPES.toArray(new String[0]);
      for (String fqn : proposalTypes) {
        typeBinding = getFactory().resolveTypeBinding(fqn);
        if (typeBinding != null) {
          links.addLinkedPositionProposal(AstNodeFactory.CODE_DATA_TYPE_GROUP, typeBinding);
        }
      }
    }

    return this;
  }

  protected void applyTypeArgToSuperType(Type genericFromCodeType) {
    ParameterizedType parameterizedType = getFactory().getAst().newParameterizedType(getSuperType());
    parameterizedType.typeArguments().add(genericFromCodeType);
    withSuperType(parameterizedType);
  }

  protected TypeDeclaration getDeclaringCodeType() {
    Deque<TypeDeclaration> parentTypes = AstUtils.getDeclaringTypes(getDeclaringType());
    return parentTypes.getLast();
  }

  protected String parseCodeIdTypeFromCodeType() {
    TypeDeclaration codeType = getDeclaringCodeType();
    IType typeBinding = Ensure.notNull(AstUtils.getTypeBinding(codeType));
    org.eclipse.scout.sdk.core.model.api.IType scoutType = getFactory().getScoutElementProvider().toScoutType(typeBinding);
    return scoutType.resolveTypeParamValue(IScoutRuntimeTypes.TYPE_PARAM_CODETYPE__CODE_ID, IScoutRuntimeTypes.ICodeType)
        .flatMap(Stream::findFirst)
        .map(org.eclipse.scout.sdk.core.model.api.IType::name)
        .orElse(JavaTypes.Integer);
  }

  protected FieldDeclaration createId(String codeIdTypeFqn) {
    AST ast = getFactory().getAst();

    Type dataType = calcIdDataType(codeIdTypeFqn);

    Expression initValue = getFactory().newDefaultValueExpression(dataType.toString(), true);
    VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
    fragment.setName(ast.newSimpleName(ID_FIELD_NAME));
    fragment.setInitializer(initValue);

    FieldDeclaration declaration = ast.newFieldDeclaration(fragment);

    declaration.setType(dataType);
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition dataTypeTracker = getFactory().getRewrite().track(dataType);
      links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.ID_DATA_TYPE_GROUP);
      ITrackedNodePosition valueTracker = getFactory().getRewrite().track(initValue);
      links.addLinkedPosition(valueTracker, true, AstNodeFactory.ID_VALUE_GROUP);
    }

    return declaration;
  }

  protected Type calcIdDataType(String codeIdTypeFqn) {
    String primitive = JavaTypes.unboxToPrimitive(codeIdTypeFqn);
    if (JavaTypes.isPrimitive(primitive)) {
      return getFactory().getAst().newPrimitiveType(PrimitiveType.toCode(primitive));
    }
    return getFactory().newTypeReference(codeIdTypeFqn);
  }

  protected MethodDeclaration createGetId(String codeIdTypeFqn) {
    AST ast = getFactory().getAst();
    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(ast.newSimpleName(ID_FIELD_NAME));

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    MethodDeclaration getId = ast.newMethodDeclaration();
    getId.setConstructor(false);
    getId.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    getId.setName(ast.newSimpleName("getId"));

    Type simpleDataType = getFactory().newTypeReference(codeIdTypeFqn);
    getId.setReturnType2(simpleDataType);

    getId.setBody(body);

    AstUtils.addAnnotationTo(getFactory().newOverrideAnnotation(), getId);

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition dataTypeTracker = getFactory().getRewrite().track(simpleDataType);
      links.addLinkedPosition(dataTypeTracker, false, AstNodeFactory.CODE_DATA_TYPE_GROUP);
    }

    return getId;
  }
}
