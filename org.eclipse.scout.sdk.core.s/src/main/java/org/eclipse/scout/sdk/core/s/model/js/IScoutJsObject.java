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

import java.util.Map;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;

public interface IScoutJsObject {

  ScoutJsModel scoutJsModel();

  String name();

  default String shortName() {
    if (scoutJsModel().namespace().map(ScoutJsModel.SCOUT_NAMESPACE::equals).orElse(false)) {
      return name();
    }
    return qualifiedName();
  }

  default String qualifiedName() {
    return scoutJsModel().namespace().map(ns -> ns + "." + name()).orElse(name());
  }

  IES6Class declaringClass();

  Map<String, ScoutJsProperty> properties();

  default ScoutJsProperty property(String name) {
    if (name == null) {
      return null;
    }

    return properties().get(name);
  }

  /**
   * Properties might be declared on several levels in the class hierarchy. E.g. a property 'x' may be declared on
   * Widget and on FormField. In that case basically the FormField declaration (lower in the hierarchy) should win
   * (might be narrowed) unless the specification of the Widget element (higher in the hierarchy) is more specific.
   */
  default ScoutJsProperty chooseProperty(ScoutJsProperty higher, ScoutJsProperty lower) {
    if (lower == null) {
      return higher; // first occurrence
    }
    if (lower.type().dataType().isEmpty() && higher.type().dataType().isPresent()) {
      // higher level is more specific
      return higher;
    }
    return lower;
  }
}
