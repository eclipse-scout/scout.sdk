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
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Converts an {@link IObjectLiteral} to an {@link IConstantValue}.
 */
public class ConstantValueAdapter implements IConstantValue {

  private final IObjectLiteral m_literal;

  public ConstantValueAdapter(IObjectLiteral literal) {
    m_literal = Ensure.notNull(literal);
  }

  @Override
  public <T> Optional<T> convertTo(Class<T> expectedType) {
    if (expectedType == IObjectLiteral.class) {
      //noinspection unchecked
      return (Optional<T>) asObjectLiteral();
    }
    return Optional.empty();
  }

  @Override
  public Optional<IObjectLiteral> asObjectLiteral() {
    return Optional.of(m_literal);
  }

  @Override
  public ConstantValueType type() {
    return ConstantValueType.ObjectLiteral;
  }
}
