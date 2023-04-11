/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue.ConstantValueType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

public class ColumnMap extends IdObjectTypeMap {

  private final FinalValue<Optional<IES6Class>> m_tableClass = new FinalValue<>();
  private final FinalValue<Optional<IES6Class>> m_columnClass = new FinalValue<>();

  protected ColumnMap(String name, IObjectLiteral model, Collection<String> usedNames) {
    super(name, model, usedNames);
  }

  public static Optional<ColumnMap> create(String tableOrModelName, IObjectLiteral tableModel, Collection<String> usedNames) {
    if (tableOrModelName == null || tableModel == null) {
      return empty();
    }
    return Optional.of(tableOrModelName)
        .map(n -> Strings.removeSuffix(n, ScoutJsCoreConstants.CLASS_NAME_SUFFIX_MODEL))
        .map(n -> n + ScoutJsCoreConstants.CLASS_NAME_SUFFIX_COLUMN_MAP)
        .map(n -> new ColumnMap(n, tableModel, usedNames));
  }

  protected Optional<IES6Class> tableClass() {
    return m_tableClass.computeIfAbsentAndGet(() -> classByObjectType(ScoutJsCoreConstants.CLASS_NAME_TABLE));
  }

  protected Optional<IES6Class> columnClass() {
    return m_columnClass.computeIfAbsentAndGet(() -> classByObjectType(ScoutJsCoreConstants.CLASS_NAME_COLUMN));
  }

  @Override
  protected Map<String, IdObjectType> parseElements() {
    var columnClass = columnClass().orElse(null);
    if (columnClass == null) {
      return emptyMap();
    }

    return model().property(ScoutJsCoreConstants.PROPERTY_NAME_COLUMNS)
        .flatMap(IConstantValue::asArray).stream()
        .flatMap(Stream::of)
        .filter(cv -> cv.type() == ConstantValueType.ObjectLiteral)
        .flatMap(cv -> cv.asObjectLiteral().stream())
        .map(ol -> IdObjectType.create(ol, usedNames()))
        .flatMap(Optional::stream)
        .filter(idObjectType -> idObjectType.objectType().isInstanceOf(columnClass))
        .collect(Collectors.toMap(IdObjectType::id, identity(), (a, b) -> {
          createDuplicateIdWarning(a.id());
          return b;
        }, LinkedHashMap::new));
  }

  @Override
  protected Set<IdObjectTypeMapReference> parseIdObjectTypeMapReferences() {
    var tableClass = tableClass().orElse(null);
    return model().property(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE)
        .flatMap(IConstantValue::asES6Class)
        .filter(es6Class -> es6Class != tableClass)
        .flatMap(es6Class -> es6Class.field(ScoutJsCoreConstants.PROPERTY_NAME_COLUMN_MAP))
        .flatMap(IField::dataType)
        .filter(IES6Class.class::isInstance)
        .map(IES6Class.class::cast)
        .flatMap(IdObjectTypeMapReference::create)
        .stream()
        .collect(Collectors.toSet());
  }
}
