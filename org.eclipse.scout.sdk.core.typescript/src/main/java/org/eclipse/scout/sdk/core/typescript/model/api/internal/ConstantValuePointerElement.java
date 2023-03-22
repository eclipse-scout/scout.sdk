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

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer.IJsonPointerElement;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class ConstantValuePointerElement implements IJsonPointerElement {
  private final IConstantValue m_value;
  private final FinalValue<Optional<IConstantValue[]>> m_asArray;
  private final FinalValue<Optional<IObjectLiteral>> m_asObject;

  ConstantValuePointerElement(IConstantValue value) {
    m_value = value;
    m_asArray = new FinalValue<>();
    m_asObject = new FinalValue<>();
  }

  @Override
  public int arrayLength() {
    var asArray = asArray();
    if (asArray.isPresent()) {
      return asArray.orElseThrow().length;
    }
    return 0;
  }

  @Override
  public boolean isObject() {
    return asObject().isPresent();
  }

  @Override
  public IJsonPointerElement element(String name) {
    return asObject().orElseThrow()
        .property(name)
        .map(ConstantValuePointerElement::new)
        .orElse(null);
  }

  @Override
  public IJsonPointerElement element(int index) {
    return asArray()
        .filter(e -> index < e.length)
        .map(e -> e[index])
        .map(ConstantValuePointerElement::new)
        .orElse(null);
  }

  protected Optional<IObjectLiteral> asObject() {
    return m_asObject.computeIfAbsentAndGet(m_value::asObjectLiteral);
  }

  protected Optional<IConstantValue[]> asArray() {
    return m_asArray.computeIfAbsentAndGet(() -> m_value.convertTo(IConstantValue[].class));
  }

  public IConstantValue getValue() {
    return m_value;
  }
}
