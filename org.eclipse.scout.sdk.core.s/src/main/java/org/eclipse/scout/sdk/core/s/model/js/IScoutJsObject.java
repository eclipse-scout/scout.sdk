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

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.util.Strings;

public interface IScoutJsObject {

  ScoutJsModel scoutJsModel();

  String name();

  default String shortName() {
    if (ScoutJsCoreConstants.NAMESPACE.equals(scoutJsModel().namespace().orElse(null))) {
      return name();
    }
    return qualifiedName();
  }

  default String qualifiedName() {
    return toQualifiedName(scoutJsModel().namespace().orElse(null), name());
  }

  static String toQualifiedName(String namespace, String objectName) {
    if (Strings.isEmpty(objectName)) {
      return null;
    }
    var result = new StringBuilder();
    if (Strings.hasText(namespace)) {
      result.append(namespace).append('.');
    }
    result.append(objectName);
    return result.toString();
  }

  default boolean hasProperty(String name) {
    return findProperties()
        .withSuperClasses(true)
        .withName(name)
        .existsAny();
  }

  IES6Class declaringClass();

  Map<String, ScoutJsProperty> properties();

  default ScoutJsPropertyQuery findProperties() {
    return new ScoutJsPropertyQuery(this);
  }

  List<IFunction> _inits();
}
