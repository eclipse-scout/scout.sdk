/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.testing.spi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.DataTypeImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor;
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementFactorySpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;

public class TestingNodeElementFactorySpi extends AbstractNodeElementFactorySpi {

  private final Map<String, DataTypeSpi> m_dataTypes;

  public TestingNodeElementFactorySpi() {
    this(TestingNodeModulesProviderSpi.createNodeModule("unknown", "0.0.0", null));
    //noinspection ThisEscapedInObjectConstruction
    when(containingModule().nodeElementFactory()).thenReturn(this);
  }

  public TestingNodeElementFactorySpi(NodeModuleSpi module) {
    super(module);
    m_dataTypes = new HashMap<>();
  }

  public IDataType getOrCreateDataType(String name, IDataType... typeArguments) {
    return getOrCreateDataTypeSpi(name, typeArguments).api();
  }

  public DataTypeSpi getOrCreateDataTypeSpi(String name, IDataType... typeArguments) {
    var dt = m_dataTypes.computeIfAbsent(name, n -> newDataType(n, typeArguments).spi());
    when(dt.containingModule()).thenReturn(containingModule());
    return dt;
  }

  public static IDataType newDataType(String name, IDataType... typeArguments) {
    if (typeArguments != null && typeArguments.length > 0) {
      var clazz = mock(ES6ClassSpi.class);
      when(clazz.api()).thenReturn(new ES6ClassImplementor(clazz));
      when(clazz.flavor()).thenReturn(DataTypeFlavor.Single);
      when(clazz.isPrimitive()).thenReturn(false);
      when(clazz.name()).thenReturn(name);
      var args = Arrays.stream(typeArguments).map(IDataType::spi).toList();
      return new AbstractNodeElementFactorySpi(mock(NodeModuleSpi.class)) {
      }.createClassWithTypeArgumentsDataType(clazz, args).api();
    }

    var dataType = mock(DataTypeSpi.class);
    when(dataType.api()).thenReturn(new DataTypeImplementor<>(dataType));
    when(dataType.flavor()).thenReturn(DataTypeFlavor.Single);
    when(dataType.isPrimitive()).thenReturn(TypeScriptTypes.isPrimitive(name));
    when(dataType.name()).thenReturn(name);
    return dataType.api();
  }
}
