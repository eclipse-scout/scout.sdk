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

public class JsElementModelElementModelTypeNode extends JsElementModelTypeNode implements IJsElementModelElementModelTypeNode {

  private final JsElementModel m_jsElementModel;

  protected JsElementModelElementModelTypeNode(IType type, JsElementModel jsElementModel) {
    super(type);
    m_jsElementModel = Ensure.notNull(jsElementModel);
  }

  public static JsElementModelElementModelTypeNode create(IType type, JsElementModel jsElementModel) {
    return new JsElementModelElementModelTypeNode(type, jsElementModel);
  }

  @Override
  public JsElementModel jsElementModel() {
    return m_jsElementModel;
  }
}
