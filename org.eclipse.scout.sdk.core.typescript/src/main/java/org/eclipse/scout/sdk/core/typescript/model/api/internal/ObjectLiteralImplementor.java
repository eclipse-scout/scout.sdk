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

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
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

  @Override
  public Optional<IConstantValue> property(String name) {
    return Optional.ofNullable(spi().properties().get(name));
  }

  @Override
  public Optional<IObjectLiteral> propertyAsObjectLiteral(String name) {
    return propertyAs(name, IObjectLiteral.class);
  }

  @Override
  public Optional<String> propertyAsString(String name) {
    return propertyAs(name, String.class);
  }

  @Override
  public <T> Optional<T> propertyAs(String name, Class<T> type) {
    return property(name)
        .flatMap(v -> v.convertTo(type));
  }
}
