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
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.visitor.DefaultDepthFirstVisitor;
import org.eclipse.scout.sdk.core.util.visitor.TreeTraversals;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

public class WidgetMap extends IdObjectTypeMap {

  private final FinalValue<Optional<IES6Class>> m_tableClass = new FinalValue<>();

  protected WidgetMap(String name, IObjectLiteral model) {
    super(name, model);
  }

  public static Optional<WidgetMap> create(String widgetOrModelName, IObjectLiteral widgetModel) {
    if (widgetOrModelName == null || widgetModel == null) {
      return empty();
    }
    return Optional.of(widgetOrModelName)
        .map(n -> Strings.removeSuffix(n, ScoutJsCoreConstants.CLASS_NAME_SUFFIX_MODEL))
        .map(n -> n + ScoutJsCoreConstants.CLASS_NAME_SUFFIX_WIDGET_MAP)
        .map(n -> new WidgetMap(n, widgetModel));
  }

  @Override
  protected Map<String, IdObjectType> parseElements() {
    if (widgetClass().isEmpty()) {
      return emptyMap();
    }

    Map<String, IdObjectType> widgets = new LinkedHashMap<>();

    TreeTraversals
        .create(new DefaultDepthFirstVisitor<>() {

          private final Deque<IObjectLiteral> m_ancestors = new ArrayDeque<>();

          @Override
          public TreeVisitResult preVisit(IObjectLiteral element, int level, int index) {
            var result = TreeVisitResult.CONTINUE;

            if (element == null) {
              result = TreeVisitResult.SKIP_SUBTREE;
            }
            else if (level > 0) {
              result = collectIdObjectType(
                  element,
                  m_ancestors,
                  idObjectType -> Optional.ofNullable(widgets.put(idObjectType.id(), idObjectType))
                      .ifPresent((iot) -> createDuplicateIdWarning(iot.id())));
            }

            m_ancestors.addLast(element);
            return result;
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

  protected Optional<IES6Class> tableClass() {
    return m_tableClass.computeIfAbsentAndGet(() -> classByObjectType(ScoutJsCoreConstants.CLASS_NAME_TABLE));
  }

  protected boolean isWidget(IdObjectType idObjectType) {
    return widgetClass()
        .filter(clazz -> idObjectType.objectType().isInstanceOf(clazz))
        .isPresent();
  }

  protected boolean isTable(IdObjectType idObjectType) {
    return tableClass()
        .filter(clazz -> idObjectType.objectType().isInstanceOf(clazz))
        .isPresent();
  }

  protected TreeVisitResult collectIdObjectType(IObjectLiteral element, Deque<IObjectLiteral> ancestors, Consumer<IdObjectType> collector) {
    var idObjectType = IdObjectType.create(element).orElse(null);
    if (idObjectType == null) {
      return TreeVisitResult.CONTINUE;
    }

    if (isTable(idObjectType)) {
      handleTable(element, idObjectType, ancestors);
      collector.accept(idObjectType);
      return TreeVisitResult.SKIP_SUBTREE;
    }

    if (isWidget(idObjectType)) {
      collector.accept(idObjectType);
    }

    return TreeVisitResult.CONTINUE;
  }

  @SuppressWarnings("MethodMayBeStatic")
  protected void handleTable(IObjectLiteral element, IdObjectType idObjectType, Deque<IObjectLiteral> ancestors) {
    var name = idObjectType.id().replace(".", "");
    if (ScoutJsCoreConstants.CLASS_NAME_TABLE.equals(name) || name.equals(idObjectType.objectType().es6Class().name())) {
      name = Optional.ofNullable(ancestors.peekLast()) // get parent
          .flatMap(IdObjectType::create)
          .map(iot -> iot.id().replace(".", "") + ScoutJsCoreConstants.CLASS_NAME_TABLE)
          .orElse(name);
    }

    var objectType = idObjectType.objectType()
        .withNewClassName(name);

    WidgetMap.create(name, element).ifPresent(objectType::withWidgetMap);
    ColumnMap.create(name, element).ifPresent(objectType::withColumnMap);
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
