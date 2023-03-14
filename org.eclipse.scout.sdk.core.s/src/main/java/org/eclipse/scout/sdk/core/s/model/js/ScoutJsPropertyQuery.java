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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;

public class ScoutJsPropertyQuery extends AbstractQuery<ScoutJsProperty> {

  private final IScoutJsObject m_object;

  private String m_name;
  private boolean m_includeSuperClasses;

  public ScoutJsPropertyQuery(IScoutJsObject object) {
    m_object = object;
  }

  protected IScoutJsObject object() {
    return m_object;
  }

  public ScoutJsPropertyQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String name() {
    return m_name;
  }

  public ScoutJsPropertyQuery withSuperClasses(boolean includeSuperClasses) {
    m_includeSuperClasses = includeSuperClasses;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  @Override
  protected Stream<ScoutJsProperty> createStream() {
    var obj = object();
    var properties = new HashMap<>(obj.properties());

    // for each property also check the super classes (it may contain the same property but with a more detailed data-type)
    // choose the one which is better, even no super-classes should be considered in this query
    var superClasses = obj.declaringClass().supers().stream().toList();
    if (!superClasses.isEmpty()) {
      obj.scoutJsModel().findScoutObjects()
          .withIncludeDependencies(true)
          .withObjectClasses(superClasses)
          .stream()
          .flatMap(superObject -> superObject.properties().values().stream())
          .forEach(inheritedProperty -> updateOrAddSuperPropertyIfNecessary(properties, inheritedProperty));
    }

    var name = name();
    if (name == null) {
      return properties.values().stream();
    }
    return Stream.ofNullable(properties.get(name)); // if name filter is given: only one property can be part of the result
  }

  protected void updateOrAddSuperPropertyIfNecessary(Map<String, ScoutJsProperty> properties, ScoutJsProperty inheritedProperty) {
    BiFunction<String, ScoutJsProperty, ScoutJsProperty> chooseProperty = (name, lower) -> ScoutJsProperty.choose(inheritedProperty, lower);
    if (isIncludeSuperClasses()) {
      // with super classes is requested: choose the better one if existing, add if not yet part of the result
      properties.compute(inheritedProperty.name(), chooseProperty);
    }
    else {
      // super class properties should not be returned. Only update if property is already part of the result and the super class is better
      properties.computeIfPresent(inheritedProperty.name(), chooseProperty);
    }
  }
}
