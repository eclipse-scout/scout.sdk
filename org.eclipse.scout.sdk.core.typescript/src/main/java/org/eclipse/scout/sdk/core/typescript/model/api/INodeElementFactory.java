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

import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementFactorySpi;

public interface INodeElementFactory extends INodeElement {
  @Override
  NodeElementFactorySpi spi();

  IField createSyntheticField(String name, IDataType dataType);

  IDataType createObjectLiteralDataType(String name, IObjectLiteral objectLiteral);

  IDataType createArrayDataType(IDataType componentDataType, int arrayDimension);
}