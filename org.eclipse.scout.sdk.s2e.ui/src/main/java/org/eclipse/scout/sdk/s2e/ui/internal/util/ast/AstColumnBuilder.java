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

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstColumnBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AstColumnBuilder extends AstTypeBuilder<AstColumnBuilder> {

  protected AstColumnBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstColumnBuilder insert() {

    super.insert();

    // getConfiguredWidth
    getFactory().newGetConfiguredWidth(100)
        .in(get())
        .insert();

    // column getter
    addColumnGetter();

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IColumn);
    }

    return this;
  }

  protected void addColumnGetter() {

    MethodInvocation getColumnSet = getFactory().getAst().newMethodInvocation();
    getColumnSet.setName(getFactory().getAst().newSimpleName("getColumnSet"));

    if (AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), IScoutRuntimeTypes.IExtension)) {
      // column in table extension
      MethodInvocation getOwner = getFactory().getAst().newMethodInvocation();
      getOwner.setName(getFactory().getAst().newSimpleName("getOwner"));
      getColumnSet.setExpression(getOwner);
    }

    getFactory().newInnerTypeGetter()
        .withMethodNameToFindInnerType("getColumnByClass")
        .withMethodToFindInnerTypeExpression(getColumnSet)
        .withName(getTypeName())
        .withReadOnlySuffix(getReadOnlySuffix())
        .withReturnType(getFactory().getAst().newSimpleType(getFactory().getAst().newSimpleName(getTypeName() + getReadOnlySuffix())))
        .in(getDeclaringType())
        .insert();
  }
}
