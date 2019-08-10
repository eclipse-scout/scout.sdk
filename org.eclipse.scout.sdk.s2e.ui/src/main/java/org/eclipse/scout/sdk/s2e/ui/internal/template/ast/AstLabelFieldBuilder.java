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

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
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

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.ILabelField);
    }

    return this;
  }
}
