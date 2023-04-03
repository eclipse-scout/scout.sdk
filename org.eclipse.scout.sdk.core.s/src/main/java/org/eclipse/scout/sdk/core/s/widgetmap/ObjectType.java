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

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ObjectType {

  private final IES6Class m_es6Class;
  private String m_newClassName = null;
  private WidgetMap m_widgetMap = null;
  private ColumnMap m_columnMap = null;

  protected ObjectType(IES6Class es6Class) {
    m_es6Class = Ensure.notNull(es6Class);
  }

  public static Optional<ObjectType> create(IES6Class es6Class) {
    if (es6Class == null) {
      return empty();
    }
    return Optional.of(new ObjectType(es6Class));
  }

  public IES6Class es6Class() {
    return m_es6Class;
  }

  public boolean isInstanceOf(IES6Class es6Class) {
    return es6Class().isInstanceOf(es6Class);
  }

  public Optional<String> newClassName() {
    return Optional.ofNullable(m_newClassName);
  }

  public ObjectType withNewClassName(String newClassName) {
    m_newClassName = newClassName;
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
}
