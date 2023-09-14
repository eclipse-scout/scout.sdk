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

/**
 * Evaluator to test if a datatype fulfills a given {@link Predicate}.<br>
 * The evaluator handles all {@link IDataType#flavor() flavors} and uses the given children supplier to step into union
 * and intersection data types. The {@link Predicate} is called for arrays and single flavors only.
 */
public class DataTypeFulfillsEvaluator {

  private final Predicate<IDataType> m_evalStrategy;
  private final Function<IDataType, Stream<IDataType>> m_childrenSupplier;

  public DataTypeFulfillsEvaluator(Predicate<IDataType> evalStrategy) {
    this(evalStrategy, IDataType::childTypes);
  }

  public DataTypeFulfillsEvaluator(Predicate<IDataType> evalStrategy, Function<IDataType, Stream<IDataType>> childrenSupplier) {
    m_evalStrategy = evalStrategy;
    m_childrenSupplier = childrenSupplier;
  }

  /**
   * Checks if the given {@link IDataType} fulfills the {@link Predicate} of this evaluator.
   * 
   * @param dataType
   *          The {@link IDataType} to check. If {@code null}, {@code false} is returned.
   * @return {@code true} if the given {@link IDataType} fulfills the {@link Predicate} of this evaluator.
   */
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
    return m_childrenSupplier.apply(dataType).anyMatch(this::fulfills);
  }

  protected boolean checkIntersection(IDataType dataType) {
    return m_childrenSupplier.apply(dataType).allMatch(this::fulfills);
  }

  protected boolean checkArray(IDataType dataType) {
    return m_evalStrategy.test(dataType);
  }

  protected boolean checkSingle(IDataType dataType) {
    // direct check first
    if (m_evalStrategy.test(dataType)) {
      return true;
    }
    // continue with children if not matched (e.g. for a TypeAlias or TypeOfType)
    return checkUnion(dataType);
  }
}
