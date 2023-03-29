/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class ConstantValueDataTypeSpi extends AbstractNodeElementSpi<IDataType> implements DataTypeSpi {

  private final IConstantValue m_constantValue;
  private final FinalValue<String> m_name = new FinalValue<>();

  protected ConstantValueDataTypeSpi(NodeModuleSpi module, IConstantValue constantValue) {
    super(module);
    m_constantValue = constantValue;
  }

  @Override
  public Collection<DataTypeSpi> childTypes() {
    return Collections.emptyList();
  }

  @Override
  protected IDataType createApi() {
    return new DataTypeImplementor<>(this);
  }

  @Override
  public Optional<IConstantValue> constantValue() {
    return Optional.of(m_constantValue);
  }

  @Override
  public String name() {
    return m_name.computeIfAbsentAndGet(() -> constantValue().flatMap(IConstantValue::value)
        .map(v -> {
          if (v instanceof INodeElement nodeElement) {
            return nodeElement.name();
          }
          return v.toString();
        }).orElse(null));
  }

  @Override
  public boolean isPrimitive() {
    return constantValue().flatMap(IConstantValue::dataType)
        .map(IDataType::isPrimitive).orElse(false);
  }

  @Override
  public Optional<SourceRange> source() {
    return Optional.empty();
  }
}
