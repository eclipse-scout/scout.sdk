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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class DataTypeImplementor<SPI extends DataTypeSpi> extends AbstractNodeElement<SPI> implements IDataType {

  private final FinalValue<Set<IDataType>> m_leafTypes;

  public DataTypeImplementor(SPI spi) {
    super(spi);
    m_leafTypes = new FinalValue<>();
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public boolean isPrimitive() {
    return spi().isPrimitive();
  }

  @Override
  public Stream<IDataType> leafTypes() {
    return m_leafTypes.computeIfAbsentAndGet(() -> {
      var children = componentDataTypes().collect(Collectors.toSet());
      if (children.isEmpty()) {
        return Collections.singleton(this); // this is a leaf
      }
      return children;
    }).stream();
  }

  @Override
  public String toString() {
    return name() + " [" + containingModule() + ']';
  }
}
