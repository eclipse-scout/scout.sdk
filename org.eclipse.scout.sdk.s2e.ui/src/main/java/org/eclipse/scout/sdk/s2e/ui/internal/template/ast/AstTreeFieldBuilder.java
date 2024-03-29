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

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.scout.sdk.core.s.ISdkConstants;

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
    var treeSuperType = getFactory().newTypeReference(getFactory().getScoutApi().AbstractTree().fqn());
    getFactory().newType(ISdkConstants.INNER_TREE_TYPE_NAME)
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

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ITreeField().fqn());
    }

    return this;
  }
}
