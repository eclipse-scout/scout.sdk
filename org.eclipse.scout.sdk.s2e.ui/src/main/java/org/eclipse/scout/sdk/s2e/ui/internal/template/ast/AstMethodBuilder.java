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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.manipulation.CodeGeneration;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstMethodBuilder}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public class AstMethodBuilder<INSTANCE extends AstMethodBuilder<INSTANCE>> extends AbstractAstBuilder<INSTANCE> {

  private final INSTANCE m_return;

  private String m_methodName;
  private Type m_returnType;
  private Block m_body;
  private boolean m_createOverride;
  private boolean m_createJavaDoc;

  // out
  private MethodDeclaration m_resultMethod;

  protected AstMethodBuilder(AstNodeFactory owner) {
    super(owner);
    m_return = (INSTANCE) this;
  }

  public INSTANCE withName(String name) {
    m_methodName = name;
    return m_return;
  }

  public INSTANCE withReturnType(Type returnType) {
    m_returnType = returnType;
    return m_return;
  }

  public INSTANCE withBody(Block body) {
    m_body = body;
    return m_return;
  }

  public INSTANCE withOverride(boolean createOverride) {
    m_createOverride = createOverride;
    return m_return;
  }

  public INSTANCE withJavaDoc(boolean createJavaDoc) {
    m_createJavaDoc = createJavaDoc;
    return m_return;
  }

  public MethodDeclaration get() {
    return m_resultMethod;
  }

  public String getMethodName() {
    return m_methodName;
  }

  public boolean isCreateOverride() {
    return m_createOverride;
  }

  public boolean isCreateJavaDoc() {
    return m_createJavaDoc;
  }

  public Type getReturnType() {
    return m_returnType;
  }

  public Block getBody() {
    return m_body;
  }

  protected void insertMethod() {
    if (getDeclaringType() != null) {
      getDeclaringType().bodyDeclarations().add(m_resultMethod);
    }
  }

  @Override
  public INSTANCE insert() {
    Ensure.notNull(getMethodName());
    var ast = getFactory().getAst();
    var body = getBody();
    if (body == null) {
      body = ast.newBlock();
    }

    m_resultMethod = ast.newMethodDeclaration();
    m_resultMethod.setConstructor(false);
    for (var mod : getModifiers()) {
      m_resultMethod.modifiers().add(ast.newModifier(mod));
    }

    var methodName = ast.newSimpleName(getMethodName());
    m_resultMethod.setName(methodName);
    var returnType = getReturnType();
    if (returnType != null) {
      m_resultMethod.setReturnType2(returnType);
    }

    m_resultMethod.setBody(body);

    if (isCreateJavaDoc() && getFactory().isCreateCommentsSetting() && getModifiers().contains(ModifierKeyword.PUBLIC_KEYWORD)) {
      try {
        var declaringTypeFqn = AstUtils.getFullyQualifiedName(getDeclaringType(), getFactory().getType(), JavaTypes.C_DOT);
        var javadocText = CodeGeneration.getMethodComment(getFactory().getIcu(), declaringTypeFqn, m_resultMethod, null, getFactory().getIcu().findRecommendedLineSeparator());
        if (Strings.hasText(javadocText)) {
          var javadoc = (Javadoc) getFactory().getRewrite().createStringPlaceholder(javadocText, ASTNode.JAVADOC);
          m_resultMethod.setJavadoc(javadoc);
        }
      }
      catch (CoreException e) {
        SdkLog.warning("Unable to add default javadoc to form field getter.", e);
      }
    }

    if (isCreateOverride() && getFactory().isCreateOverrideAnnotationSetting()) {
      AstUtils.addAnnotationTo(getFactory().newOverrideAnnotation(), m_resultMethod);
    }

    insertMethod();

    return (INSTANCE) this;
  }
}
