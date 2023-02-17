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

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;

public record ScoutJsProperty(IScoutJsObject scoutJsObject, IField field, ScoutJsPropertyType type) {
  ScoutJsProperty(IScoutJsObject scoutJsObject, IField field, JavaScriptPropertyDataTypeDetector dataTypeDetector) {
    this(scoutJsObject, field, dataTypeDetector.detect(field));
  }

  public String name() {
    return field().name();
  }

  public ScoutJsModel scoutJsModel() {
    return scoutJsObject.scoutJsModel();
  }

  public boolean isWidget(ScoutJsModel scoutJsModel) {
    if (type() == null) {
      return false;
    }

    var dataType = type().dataType().orElse(null);
    if (dataType == ScoutJsModel.DATA_TYPE_WIDGET) {
      return true;
    }

    if (dataType instanceof IES6Class es6Class && scoutJsModel != null) {
      return scoutJsModel.scoutWidgetClass()
          .map(es6Class::isInstanceOf)
          .orElse(false);
    }

    return false;
  }

  public boolean isObjectType() {
    return ScoutJsModel.OBJECT_TYPE_PROPERTY_NAME.equals(name());
  }

  public boolean isEnum() {
    if (type() == null) {
      return false;
    }

    // FIXME model: add enum support
    if (type().dataType().orElse(null)instanceof IES6Class es6Class) {
      return es6Class.isEnum();
    }

    return false;
  }

  // FIXME model: add array support
  @SuppressWarnings("MethodMayBeStatic")
  public boolean isArray() {
    return false;
  }

  public boolean isBoolean() {
    return type.dataType().map(IDataType::name).map(TypeScriptTypes._boolean::equals).orElse(false);
  }

  public List<? extends IScoutJsPropertyValue> values(ScoutJsModel scoutJsModel) {
    if (isEnum()) {
      return emptyList();
    }
    if (isBoolean()) {
      return Stream.of(true, false)
          .map(ScoutJsBooleanPropertyValue::new)
          .toList();
    }

    return type.dataType()
        .filter(IES6Class.class::isInstance)
        .map(IES6Class.class::cast)
        .or(() -> isWidget(scoutJsModel) || isObjectType() ? scoutJsModel.scoutWidgetClass() : Optional.empty())
        .map(es6Class -> scoutJsModel.scoutObjects()
            .filter(sjo -> sjo.declaringClass().isInstanceOf(es6Class))
            .map(ScoutJsObjectPropertyValue::new)
            .toList())
        .orElse(emptyList());
  }
}
