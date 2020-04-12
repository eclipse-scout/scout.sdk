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

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.s.structured.IStructuredType;
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
    AST ast = getFactory().getAst();
    TypeLiteral fieldClass = ast.newTypeLiteral();
    m_typeLiteralName = ast.newSimpleName(super.getMethodName() + getReadOnlySuffix());
    fieldClass.setType(ast.newSimpleType(m_typeLiteralName));

    MethodInvocation get = ast.newMethodInvocation();
    get.setName(ast.newSimpleName(m_innerTypeFindMethodName));
    get.arguments().add(fieldClass);
    if (m_leftHandSideExpression != null) {
      get.setExpression(m_leftHandSideExpression);
    }

    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(get);

    Block body = ast.newBlock();
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
      withReadOnlyPrefix("get");
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
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition methodPos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(get().getName()), getReadOnlyPrefix().length(), -getReadOnlyPrefix().length() - getReadOnlySuffix().length());
      ASTNode returnTypeNode = getReturnType();
      if (returnTypeNode instanceof QualifiedType) {
        QualifiedType t = (QualifiedType) returnTypeNode;
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
    ASTNode methodSibling = getSiblingForGetter(get().getName().getIdentifier(), getDeclaringType(), getFactory().getScoutElementProvider());
    ListRewrite rewrite = getFactory().getRewrite().getListRewrite(getDeclaringType(), getDeclaringType().getBodyDeclarationsProperty());
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
    IType t = Ensure.notNull(AstUtils.getTypeBinding(declaringType));

    org.eclipse.scout.sdk.core.model.api.IType jdtTypeToScoutType = scoutElementProvider.toScoutType(t);
    IStructuredType structuredForm = StructuredType.of(jdtTypeToScoutType);
    IJavaElement methodSibling = structuredForm.getSiblingMethodFieldGetter(getterName);
    if (methodSibling == null) {
      return null;
    }

    if (methodSibling instanceof IMethod) {
      String sigOfSiblingMethod = ((IMethod) methodSibling).identifier();
      for (MethodDeclaration methodDeclaration : declaringType.getMethods()) {
        if (sigOfSiblingMethod.equals(AstUtils.createMethodIdentifier(methodDeclaration))) {
          return methodDeclaration;
        }
      }
    }
    else if (methodSibling instanceof IField) {
      String elemName = methodSibling.elementName();
      for (FieldDeclaration field : declaringType.getFields()) {
        List<VariableDeclarationFragment> fragments = field.fragments();
        for (VariableDeclarationFragment fragment : fragments) {
          if (elemName.equals(fragment.getName().getIdentifier())) {
            return field;
          }
        }
      }
    }
    else if (methodSibling instanceof org.eclipse.scout.sdk.core.model.api.IType) {
      String elemName = methodSibling.elementName();
      for (TypeDeclaration innerType : declaringType.getTypes()) {
        if (elemName.equals(innerType.getName().getIdentifier())) {
          return innerType;
        }
      }
    }
    return null;
  }
}
