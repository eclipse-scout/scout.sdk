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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElementFactory;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementFactorySpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class NodeElementFactoryImplementor extends AbstractNodeElement<NodeElementFactorySpi> implements INodeElementFactory {

  public NodeElementFactoryImplementor(NodeElementFactorySpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return "INodeElementFactory for " + containingModule().name();
  }

  @Override
  public IField createSyntheticField(String name, IDataType dataType) {
    return spi().createSyntheticField(name, Ensure.notNull(dataType).spi()).api();
  }

  @Override
  public IDataType createObjectLiteralDataType(String name, IObjectLiteral objectLiteral) {
    return spi().createObjectLiteralDataType(name, Ensure.notNull(objectLiteral).spi()).api();
  }

  @Override
  public IDataType createArrayDataType(IDataType componentDataType, int arrayDimension) {
    return spi().createArrayDataType(Optional.ofNullable(componentDataType).map(IDataType::spi).orElse(null), arrayDimension).api();
  }

  @Override
  public IDataType createUnionDataType(Collection<IDataType> componentDataTypes) {
    return Optional.ofNullable(componentDataTypes)
        .map(types -> types.stream()
            .map(IDataType::spi)
            .collect(Collectors.toSet()))
        .map(spi()::createUnionDataType)
        .map(DataTypeSpi::api)
        .orElse(null);
  }

  @Override
  public IDataType createIntersectionDataType(Collection<IDataType> componentDataTypes) {
    return Optional.ofNullable(componentDataTypes)
        .map(types -> types.stream()
            .map(IDataType::spi)
            .collect(Collectors.toSet()))
        .map(spi()::createIntersectionDataType)
        .map(DataTypeSpi::api)
        .orElse(null);
  }
}
