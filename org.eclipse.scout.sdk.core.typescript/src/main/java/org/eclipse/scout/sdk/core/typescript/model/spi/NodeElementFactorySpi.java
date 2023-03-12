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

import java.util.List;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeElementFactory;
import org.eclipse.scout.sdk.core.util.SourceRange;

public interface NodeElementFactorySpi extends NodeElementSpi {
  @Override
  INodeElementFactory api();

  FieldSpi createSyntheticField(String name, DataTypeSpi dataType);

  DataTypeSpi createObjectLiteralDataType(String name, ObjectLiteralSpi objectLiteral);

  DataTypeSpi createArrayDataType(DataTypeSpi componentDataType, int arrayDimension);

  DataTypeSpi createClassWithTypeArgumentsDataType(ES6ClassSpi classSpi, List<DataTypeSpi> arguments);

  @Override
  default Optional<SourceRange> source() {
    return Optional.empty();
  }
}
