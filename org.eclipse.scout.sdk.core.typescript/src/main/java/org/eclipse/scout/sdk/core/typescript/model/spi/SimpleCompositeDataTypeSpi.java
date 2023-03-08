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
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class SimpleCompositeDataTypeSpi extends AbstractNodeElementSpi<IDataType> implements DataTypeSpi {

  private final DataTypeFlavor m_dataTypeFlavor;
  private final Collection<DataTypeSpi> m_componentDataTypes;
  private final int m_arrayDimension;

  protected SimpleCompositeDataTypeSpi(NodeModuleSpi module, DataTypeFlavor dataTypeFlavor, Collection<DataTypeSpi> componentDataTypes, int arrayDimension) {
    super(module);
    m_dataTypeFlavor = Ensure.notNull(dataTypeFlavor);
    m_componentDataTypes = componentDataTypes;
    m_arrayDimension = arrayDimension;
  }

  public static SimpleCompositeDataTypeSpi createArray(NodeModuleSpi module, DataTypeSpi componentDataType, int arrayDimension) {
    return new SimpleCompositeDataTypeSpi(
        module,
        DataTypeFlavor.Array,
        Optional.ofNullable(componentDataType)
            .map(Collections::singleton)
            .orElse(Collections.emptySet()),
        arrayDimension);
  }

  @Override
  protected IDataType createApi() {
    return new DataTypeImplementor<>(this);
  }

  @Override
  public String name() {
    return switch (dataTypeFlavor()) {
      case Array -> componentDataTypes().findFirst().map(DataTypeSpi::name).orElse("") + "[]".repeat(arrayDimension());
      case Single -> null;
    };
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public DataTypeFlavor dataTypeFlavor() {
    return m_dataTypeFlavor;
  }

  @Override
  public Stream<DataTypeSpi> componentDataTypes() {
    return Optional.ofNullable(m_componentDataTypes).stream().flatMap(Collection::stream);
  }

  @Override
  public int arrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public Optional<SourceRange> source() {
    return Optional.of(new SourceRange(name(), 0));
  }
}
