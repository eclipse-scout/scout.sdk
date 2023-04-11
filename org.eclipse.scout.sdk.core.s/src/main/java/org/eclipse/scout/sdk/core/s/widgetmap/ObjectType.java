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

import static java.util.Optional.empty;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ObjectType extends Type {

  private final IES6Class m_es6Class;
  private WidgetMap m_widgetMap = null;
  private ColumnMap m_columnMap = null;

  protected ObjectType(IES6Class es6Class, Collection<String> usedNames) {
    super(Ensure.notNull(usedNames));
    m_es6Class = Ensure.notNull(es6Class);
  }

  public static Optional<ObjectType> create(IES6Class es6Class, Collection<String> usedNames) {
    if (es6Class == null || usedNames == null) {
      return empty();
    }
    return Optional.of(new ObjectType(es6Class, usedNames));
  }

  public IES6Class es6Class() {
    return m_es6Class;
  }

  public boolean isInstanceOf(IES6Class es6Class) {
    return es6Class().isInstanceOf(es6Class);
  }

  @Override
  public ObjectType withNewClassName(CharSequence newClassName) {
    super.withNewClassName(newClassName);
    return this;
  }

  public Optional<WidgetMap> widgetMap() {
    return Optional.ofNullable(m_widgetMap)
        .filter(wm -> !wm.elements().isEmpty());
  }

  public ObjectType withWidgetMap(WidgetMap widgetMap) {
    m_widgetMap = widgetMap;
    return this;
  }

  public Optional<ColumnMap> columnMap() {
    return Optional.ofNullable(m_columnMap)
        .filter(cm -> !cm.elements().isEmpty());
  }

  public ObjectType withColumnMap(ColumnMap columnMap) {
    m_columnMap = columnMap;
    return this;
  }

  public ObjectType withNewClassNameAndMaps(CharSequence newClassName, IObjectLiteral model) {
    withNewClassName(newClassName);

    WidgetMap.create(newClassName().orElse(null), model, usedNames()).ifPresent(this::withWidgetMap);
    ColumnMap.create(newClassName().orElse(null), model, usedNames()).ifPresent(this::withColumnMap);

    return this;
  }
}
