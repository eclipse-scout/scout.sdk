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
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

public class SimpleCompositeDataTypeSpi extends AbstractNodeElementSpi<IDataType> implements DataTypeSpi {

  private final DataTypeFlavor m_flavor;
  private final Collection<DataTypeSpi> m_componentDataTypes;
  private final int m_arrayDimension;
  private final FinalValue<String> m_name = new FinalValue<>();

  protected SimpleCompositeDataTypeSpi(NodeModuleSpi module, DataTypeFlavor flavor, Collection<DataTypeSpi> componentDataTypes, int arrayDimension) {
    super(module);
    m_flavor = Ensure.notNull(flavor);
    m_componentDataTypes = componentDataTypes;
    m_arrayDimension = arrayDimension;
  }

  @Override
  protected IDataType createApi() {
    return new DataTypeImplementor<>(this);
  }

  @Override
  public String name() {
    return m_name.computeIfAbsentAndGet(() -> switch (flavor()) {
      case Array -> childTypes().stream().findFirst().map(this::boxComponentDataType).orElse("") + "[]".repeat(arrayDimension());
      case Union -> childTypes().stream().map(this::boxComponentDataType).collect(Collectors.joining(" | "));
      case Intersection -> childTypes().stream().map(this::boxComponentDataType).collect(Collectors.joining(" & "));
      case Single -> null;
    });
  }

  protected String boxComponentDataType(DataTypeSpi componentDataType) {
    if (componentDataType == null || Strings.isBlank(componentDataType.name())) {
      return null;
    }

    var requiresParentheses = switch (flavor()) {
      case Array -> componentDataType.flavor() == DataTypeFlavor.Union || componentDataType.flavor() == DataTypeFlavor.Intersection;
      case Union -> componentDataType.flavor() == DataTypeFlavor.Intersection;
      case Intersection -> componentDataType.flavor() == DataTypeFlavor.Union;
      case Single -> false;
    };
    if (requiresParentheses) {
      return "(" + componentDataType.name() + ")";
    }
    return componentDataType.name();
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public DataTypeFlavor flavor() {
    return m_flavor;
  }

  @Override
  public Collection<DataTypeSpi> childTypes() {
    return Collections.unmodifiableCollection(m_componentDataTypes);
  }

  @Override
  public int arrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public Optional<SourceRange> source() {
    return Optional.empty();
  }
}
