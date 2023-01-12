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

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

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
    m_execAction = getFactory().newExecMethod(getFactory().getScoutApi().AbstractAction().execActionMethodName())
        .in(get())
        .insert()
        .get();

    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfiguredKeyStroke() {
    var ast = getFactory().getAst();

    var defaultValue = getFactory().newCombineKeyStrokes("ALT", "F6");
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(defaultValue);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    var stringType = getFactory().newTypeReference(String.class.getName());
    getFactory().newMethod(getFactory().getScoutApi().AbstractAction().getConfiguredKeyStrokeMethodName())
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(stringType)
        .withBody(body)
        .in(get())
        .insert();

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      var iKeyStroke = getFactory().getScoutApi().IKeyStroke().fqn();
      var iKsSimpleName = getFactory().getImportRewrite().addImport(iKeyStroke);
      var typeNamePos = getFactory().getRewrite().track(defaultValue);
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
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, getFactory().newCombineKeyStrokes("CONTROL", "C").toString());
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, getFactory().newCombineKeyStrokes("SHIFT", "4").toString());
      links.addLinkedPositionProposal(AstNodeFactory.KEY_STROKE_GROUP, getFactory().newCombineKeyStrokes("ALT", "F6").toString());

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, iKeyStroke);
    }
  }

  public MethodDeclaration getExecAction() {
    return m_execAction;
  }
}
