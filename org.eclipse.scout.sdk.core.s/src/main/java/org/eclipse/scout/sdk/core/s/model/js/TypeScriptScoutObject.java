/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class TypeScriptScoutObject implements IScoutJsObject {

  private final IES6Class m_class;
  private final IES6Class m_model;
  private final FinalValue<Map<String, ScoutJsProperty>> m_properties;

  protected TypeScriptScoutObject(IES6Class clazz, IES6Class model) {
    m_class = clazz;
    m_model = model;
    m_properties = new FinalValue<>();
  }

  public static Optional<IScoutJsObject> create(IES6Class clazz) {
    return Optional.ofNullable(clazz)
        .flatMap(c -> c.field("model")
            .flatMap(IField::dataType)
            .filter(m -> m instanceof IES6Class)
            .map(m -> (IES6Class) m)
            .map(m -> new TypeScriptScoutObject(c, m)));
  }

  @Override
  public IES6Class declaringClass() {
    return m_class;
  }

  @Override
  public Map<String, ScoutJsProperty> properties() {
    return m_properties.computeIfAbsentAndGet(this::parseProperties);
  }

  protected Map<String, ScoutJsProperty> parseProperties() {
    return model().fields().stream()
        .flatMap(p -> p.dataType().map(dt -> new ScoutJsProperty(p, new ScoutJsPropertyType(dt))).stream())
        .collect(toUnmodifiableMap(p -> p.field().name(), identity()));
  }

  public IES6Class model() {
    return m_model;
  }

  @Override
  public String name() {
    return declaringClass().name();
  }
}
