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

import static java.util.stream.Collectors.joining;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.util.Strings;

public class DataTypeNameEvaluator {

  private final Function<IDataType, Stream<IDataType>> m_childrenSupplier;

  public DataTypeNameEvaluator() {
    this(IDataType::childTypes);
  }

  public DataTypeNameEvaluator(Function<IDataType, Stream<IDataType>> childrenSupplier) {
    m_childrenSupplier = childrenSupplier;
  }

  public String eval(IDataType type) {
    if (type == null) {
      return null;
    }
    return switch (type.flavor()) {
      case Array -> m_childrenSupplier.apply(type)
          .findFirst()
          .map(c -> boxComponentDataType(DataTypeFlavor.Array, c)).orElse("") + "[]".repeat(type.arrayDimension());
      case Union -> m_childrenSupplier.apply(type)
          .map(c -> boxComponentDataType(DataTypeFlavor.Union, c))
          .filter(Objects::nonNull)
          .collect(joining(" | "));
      case Intersection -> m_childrenSupplier.apply(type)
          .map(c -> boxComponentDataType(DataTypeFlavor.Intersection, c))
          .filter(Objects::nonNull)
          .collect(joining(" & "));
      case Single -> nameForSimpleDataType(type);
    };
  }

  protected String nameForSimpleDataType(IDataType type) {
    var args = type.typeArguments()
        .map(this::eval)
        .collect(joining(", "));
    if (!Strings.isEmpty(args)) {
      args = "<" + args + ">";
    }
    return nameForLeafType(type) + args;
  }

  protected String nameForLeafType(IDataType type) {
    return type.name();
  }

  protected String boxComponentDataType(DataTypeFlavor parentFlavor, IDataType componentDataType) {
    if (componentDataType == null) {
      return null;
    }
    var componentDataTypeName = eval(componentDataType);
    if (Strings.isBlank(componentDataTypeName)) {
      return null;
    }
    var requiresParentheses = switch (parentFlavor) {
      case Array -> componentDataType.flavor() == DataTypeFlavor.Union || componentDataType.flavor() == DataTypeFlavor.Intersection;
      case Union -> componentDataType.flavor() == DataTypeFlavor.Intersection;
      case Intersection -> componentDataType.flavor() == DataTypeFlavor.Union;
      case Single -> false;
    };
    if (requiresParentheses) {
      return "(" + componentDataTypeName + ")";
    }
    return componentDataTypeName;
  }
}
