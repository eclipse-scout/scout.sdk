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

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link AstLongFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstLongFieldBuilder extends AstTypeBuilder<AstLongFieldBuilder> {

  protected AstLongFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstLongFieldBuilder insert() {
    super.insert();
    var abstractNumberFieldApi = getFactory().getScoutApi().AbstractNumberField();
    addGetConfigured(abstractNumberFieldApi.getConfiguredMinValueMethodName(), "-999999999999L", AstNodeFactory.MIN_GROUP, get());
    addGetConfigured(abstractNumberFieldApi.getConfiguredMaxValueMethodName(), "999999999999L", AstNodeFactory.MAX_GROUP, get());
    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfigured(String name, String value, String group, TypeDeclaration newFormField) {
    var ast = getFactory().getAst();

    var longType = getFactory().newTypeReference(JavaTypes.Long);
    var literal = ast.newNumberLiteral(value);
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(longType)
        .withBody(body)
        .in(newFormField)
        .insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition literalTracker = new WrappedTrackedNodePosition(getFactory().getRewrite().track(literal), 0, -1);
      links.addLinkedPosition(literalTracker, true, group);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ILongField().fqn());
    }
  }
}
