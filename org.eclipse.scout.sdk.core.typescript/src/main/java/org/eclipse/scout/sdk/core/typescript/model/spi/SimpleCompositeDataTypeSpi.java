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

import org.eclipse.scout.sdk.core.typescript.model.api.DataTypeNameEvaluator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

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
    return m_name.computeIfAbsentAndGet(() -> new DataTypeNameEvaluator().eval(this.api()));
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
