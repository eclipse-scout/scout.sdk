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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.structured.StructuredType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstInnerTypeGetterBuilder}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public class AstInnerTypeGetterBuilder extends AstMethodBuilder<AstInnerTypeGetterBuilder> {

  private String m_innerTypeFindMethodName;
  private Expression m_leftHandSideExpression;
  private String m_readOnlyPrefix;
  private String m_readOnlySuffix;
  private SimpleName m_typeLiteralName;

  protected AstInnerTypeGetterBuilder(AstNodeFactory owner) {
    super(owner);
  }

  public AstInnerTypeGetterBuilder withMethodNameToFindInnerType(String innerTypeFindMethodName) {
    m_innerTypeFindMethodName = innerTypeFindMethodName;
    return this;
  }

  public AstInnerTypeGetterBuilder withReadOnlySuffix(String suffix) {
    m_readOnlySuffix = suffix;
    return this;
  }

  public AstInnerTypeGetterBuilder withReadOnlyPrefix(String prefix) {
    m_readOnlyPrefix = prefix;
    return this;
  }

  public AstInnerTypeGetterBuilder withMethodToFindInnerTypeExpression(Expression methodToFindInnerTypeExp) {
    m_leftHandSideExpression = methodToFindInnerTypeExp;
    return this;
  }

  public String getReadOnlyPrefix() {
    return m_readOnlyPrefix;
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  public String getMethodNameToFinderInnerType() {
    return m_innerTypeFindMethodName;
  }

  public Expression getMethodToFindInnerTypeExpression() {
    return m_leftHandSideExpression;
  }

  @Override
  public String getMethodName() {
    return getReadOnlyPrefix() + super.getMethodName() + getReadOnlySuffix();
  }

  protected Block getInnerTypeGetterBody() {
    var ast = getFactory().getAst();
    var fieldClass = ast.newTypeLiteral();
    m_typeLiteralName = ast.newSimpleName(super.getMethodName() + getReadOnlySuffix());
    fieldClass.setType(ast.newSimpleType(m_typeLiteralName));

    var get = ast.newMethodInvocation();
    get.setName(ast.newSimpleName(m_innerTypeFindMethodName));
    get.arguments().add(fieldClass);
    if (m_leftHandSideExpression != null) {
      get.setExpression(m_leftHandSideExpression);
    }

    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(get);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    return body;
  }

  @Override
  public AstInnerTypeGetterBuilder insert() {
    // init defaults
    if (getModifiers().isEmpty()) {
      withModifiers(ModifierKeyword.PUBLIC_KEYWORD);
    }
    if (getReadOnlyPrefix() == null) {
      withReadOnlyPrefix(PropertyBean.GETTER_PREFIX);
    }
    if (getReadOnlySuffix() == null) {
      withReadOnlySuffix("");
    }
    if (getBody() == null) {
      withBody(getInnerTypeGetterBody());
    }
    Ensure.notNull(getMethodNameToFinderInnerType());
    super.insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition methodPos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(get().getName()), getReadOnlyPrefix().length(), -getReadOnlyPrefix().length() - getReadOnlySuffix().length());
      ASTNode returnTypeNode = getReturnType();
      if (returnTypeNode instanceof QualifiedType t) {
        returnTypeNode = t.getName();
      }
      ITrackedNodePosition returnNamePos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(returnTypeNode), 0, -getReadOnlySuffix().length());
      ITrackedNodePosition typeLiteralPos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(m_typeLiteralName), 0, -getReadOnlySuffix().length());

      links.addLinkedPosition(returnNamePos, false, AstNodeFactory.TYPE_NAME_GROUP);
      links.addLinkedPosition(methodPos, false, AstNodeFactory.TYPE_NAME_GROUP);
      links.addLinkedPosition(typeLiteralPos, false, AstNodeFactory.TYPE_NAME_GROUP);
    }

    return this;
  }

  @Override
  protected void insertMethod() {
    var methodSibling = getSiblingForGetter(get().getName().getIdentifier(), getDeclaringType(), getFactory().getScoutElementProvider());
    var rewrite = getFactory().getRewrite().getListRewrite(getDeclaringType(), getDeclaringType().getBodyDeclarationsProperty());
    if (methodSibling == null) {
      List<?> originalList = rewrite.getOriginalList();
      if (originalList.isEmpty()) {
        rewrite.insertFirst(get(), null);
      }
      else {
        rewrite.insertAfter(get(), (ASTNode) originalList.get(originalList.size() - 1), null);
      }
    }
    else {
      rewrite.insertBefore(get(), methodSibling, null);
    }
  }

  protected static ASTNode getSiblingForGetter(String getterName, TypeDeclaration declaringType, EclipseEnvironment scoutElementProvider) {
    var t = Ensure.notNull(AstUtils.getTypeBinding(declaringType));

    var jdtTypeToScoutType = scoutElementProvider.toScoutType(t);
    var structuredForm = StructuredType.of(jdtTypeToScoutType);
    var methodSibling = structuredForm.getSiblingMethodFieldGetter(getterName);
    if (methodSibling == null) {
      return null;
    }

    if (methodSibling instanceof IMethod) {
      var sigOfSiblingMethod = ((IMethod) methodSibling).identifier(true);
      return Arrays.stream(declaringType.getMethods())
          .filter(methodDeclaration -> sigOfSiblingMethod.equals(AstUtils.createMethodIdentifier(methodDeclaration)))
          .findFirst()
          .orElse(null);
    }
    else if (methodSibling instanceof IField) {
      var elemName = methodSibling.elementName();
      for (var field : declaringType.getFields()) {
        List<VariableDeclarationFragment> fragments = field.fragments();
        for (var fragment : fragments) {
          if (elemName.equals(fragment.getName().getIdentifier())) {
            return field;
          }
        }
      }
    }
    else if (methodSibling instanceof org.eclipse.scout.sdk.core.model.api.IType) {
      var elemName = methodSibling.elementName();
      return Arrays.stream(declaringType.getTypes())
          .filter(innerType -> elemName.equals(innerType.getName().getIdentifier()))
          .findFirst()
          .orElse(null);
    }
    return null;
  }
}
