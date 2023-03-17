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

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.util.Strings;

public interface IScoutJsElement {

  ScoutJsModel scoutJsModel();

  String name();

  IES6Class declaringClass();

  static String toQualifiedName(String namespace, String elementName) {
    if (Strings.isEmpty(elementName)) {
      return null;
    }
    var result = new StringBuilder();
    if (Strings.hasText(namespace)) {
      result.append(namespace).append('.');
    }
    result.append(elementName);
    return result.toString();
  }
}
