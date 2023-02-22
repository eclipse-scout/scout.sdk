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

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.SimpleFieldSpi;

public interface IField extends INodeElement {
  @Override
  FieldSpi spi();

  boolean isOptional();

  boolean hasModifier(Modifier modifier);

  Optional<IDataType> dataType();

  IConstantValue constantValue();

  static IField createSimple(String name, String dataType) {
    return createSimple(name, IDataType.createSimple(dataType));
  }

  static IField createSimple(String name, IDataType dataType) {
    return new SimpleFieldSpi(name, dataType != null ? dataType.spi() : null).api();
  }
}
