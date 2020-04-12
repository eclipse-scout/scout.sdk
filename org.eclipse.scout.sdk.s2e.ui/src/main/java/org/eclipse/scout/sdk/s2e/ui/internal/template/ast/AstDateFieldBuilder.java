/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link AstDateFieldBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstDateFieldBuilder extends AstTypeBuilder<AstDateFieldBuilder> {

  protected AstDateFieldBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstDateFieldBuilder insert() {
    super.insert();

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IDateField);
    }

    return this;
  }
}
