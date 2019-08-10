/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link AstKeyStrokeBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstKeyStrokeBuilder extends AstTypeBuilder<AstKeyStrokeBuilder> {

  private MethodDeclaration m_execAction;

  protected AstKeyStrokeBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstKeyStrokeBuilder insert() {
    super.insert();

    // getConfiguredKeyStroke
    addGetConfiguredKeyStroke();

    // execAction
    m_execAction = getFactory().newExecMethod("execAction")
        .in(get())
        .insert()
        .get();

    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfiguredKeyStroke() {
    AST ast = getFactory().getAst();

    String iKsSimpleName = getFactory().getImportRewrite().addImport(IScoutRuntimeTypes.IKeyStroke);
    QualifiedName keyStrokeRef = ast.newQualifiedName(ast.newName(iKsSimpleName), ast.newSimpleName("F6"));

    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(keyStrokeRef);

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    Type stringType = getFactory().newTypeReference(String.class.getName());
    getFactory().newMethod("getConfiguredKeyStroke")
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(stringType)
        .withBody(body)
        .in(get())
        .insert();

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition typeNamePos = getFactory().getRewrite().track(keyStrokeRef);
      links.addLinkedPosition(typeNamePos, true, AstNodeFactory.KEY_STROKE_GROUP);
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F1");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F2");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F3");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F4");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F5");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F6");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F7");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F8");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F9");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F10");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F11");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".F12");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".DELETE");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".ENTER");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".ESCAPE");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".SPACE");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".TAB");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".LEFT");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".RIGHT");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".UP");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".DOWN");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".CONTROL + " + iKsSimpleName + ".KEY_STROKE_SEPARATOR + " + "'C'");
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".SHIFT + " + iKsSimpleName + ".KEY_STROKE_SEPARATOR + " + '4');
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, iKsSimpleName + ".ALT + " + iKsSimpleName + ".KEY_STROKE_SEPARATOR + " + iKsSimpleName + ".F6");

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IKeyStroke);
    }
  }

  public MethodDeclaration getExecAction() {
    return m_execAction;
  }
}
