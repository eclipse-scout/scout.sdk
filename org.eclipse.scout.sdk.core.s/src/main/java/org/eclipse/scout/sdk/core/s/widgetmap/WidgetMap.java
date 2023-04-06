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
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.visitor.DefaultDepthFirstVisitor;
import org.eclipse.scout.sdk.core.util.visitor.TreeTraversals;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

public class WidgetMap extends IdObjectTypeMap {

  private final boolean m_skipTopLevel;

  protected WidgetMap(String name, IObjectLiteral model, boolean skipTopLevel) {
    super(name, model);
    m_skipTopLevel = skipTopLevel;
  }

  public static Optional<WidgetMap> create(String widgetOrModelName, IObjectLiteral widgetModel) {
    return create(widgetOrModelName, widgetModel, false);
  }

  public static Optional<WidgetMap> create(String widgetOrModelName, IObjectLiteral widgetModel, boolean skipTopLevel) {
    if (widgetOrModelName == null || widgetModel == null) {
      return empty();
    }
    return Optional.of(widgetOrModelName)
        .map(n -> Strings.removeSuffix(n, ScoutJsCoreConstants.CLASS_NAME_SUFFIX_MODEL))
        .map(n -> n + ScoutJsCoreConstants.CLASS_NAME_SUFFIX_WIDGET_MAP)
        .map(n -> new WidgetMap(n, widgetModel, skipTopLevel));
  }

  protected boolean isSkipTopLevel() {
    return m_skipTopLevel;
  }

  @Override
  protected Map<String, IdObjectType> parseElements() {
    var widgetClass = widgetClass().orElse(null);
    if (widgetClass == null) {
      return emptyMap();
    }
    var tableClass = classByObjectType(ScoutJsCoreConstants.CLASS_NAME_TABLE);
    var tableFieldClass = classByObjectType(ScoutJsCoreConstants.CLASS_NAME_TABLE_FIELD);

    Map<String, IdObjectType> widgets = new LinkedHashMap<>();

    TreeTraversals
        .create(new DefaultDepthFirstVisitor<>() {

          private final Deque<IObjectLiteral> m_ancestors = new ArrayDeque<>();

          @Override
          public TreeVisitResult preVisit(IObjectLiteral element, int level, int index) {
            m_ancestors.addLast(element);

            if (element == null) {
              return TreeVisitResult.SKIP_SUBTREE;
            }
            if (level == 0 && isSkipTopLevel()) {
              return TreeVisitResult.CONTINUE;
            }

            return IdObjectType.create(element)
                .map(idObjectType -> {
                  if (tableClass
                      .filter(clazz -> idObjectType.objectType().isInstanceOf(clazz))
                      .isPresent()) {
                    var tableName = idObjectType.id().replace(".", "");
                    if (ScoutJsCoreConstants.CLASS_NAME_TABLE.equals(tableName) ||
                        tableName.equals(idObjectType.objectType().es6Class().name())) {
                      m_ancestors.removeLast(); // remove current element
                      tableName = Optional.ofNullable(m_ancestors.peekLast()) // get parent
                          .flatMap(IdObjectType::create)
                          .filter(iot -> tableFieldClass
                              .filter(clazz -> iot.objectType().isInstanceOf(clazz))
                              .isPresent()) // check if parent is a TableField
                          .map(iot -> iot.id().replace(".", "") + ScoutJsCoreConstants.CLASS_NAME_TABLE)
                          .orElse(tableName);
                      m_ancestors.addLast(element); // add current element again
                    }

                    var objectType = idObjectType.objectType().withNewClassName(tableName);

                    create(tableName, element, true).ifPresent(objectType::withWidgetMap);
                    ColumnMap.create(tableName, element).ifPresent(objectType::withColumnMap);

                    widgets.put(idObjectType.id(), idObjectType);

                    return TreeVisitResult.SKIP_SUBTREE;
                  }

                  if (idObjectType.objectType().isInstanceOf(widgetClass)) {
                    widgets.put(idObjectType.id(), idObjectType);
                  }

                  return TreeVisitResult.CONTINUE;
                })
                .orElse(TreeVisitResult.CONTINUE);
          }

          @Override
          public boolean postVisit(IObjectLiteral element, int level, int index) {
            m_ancestors.removeLast();
            return true;
          }
        }, IObjectLiteral::childObjectLiterals)
        .traverse(model());

    return widgets;
  }

  @Override
  protected Set<IdObjectTypeMapReference> parseIdObjectTypeMapReferences() {
    return elements().values().stream()
        .flatMap(element -> element.objectType().widgetMap().flatMap(IdObjectTypeMapReference::create)
            .or(() -> element.objectType().es6Class()
                .field(ScoutJsCoreConstants.PROPERTY_NAME_WIDGET_MAP)
                .flatMap(IField::dataType)
                .filter(IES6Class.class::isInstance)
                .map(IES6Class.class::cast)
                .filter(es6Class -> widgetClass()
                    .filter(not(es6Class::equals))
                    .isPresent())
                .flatMap(IdObjectTypeMapReference::create))
            .stream())
        .collect(toCollection(LinkedHashSet::new));
  }
}
