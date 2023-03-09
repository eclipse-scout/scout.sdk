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

public record ScoutJsProperty(IScoutJsObject scoutJsObject, IField field, ScoutJsPropertyType type) {

  public ScoutJsProperty(IScoutJsObject scoutJsObject, IField field, PropertyDataTypeDetector dataTypeDetector) {
    this(scoutJsObject, field, dataTypeDetector.detect(field));
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
    if (type.isEnumLike()) {
      // FIXME model: add enum support
      return Stream.empty();
    }
    if (type.isBoolean()) {
      return Stream.of(true, false)
          .map(v -> new ScoutJsBooleanPropertyValue(v, this));
    }
    return type
        .leafClasses()
        .flatMap(es6Class -> scope.findScoutObjects().withIncludeDependencies(true).withInstanceOf(es6Class).stream())
        .map(o -> new ScoutJsObjectPropertyValue(o, this));
  }

  @Override
  public String toString() {
    return name() + " [type=" + type + ", object=" + scoutJsObject + ']';
  }
}
