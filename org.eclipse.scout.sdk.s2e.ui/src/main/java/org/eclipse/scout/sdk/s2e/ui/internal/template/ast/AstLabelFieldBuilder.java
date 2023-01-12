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

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstLabelFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstLabelFieldBuilder extends AstTypeBuilder<AstLabelFieldBuilder> {

  protected AstLabelFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstLabelFieldBuilder insert() {
    super.insert();

    Annotation annotation = getFactory().newFormDataIgnoreAnnotation();
    AstUtils.addAnnotationTo(annotation, get());

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().ILabelField().fqn());
    }

    return this;
  }
}
