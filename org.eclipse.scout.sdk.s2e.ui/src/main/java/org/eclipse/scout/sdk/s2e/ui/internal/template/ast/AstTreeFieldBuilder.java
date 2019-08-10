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

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;

/**
 * <h3>{@link AstTreeFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstTreeFieldBuilder extends AstTypeBuilder<AstTreeFieldBuilder> {

  protected AstTreeFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstTreeFieldBuilder insert() {
    super.insert();

    getFactory().newGetConfiguredGridH(6)
        .in(get())
        .insert();

    // inner calendar
    Type treeSuperType = getFactory().newTypeReference(IScoutRuntimeTypes.AbstractTree);
    getFactory().newType(ISdkProperties.INNER_TREE_TYPE_NAME)
        .withCalculatedOrder(false)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withNlsMethod(null)
        .withOrder(false)
        .withCreateLinks(false)
        .withOrderDefinitionType(null)
        .withReadOnlyNameSuffix(null)
        .withSuperType(treeSuperType)
        .in(get())
        .insert();

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.ITreeField);
    }

    return this;
  }
}
