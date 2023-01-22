/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi;

public class ObjectLiteralImplementor extends AbstractNodeElement<ObjectLiteralSpi> implements IObjectLiteral {
  public ObjectLiteralImplementor(ObjectLiteralSpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return spi().name();
  }
}
