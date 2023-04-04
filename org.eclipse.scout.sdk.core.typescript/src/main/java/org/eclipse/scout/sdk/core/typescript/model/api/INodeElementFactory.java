/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import java.util.Collection;

public interface INodeElementFactory {
  IField createSyntheticField(String name, IDataType dataType, IES6Class declaringClass);

  IDataType createObjectLiteralDataType(String name, IObjectLiteral objectLiteral);

  IDataType createArrayDataType(IDataType componentDataType, int arrayDimension);

  IDataType createUnionDataType(Collection<IDataType> componentDataTypes);

  IDataType createIntersectionDataType(Collection<IDataType> componentDataTypes);

  IDataType createConstantValueDataType(IConstantValue constantValue);
}
