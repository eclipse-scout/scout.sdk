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

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link AstValueFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public class AstValueFieldBuilder<INSTANCE extends AstValueFieldBuilder<INSTANCE>> extends AstTypeBuilder<INSTANCE> {

  @SuppressWarnings({"PublicStaticCollectionField", "StaticCollection"})
  public static final Set<String> PROPOSAL_VALUE_DATA_TYPES = new ConcurrentSkipListSet<>();

  static {
    PROPOSAL_VALUE_DATA_TYPES.add(JavaTypes.Long);
    PROPOSAL_VALUE_DATA_TYPES.add(JavaTypes.Boolean);
    PROPOSAL_VALUE_DATA_TYPES.add(BigDecimal.class.getName());
    PROPOSAL_VALUE_DATA_TYPES.add(String.class.getName());
  }

  protected AstValueFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public INSTANCE insert() {
    // calc super type
    var parameterizedType = getFactory().getAst().newParameterizedType(getSuperType());
    var typeArg = getFactory().newTypeReference(JavaTypes.Long);
    parameterizedType.typeArguments().add(typeArg);
    withSuperType(parameterizedType);

    super.insert();

    // linked positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      if (typeArg != null) {
        var dataTypeTracker = getFactory().getRewrite().track(typeArg);
        links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.VALUE_TYPE_GROUP);
      }

      var proposalTypes = PROPOSAL_VALUE_DATA_TYPES.toArray(new String[0]);
      for (var fqn : proposalTypes) {
        var typeBinding = getFactory().resolveTypeBinding(fqn);
        if (typeBinding != null) {
          links.addLinkedPositionProposal(AstNodeFactory.VALUE_TYPE_GROUP, typeBinding);
        }
      }
    }

    return (INSTANCE) this;
  }
}
