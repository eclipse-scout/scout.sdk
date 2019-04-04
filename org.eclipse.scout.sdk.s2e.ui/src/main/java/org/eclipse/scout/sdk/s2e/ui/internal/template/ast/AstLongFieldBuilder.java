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
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
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

    addGetConfigured("getConfiguredMinValue", "-999999999999L", AstNodeFactory.MIN_GROUP, get());
    addGetConfigured("getConfiguredMaxValue", "999999999999L", AstNodeFactory.MAX_GROUP, get());

    return this;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfigured(String name, String value, String group, TypeDeclaration newFormField) {
    AST ast = getFactory().getAst();

    Type longType = getFactory().newTypeReference(JavaTypes.Long);
    NumberLiteral literal = ast.newNumberLiteral(value);
    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(longType)
        .withBody(body)
        .in(newFormField)
        .insert();

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition literalTracker = new WrappedTrackedNodePosition(getFactory().getRewrite().track(literal), 0, -1);
      links.addLinkedPosition(literalTracker, true, group);

      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.ILongField);
    }
  }
}
