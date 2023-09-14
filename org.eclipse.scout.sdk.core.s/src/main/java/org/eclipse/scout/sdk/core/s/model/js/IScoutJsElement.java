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

import org.eclipse.scout.sdk.core.s.model.js.enums.IScoutJsEnum;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Represents a Scout JS element (like a {@link IScoutJsObject} or {@link IScoutJsEnum}).
 */
public interface IScoutJsElement {

  /**
   * @return The {@link ScoutJsModel} this element belongs to. Is never {@code null}.
   */
  ScoutJsModel scoutJsModel();

  /**
   * @return The name of the element.
   */
  String name();

  /**
   * @return The {@link IES6Class} this element is based on or {@code null} if it does not belong to a class (e.g. is a
   *         top level variable).
   */
  IES6Class declaringClass();

  /**
   * Computes the qualified name (namespace + '.' + element name)
   * 
   * @param namespace
   *          The namespace or {@code null}.
   * @param elementName
   *          The element name or {@code null}.
   * @return The qualified name based on the given parts or {@code null} if elementName is empty or {@code null}.
   */
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
