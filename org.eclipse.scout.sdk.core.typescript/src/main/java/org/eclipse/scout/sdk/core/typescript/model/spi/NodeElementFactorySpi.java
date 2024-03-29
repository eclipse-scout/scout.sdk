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
import java.util.List;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElementFactory;

public interface NodeElementFactorySpi {

  INodeElementFactory api();

  FieldSpi createSyntheticField(String name, DataTypeSpi dataType, ES6ClassSpi declaringClass);

  DataTypeSpi createObjectLiteralDataType(String name, ObjectLiteralSpi objectLiteral);

  DataTypeSpi createArrayDataType(DataTypeSpi componentDataType, int arrayDimension);

  DataTypeSpi createUnionDataType(Collection<DataTypeSpi> componentDataTypes);

  DataTypeSpi createIntersectionDataType(Collection<DataTypeSpi> componentDataTypes);

  DataTypeSpi createConstantValueDataType(IConstantValue constantValue);

  ES6ClassSpi createClassWithTypeArgumentsDataType(ES6ClassSpi classSpi, List<DataTypeSpi> arguments);
}
