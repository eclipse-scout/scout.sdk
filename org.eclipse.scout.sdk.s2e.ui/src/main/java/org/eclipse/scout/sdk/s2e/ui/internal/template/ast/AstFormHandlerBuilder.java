/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstFormHandlerBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstFormHandlerBuilder extends AstTypeBuilder<AstFormHandlerBuilder> {

  private MethodDeclaration m_execLoad;
  private MethodDeclaration m_execStore;

  protected AstFormHandlerBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  @SuppressWarnings("unchecked")
  public AstFormHandlerBuilder insert() {
    super.insert();

    var scoutApi = getFactory().getScoutApi();
    m_execLoad = getFactory().newExecMethod(scoutApi.AbstractFormHandler().execLoadMethodName())
        .in(get())
        .insert()
        .get();

    m_execStore = getFactory().newExecMethod(scoutApi.AbstractFormHandler().execStoreMethodName())
        .in(get())
        .insert()
        .get();

    // form handlers created in extensions must be static!
    if (AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), scoutApi.IExtension().fqn())) {
      get().modifiers().add(getFactory().getAst().newModifier(ModifierKeyword.STATIC_KEYWORD));
    }

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, scoutApi.IFormHandler().fqn());
    }

    return this;
  }

  public MethodDeclaration getExecLoad() {
    return m_execLoad;
  }

  public MethodDeclaration getExecStore() {
    return m_execStore;
  }
}
