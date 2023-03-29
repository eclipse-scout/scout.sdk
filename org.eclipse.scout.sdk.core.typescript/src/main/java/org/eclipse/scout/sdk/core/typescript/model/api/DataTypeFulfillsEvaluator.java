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

import java.util.function.Predicate;

public class DataTypeFulfillsEvaluator {

  private final Predicate<IDataType> m_evalStrategy;

  public DataTypeFulfillsEvaluator(Predicate<IDataType> evalStrategy) {
    m_evalStrategy = evalStrategy;
  }

  public boolean fulfills(IDataType dataType) {
    if (dataType == null) {
      return false;
    }

    return switch (dataType.flavor()) {
      case Union -> checkUnion(dataType);
      case Intersection -> checkIntersection(dataType);
      case Array -> checkArray(dataType);
      case Single -> checkSingle(dataType);
    };
  }

  protected boolean checkUnion(IDataType dataType) {
    return dataType.childTypes().anyMatch(this::fulfills);
  }

  protected boolean checkIntersection(IDataType dataType) {
    return dataType.childTypes().allMatch(this::fulfills);
  }

  protected boolean checkArray(IDataType dataType) {
    return m_evalStrategy.test(dataType);
  }

  protected boolean checkSingle(IDataType dataType) {
    // direct check first
    if (m_evalStrategy.test(dataType)) {
      return true;
    }
    if (dataType.spi().childTypes().isEmpty()) {
      return false;
    }
    // continue with children if not matched (e.g. for a TypeAlias or TypeOfType)
    return checkUnion(dataType);
  }
}
