/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.js.element;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;

public class JsElementModelTypeNode implements IJsElementModelTypeNode {

  private final IType m_type;

  protected JsElementModelTypeNode(IType type) {
    m_type = Ensure.notNull(type);
  }

  public static JsElementModelTypeNode create(IType type) {
    return new JsElementModelTypeNode(type);
  }

  @Override
  public IType type() {
    return m_type;
  }

  @Override
  public String name() {
    return type().elementName();
  }
}
