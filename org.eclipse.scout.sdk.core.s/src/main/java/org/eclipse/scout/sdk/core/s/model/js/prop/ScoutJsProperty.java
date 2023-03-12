/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.prop;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.IScoutJsObject;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.PropertyDataTypeDetector;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class ScoutJsProperty {

  private final FinalValue<ScoutJsPropertyType> m_type;
  private final PropertyDataTypeDetector m_detector; // may be null
  private final IScoutJsObject m_scoutJsObject;
  private final IField m_field;

  public ScoutJsProperty(IScoutJsObject scoutJsObject, IField field, PropertyDataTypeDetector dataTypeDetector) {
    m_scoutJsObject = Ensure.notNull(scoutJsObject);
    m_field = Ensure.notNull(field);
    m_type = new FinalValue<>();
    m_detector = dataTypeDetector;
    if (dataTypeDetector != null) {
      dataTypeDetector.markUsed(field().name());
    }
  }

  public ScoutJsProperty(IScoutJsObject scoutJsObject, IField field, ScoutJsPropertyType type) {
    m_scoutJsObject = Ensure.notNull(scoutJsObject);
    m_field = Ensure.notNull(field);
    m_type = new FinalValue<>();
    m_type.set(Ensure.notNull(type));
    m_detector = null;
  }

  public IField field() {
    return m_field;
  }

  public IScoutJsObject scoutJsObject() {
    return m_scoutJsObject;
  }

  public ScoutJsPropertyType type() {
    return m_type.computeIfAbsentAndGet(this::detectType);
  }

  protected ScoutJsPropertyType detectType() {
    return m_detector.detect(this);
  }

  public String name() {
    return field().name();
  }

  public boolean isObjectType() {
    return ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE.equals(name());
  }

  /**
   * Properties might be declared on several levels in the class hierarchy. E.g. a property 'x' may be declared on
   * Widget and on FormField. In that case basically the FormField declaration (lower in the hierarchy) should win
   * (might be narrowed) unless the specification of the Widget element (higher in the hierarchy) is more specific.
   */
  public static ScoutJsProperty choose(ScoutJsProperty higher, ScoutJsProperty lower) {
    if (lower == null) {
      return higher; // first occurrence
    }
    if (lower.type().dataType().isEmpty() && higher.type().dataType().isPresent()) {
      // higher level is more specific
      return higher;
    }
    return lower;
  }

  public Stream<? extends IScoutJsPropertyValue> computePossibleValues(ScoutJsModel scope) {
    var type = type();
    if (type.isEnumLike()) {
      // FIXME model: add enum support
      return Stream.empty();
    }
    if (type.isBoolean()) {
      return Stream.of(true, false)
          .map(v -> new ScoutJsBooleanPropertyValue(v, this));
    }
    return type
        .classes()
        .flatMap(es6Class -> scope
            .findScoutObjects()
            .withIncludeDependencies(true)
            .withInstanceOf(es6Class)
            .stream())
        .map(o -> new ScoutJsObjectPropertyValue(o, this));
  }

  @Override
  public String toString() {
    return name() + " [type=" + type() + ", object=" + scoutJsObject() + ']';
  }
}