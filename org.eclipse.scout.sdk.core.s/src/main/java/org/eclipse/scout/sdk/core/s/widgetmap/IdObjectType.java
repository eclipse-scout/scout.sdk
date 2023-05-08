/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import static java.util.Optional.empty;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.Ensure;

public class IdObjectType {

  private final String m_id;
  private final ObjectType m_objectType;

  protected IdObjectType(String id, ObjectType objectType) {
    m_id = Ensure.notNull(id);
    m_objectType = Ensure.notNull(objectType);
  }

  public static Optional<IdObjectType> create(String id, IES6Class es6Class, Collection<String> usedNames) {
    if (id == null) {
      return empty();
    }
    return ObjectType.create(es6Class, usedNames).map(e -> new IdObjectType(id, e));
  }

  public static Optional<IdObjectType> create(IObjectLiteral objectLiteral, Collection<String> usedNames, ScoutJsModel scoutJsModel) {
    return Optional.ofNullable(objectLiteral)
        .flatMap(ol -> create(
            IdObjectTypeMapUtils.getId(ol, scoutJsModel).orElse(null),
            IdObjectTypeMapUtils.getObjectType(ol).orElse(null),
            usedNames));
  }

  public String id() {
    return m_id;
  }

  public ObjectType objectType() {
    return m_objectType;
  }
}
