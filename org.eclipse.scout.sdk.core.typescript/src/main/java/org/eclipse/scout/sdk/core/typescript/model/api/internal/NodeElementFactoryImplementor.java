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

import static java.util.stream.Collectors.toCollection;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElementFactory;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementFactorySpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class NodeElementFactoryImplementor implements INodeElementFactory {

  private final NodeElementFactorySpi m_spi;

  public NodeElementFactoryImplementor(NodeElementFactorySpi spi) {
    m_spi = Ensure.notNull(spi);
  }

  @Override
  public IField createSyntheticField(String name, IDataType dataType, IES6Class declaringClass) {
    return m_spi.createSyntheticField(name, Ensure.notNull(dataType).spi(), Ensure.notNull(declaringClass).spi()).api();
  }

  @Override
  public IDataType createObjectLiteralDataType(String name, IObjectLiteral objectLiteral) {
    return m_spi.createObjectLiteralDataType(name, Ensure.notNull(objectLiteral).spi()).api();
  }

  @Override
  public IDataType createArrayDataType(IDataType componentDataType, int arrayDimension) {
    return m_spi.createArrayDataType(Optional.ofNullable(componentDataType).map(IDataType::spi).orElse(null), arrayDimension).api();
  }

  @Override
  public IDataType createUnionDataType(Collection<IDataType> componentDataTypes) {
    return Optional.ofNullable(componentDataTypes)
        .map(types -> types.stream()
            .map(IDataType::spi)
            .collect(toCollection(LinkedHashSet::new)))
        .map(m_spi::createUnionDataType)
        .map(DataTypeSpi::api)
        .orElse(null);
  }

  @Override
  public IDataType createIntersectionDataType(Collection<IDataType> componentDataTypes) {
    return Optional.ofNullable(componentDataTypes)
        .map(types -> types.stream()
            .map(IDataType::spi)
            .collect(toCollection(LinkedHashSet::new)))
        .map(m_spi::createIntersectionDataType)
        .map(DataTypeSpi::api)
        .orElse(null);
  }

  @Override
  public IDataType createConstantValueDataType(IConstantValue constantValue) {
    return m_spi.createConstantValueDataType(Ensure.notNull(constantValue)).api();
  }
}
