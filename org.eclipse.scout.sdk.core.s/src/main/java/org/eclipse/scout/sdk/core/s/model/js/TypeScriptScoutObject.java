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

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class TypeScriptScoutObject implements IScoutJsObject {

  private final ScoutJsModel m_scoutJsModel;
  private final IES6Class m_class;
  private final IES6Class m_model;
  private final FinalValue<Map<String, ScoutJsProperty>> m_properties;

  protected TypeScriptScoutObject(ScoutJsModel scoutJsModel, IES6Class clazz, IES6Class model) {
    m_scoutJsModel = scoutJsModel;
    m_class = clazz;
    m_model = model;
    m_properties = new FinalValue<>();
  }

  public static Optional<IScoutJsObject> create(ScoutJsModel scoutJsModel, IES6Class clazz) {
    return Optional.ofNullable(clazz)
        .flatMap(c -> c.field("model")
            .flatMap(IField::dataType)
            .filter(IES6Class.class::isInstance)
            .map(IES6Class.class::cast)
            .map(m -> new TypeScriptScoutObject(scoutJsModel, c, m)));
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
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
    var result = model().fields().stream()
        .flatMap(p -> p.dataType().map(dt -> new ScoutJsProperty(this, p, new ScoutJsPropertyType(dt))).stream())
        .collect(toMap(ScoutJsProperty::name, identity()));

    declaringClass().supers().withSuperInterfaces(false).stream()
        .map(scoutJsModel()::scoutObject)
        .filter(Objects::nonNull)
        .forEach(scoutObject -> scoutObject.properties()
            .forEach((propertyName, inheritedProperty) -> result.compute(propertyName, (__, lower) -> chooseProperty(inheritedProperty, lower))));

    return unmodifiableMap(result);
  }

  public IES6Class model() {
    return m_model;
  }

  @Override
  public String name() {
    return declaringClass().name();
  }
}
