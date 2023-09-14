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

import static java.util.stream.Collectors.toCollection;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElementFactory;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeElementFactoryImplementor;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public abstract class AbstractNodeElementFactorySpi implements NodeElementFactorySpi {

  private final Map<Object, Object> m_elements;
  private final FinalValue<INodeElementFactory> m_api;
  private final NodeModuleSpi m_module;

  protected AbstractNodeElementFactorySpi(NodeModuleSpi module) {
    m_module = Ensure.notNull(module);
    m_elements = new ConcurrentHashMap<>();
    m_api = new FinalValue<>();
  }

  public NodeModuleSpi containingModule() {
    return m_module;
  }

  @Override
  public INodeElementFactory api() {
    return m_api.computeIfAbsentAndGet(() -> new NodeElementFactoryImplementor(this));
  }

  @SuppressWarnings("unchecked")
  protected <ID, R> R getOrCreate(ID identifier, Function<ID, R> factory) {
    return (R) m_elements.computeIfAbsent(identifier, id -> factory.apply((ID) id));
  }

  @Override
  public SyntheticFieldSpi createSyntheticField(String name, DataTypeSpi dataType, ES6ClassSpi declaringClass) {
    return getOrCreate(new CompositeObject(name, declaringClass, SyntheticFieldSpi.class), id -> new SyntheticFieldSpi(containingModule(), name, dataType, declaringClass));
  }

  @Override
  public ObjectLiteralDataTypeSpi createObjectLiteralDataType(String name, ObjectLiteralSpi objectLiteral) {
    return getOrCreate(new CompositeObject(name, objectLiteral), id -> new ObjectLiteralDataTypeSpi(containingModule(), name, objectLiteral));
  }

  private DataTypeSpi createCompositeDataType(DataTypeFlavor flavor, Collection<DataTypeSpi> componentDataTypes) {
    return getOrCreate(new CompositeObject(flavor, componentDataTypes), id -> new SimpleCompositeDataTypeSpi(containingModule(), flavor, componentDataTypes, 0));
  }

  @Override
  public ES6ClassSpi createClassWithTypeArgumentsDataType(ES6ClassSpi classSpi, List<DataTypeSpi> arguments) {
    return getOrCreate(new SimpleEntry<>(classSpi, arguments), id -> new ES6ClassWithTypeArgumentsSpi(containingModule(), id.getKey(), id.getValue()));
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
      leafComponentType = componentDataType.childTypes().stream().findAny().orElse(null);
    }
    var componentDataTypes = Optional.ofNullable(leafComponentType)
        .map(Collections::singleton)
        .orElse(Collections.emptySet());
    var dimension = newDimension;
    return getOrCreate(new CompositeObject(DataTypeFlavor.Array, componentDataTypes, dimension), id -> new SimpleCompositeDataTypeSpi(containingModule(), DataTypeFlavor.Array, componentDataTypes, dimension));
  }

  private DataTypeSpi createUnionOrIntersectionDataType(Collection<DataTypeSpi> componentDataTypes, DataTypeFlavor unionOrIntersection) {
    if (componentDataTypes == null || componentDataTypes.isEmpty()) {
      return null;
    }
    if (componentDataTypes.size() == 1) {
      return componentDataTypes.iterator().next();
    }
    if (unionOrIntersection != DataTypeFlavor.Union && unionOrIntersection != DataTypeFlavor.Intersection) {
      return null;
    }

    return createCompositeDataType(unionOrIntersection, componentDataTypes.stream()
        .flatMap(componentDataType -> componentDataType.flavor() == unionOrIntersection ? componentDataType.childTypes().stream() : Stream.of(componentDataType))
        .collect(toCollection(LinkedHashSet::new)));
  }

  @Override
  public DataTypeSpi createUnionDataType(Collection<DataTypeSpi> componentDataTypes) {
    return createUnionOrIntersectionDataType(componentDataTypes, DataTypeFlavor.Union);
  }

  @Override
  public DataTypeSpi createIntersectionDataType(Collection<DataTypeSpi> componentDataTypes) {
    return createUnionOrIntersectionDataType(componentDataTypes, DataTypeFlavor.Intersection);
  }

  @Override
  public DataTypeSpi createConstantValueDataType(IConstantValue constantValue) {
    if (constantValue == null) {
      return null;
    }

    return getOrCreate(new CompositeObject(constantValue, ConstantValueDataTypeSpi.class), id -> new ConstantValueDataTypeSpi(containingModule(), constantValue));
  }
}
