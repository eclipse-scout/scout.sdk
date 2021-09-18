/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
  @SuppressWarnings({"PublicStaticCollectionField", "StaticCollection"})
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
    var genericFromCodeTypeFqn = parseCodeIdTypeFromCodeType();
    var genericFromCodeType = getFactory().newTypeReference(genericFromCodeTypeFqn);

    applyTypeArgToSuperType(genericFromCodeType);

    var codeType = super.insert().get();

    // serialVersionUID
    codeType.bodyDeclarations().add(0, getFactory().newSerialVersionUid());

    // ID
    codeType.bodyDeclarations().add(1, createId(genericFromCodeTypeFqn));

    // getId
    var getId = createGetId(genericFromCodeTypeFqn);
    codeType.bodyDeclarations().add(getId);

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      var dataTypeTracker = getFactory().getRewrite().track(genericFromCodeType);
      links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.CODE_DATA_TYPE_GROUP);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ICode().fqn());

      var typeBinding = getFactory().resolveTypeBinding(genericFromCodeTypeFqn);
      if (typeBinding != null) {
        links.addLinkedPositionProposal(AstNodeFactory.CODE_DATA_TYPE_GROUP, typeBinding);
      }
      var proposalTypes = PROPOSAL_CODE_DATA_TYPES.toArray(new String[0]);
      for (var fqn : proposalTypes) {
        typeBinding = getFactory().resolveTypeBinding(fqn);
        if (typeBinding != null) {
          links.addLinkedPositionProposal(AstNodeFactory.CODE_DATA_TYPE_GROUP, typeBinding);
        }
      }
    }

    return this;
  }

  protected void applyTypeArgToSuperType(Type genericFromCodeType) {
    var parameterizedType = getFactory().getAst().newParameterizedType(getSuperType());
    parameterizedType.typeArguments().add(genericFromCodeType);
    withSuperType(parameterizedType);
  }

  protected TypeDeclaration getDeclaringCodeType() {
    var parentTypes = AstUtils.getDeclaringTypes(getDeclaringType());
    return parentTypes.getLast();
  }

  protected String parseCodeIdTypeFromCodeType() {
    var codeType = getDeclaringCodeType();
    var typeBinding = Ensure.notNull(AstUtils.getTypeBinding(codeType));
    var scoutType = getFactory().getScoutElementProvider().toScoutType(typeBinding);
    var iCodeType = getFactory().getScoutApi().ICodeType();
    return scoutType.resolveTypeParamValue(iCodeType.codeIdTypeParamIndex(), iCodeType.fqn())
        .flatMap(Stream::findFirst)
        .map(org.eclipse.scout.sdk.core.model.api.IType::name)
        .orElse(JavaTypes.Integer);
  }

  protected FieldDeclaration createId(String codeIdTypeFqn) {
    var ast = getFactory().getAst();

    var dataType = calcIdDataType(codeIdTypeFqn);

    var initValue = getFactory().newDefaultValueExpression(dataType.toString(), true);
    var fragment = ast.newVariableDeclarationFragment();
    fragment.setName(ast.newSimpleName(ID_FIELD_NAME));
    fragment.setInitializer(initValue);

    var declaration = ast.newFieldDeclaration(fragment);

    declaration.setType(dataType);
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      var dataTypeTracker = getFactory().getRewrite().track(dataType);
      links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.ID_DATA_TYPE_GROUP);
      var valueTracker = getFactory().getRewrite().track(initValue);
      links.addLinkedPosition(valueTracker, true, AstNodeFactory.ID_VALUE_GROUP);
    }

    return declaration;
  }

  protected Type calcIdDataType(String codeIdTypeFqn) {
    var primitive = JavaTypes.unboxToPrimitive(codeIdTypeFqn);
    if (JavaTypes.isPrimitive(primitive)) {
      return getFactory().getAst().newPrimitiveType(PrimitiveType.toCode(primitive));
    }
    return getFactory().newTypeReference(codeIdTypeFqn);
  }

  protected MethodDeclaration createGetId(String codeIdTypeFqn) {
    var ast = getFactory().getAst();
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(ast.newSimpleName(ID_FIELD_NAME));

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    var getId = ast.newMethodDeclaration();
    getId.setConstructor(false);
    getId.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    getId.setName(ast.newSimpleName(getFactory().getScoutApi().ICode().getIdMethodName()));

    var simpleDataType = getFactory().newTypeReference(codeIdTypeFqn);
    getId.setReturnType2(simpleDataType);

    getId.setBody(body);

    AstUtils.addAnnotationTo(getFactory().newOverrideAnnotation(), getId);

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      var dataTypeTracker = getFactory().getRewrite().track(simpleDataType);
      links.addLinkedPosition(dataTypeTracker, false, AstNodeFactory.CODE_DATA_TYPE_GROUP);
    }

    return getId;
  }
}
