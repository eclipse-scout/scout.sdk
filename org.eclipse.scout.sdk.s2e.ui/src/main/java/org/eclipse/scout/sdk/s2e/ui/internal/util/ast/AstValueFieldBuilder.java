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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;

/**
 * <h3>{@link AstValueFieldBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public class AstValueFieldBuilder<INSTANCE extends AstValueFieldBuilder<INSTANCE>> extends AstTypeBuilder<INSTANCE> {

  public static final Set<String> PROPOSAL_VALUE_DATA_TYPES = Collections.synchronizedSortedSet(new TreeSet<String>());

  static {
    PROPOSAL_VALUE_DATA_TYPES.add(IJavaRuntimeTypes.java_lang_Long);
    PROPOSAL_VALUE_DATA_TYPES.add(IJavaRuntimeTypes.java_lang_Boolean);
    PROPOSAL_VALUE_DATA_TYPES.add(IJavaRuntimeTypes.java_math_BigDecimal);
    PROPOSAL_VALUE_DATA_TYPES.add(IJavaRuntimeTypes.java_lang_String);
  }

  protected AstValueFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public INSTANCE insert() {
    Type typeArg = null;
    // calc super type
    ParameterizedType parameterizedType = getFactory().getAst().newParameterizedType(getSuperType());
    typeArg = getFactory().newTypeReference(IJavaRuntimeTypes.java_lang_Long);
    parameterizedType.typeArguments().add(typeArg);
    withSuperType(parameterizedType);

    super.insert();

    // linked positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      if (typeArg != null) {
        ITrackedNodePosition dataTypeTracker = getFactory().getRewrite().track(typeArg);
        links.addLinkedPosition(dataTypeTracker, true, AstNodeFactory.VALUE_TYPE_GROUP);
      }

      String[] proposalTypes = PROPOSAL_VALUE_DATA_TYPES.toArray(new String[PROPOSAL_VALUE_DATA_TYPES.size()]);
      for (String fqn : proposalTypes) {
        ITypeBinding typeBinding = getFactory().resolveTypeBinding(fqn);
        if (typeBinding != null) {
          links.addLinkedPositionProposal(AstNodeFactory.VALUE_TYPE_GROUP, typeBinding);
        }
      }
    }

    return (INSTANCE) this;
  }
}
