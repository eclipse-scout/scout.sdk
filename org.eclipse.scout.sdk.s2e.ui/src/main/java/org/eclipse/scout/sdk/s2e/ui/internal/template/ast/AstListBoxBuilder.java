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

/**
 * <h3>{@link AstListBoxBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstListBoxBuilder extends AstValueFieldBuilder<AstListBoxBuilder> {

  protected AstListBoxBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstListBoxBuilder insert() {
    super.insert();
    getFactory().newGetConfiguredGridH(6)
        .in(get())
        .insert();

    return this;
  }
}
