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
import java.util.Objects;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;

public interface FieldSpi extends NodeElementSpi {
  @Override
  IField api();

  String name();

  boolean hasModifier(Modifier modifier);

  boolean isOptional();

  IConstantValue constantValue();

  default DataTypeSpi dataType() {
    var dataType = dataTypeImpl();
    if (dataType == null) {
      dataType = getAdditionalFields().stream()
          .map(FieldSpi::dataType)
          .filter(Objects::nonNull)
          .findFirst().orElse(null);
    }
    return dataType;
  }

  List<FieldSpi> getAdditionalFields();

  boolean addAdditionalField(FieldSpi field);

  DataTypeSpi dataTypeImpl();
}
