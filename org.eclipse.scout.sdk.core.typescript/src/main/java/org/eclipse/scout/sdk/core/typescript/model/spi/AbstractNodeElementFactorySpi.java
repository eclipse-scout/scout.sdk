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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElementFactory;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeElementFactoryImplementor;
import org.eclipse.scout.sdk.core.util.CompositeObject;

public abstract class AbstractNodeElementFactorySpi extends AbstractNodeElementSpi<INodeElementFactory> implements NodeElementFactorySpi {

  private final Map<Object, Object> m_elements = new ConcurrentHashMap<>();

  protected AbstractNodeElementFactorySpi(NodeModuleSpi module) {
    super(module);
  }

  @Override
  protected INodeElementFactory createApi() {
    return new NodeElementFactoryImplementor(this);
  }

  protected <ID, R> R getOrCreate(ID identifier, Function<ID, R> factory) {
    //noinspection unchecked
    return (R) m_elements.computeIfAbsent(identifier, id -> factory.apply((ID) id));
  }

  @Override
  public SyntheticFieldSpi createSyntheticField(String name, DataTypeSpi dataType) {
    return getOrCreate(new CompositeObject(name, SyntheticFieldSpi.class), id -> new SyntheticFieldSpi(containingModule(), name, dataType));
  }

  @Override
  public ObjectLiteralDataTypeSpi createObjectLiteralDataType(String name, ObjectLiteralSpi objectLiteral) {
    return getOrCreate(new CompositeObject(name, objectLiteral), id -> new ObjectLiteralDataTypeSpi(containingModule(), name, objectLiteral));
  }

  protected DataTypeSpi createCompositeDataType(DataTypeFlavor flavor, Collection<DataTypeSpi> componentDataTypes, int arrayDimension) {
    return getOrCreate(new CompositeObject(flavor, componentDataTypes, arrayDimension), id -> new SimpleCompositeDataTypeSpi(containingModule(), flavor, componentDataTypes, arrayDimension));
  }

  @Override
  public DataTypeSpi createArrayDataType(DataTypeSpi componentDataType, int arrayDimension) {
    if (arrayDimension < 1) {
      return componentDataType;
    }

    var newDimension = arrayDimension;
    var leafComponentType = componentDataType;
    if (componentDataType != null && componentDataType.flavor() == DataTypeFlavor.Array) {
      newDimension += componentDataType.arrayDimension();
      leafComponentType = componentDataType.componentDataTypes().findAny().orElse(null);
    }
    var componentDataTypes = Optional.ofNullable(leafComponentType)
        .map(Collections::singleton)
        .orElse(Collections.emptySet());

    return createCompositeDataType(DataTypeFlavor.Array, componentDataTypes, newDimension);
  }
}
