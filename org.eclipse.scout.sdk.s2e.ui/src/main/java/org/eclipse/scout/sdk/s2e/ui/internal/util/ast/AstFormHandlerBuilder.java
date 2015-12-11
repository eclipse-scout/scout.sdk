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

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstFormHandlerBuilder}</h3>
 *
 * @author Matthias Villiger
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

    m_execLoad = getFactory().newExecMethod("execLoad")
        .in(get())
        .insert()
        .get();

    m_execStore = getFactory().newExecMethod("execStore")
        .in(get())
        .insert()
        .get();

    // form handlers created in extensions must be static!
    if (AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), IScoutRuntimeTypes.IExtension)) {
      get().modifiers().add(getFactory().getAst().newModifier(ModifierKeyword.STATIC_KEYWORD));
    }

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IFormHandler);
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
