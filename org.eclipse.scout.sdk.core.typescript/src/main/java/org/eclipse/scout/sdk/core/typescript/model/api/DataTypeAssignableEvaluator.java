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

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;

public class DataTypeAssignableEvaluator implements Predicate<IDataType> {

  private final IDataType m_childType;
  private final Function<IDataType, Stream<IDataType>> m_childrenSupplier;
  private final boolean m_childIsArray;
  private final boolean m_requiresInstanceOfCheck;

  public DataTypeAssignableEvaluator(IDataType child) {
    this(child, IDataType::childTypes);
  }

  public DataTypeAssignableEvaluator(IDataType child, Function<IDataType, Stream<IDataType>> childrenSupplier) {
    m_childType = child;
    m_childIsArray = child.flavor() == DataTypeFlavor.Array;
    m_childrenSupplier = childrenSupplier;
    m_requiresInstanceOfCheck = isInstanceOfCheckRequired(child);
  }

  private static boolean isInstanceOfCheckRequired(IDataType child) {
    if (!(child instanceof IES6Class clazz)) {
      return false;
    }
    return !clazz.isEnum() && !clazz.isTypeAlias();
  }

  public boolean fulfills(IDataType base) {
    if (base == null) {
      return false;
    }
    return new DataTypeFulfillsEvaluator(this, m_childrenSupplier).fulfills(base);
  }

  @Override
  public boolean test(IDataType dataType) {
    if (m_childType == dataType) {
      return true;
    }
    if (m_childIsArray) {
      if (dataType.flavor() == DataTypeFlavor.Array) {
        if (m_childType.arrayDimension() != dataType.arrayDimension()) {
          return false;
        }
        var childArrComponentType = m_childrenSupplier.apply(m_childType).findAny().orElse(null);
        var baseArrComponentType = m_childrenSupplier.apply(dataType).findAny().orElse(null);
        if (childArrComponentType == null || baseArrComponentType == null) {
          return false;
        }
        return new DataTypeAssignableEvaluator(childArrComponentType, m_childrenSupplier).fulfills(baseArrComponentType);
      }
      return false;
    }
    if (m_requiresInstanceOfCheck && isInstanceOfCheckRequired(dataType)) {
      return ((IES6Class) m_childType).isInstanceOf((IES6Class) dataType);
    }
    return false;
  }
}
