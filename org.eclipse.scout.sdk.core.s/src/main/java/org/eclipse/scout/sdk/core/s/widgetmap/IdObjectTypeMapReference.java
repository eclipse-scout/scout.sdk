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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;

public class IdObjectTypeMapReference {

  private final IES6Class m_es6Class;
  private final IdObjectTypeMap m_idObjectTypeMap;

  protected IdObjectTypeMapReference(IES6Class es6Class, IdObjectTypeMap idObjectTypeMap) {
    m_es6Class = es6Class;
    m_idObjectTypeMap = idObjectTypeMap;
  }

  public static Optional<IdObjectTypeMapReference> create(IES6Class es6Class) {
    return Optional.ofNullable(es6Class)
        .map(c -> new IdObjectTypeMapReference(c, null));
  }

  public static Optional<IdObjectTypeMapReference> create(IdObjectTypeMap idObjectTypeMap) {
    return Optional.ofNullable(idObjectTypeMap)
        .map(wm -> new IdObjectTypeMapReference(null, wm));
  }

  protected Optional<IES6Class> es6Class() {
    return Optional.ofNullable(m_es6Class);
  }

  protected Optional<IdObjectTypeMap> idObjectTypeMap() {
    return Optional.ofNullable(m_idObjectTypeMap);
  }

  public String name() {
    return es6Class().map(IES6Class::name)
        .or(() -> idObjectTypeMap().map(IdObjectTypeMap::name))
        .orElseThrow();
  }

  public IDataType reference() {
    return es6Class()
        .<IDataType> map(Function.identity())
        .orElseGet(() -> m_idObjectTypeMap.model().spi().createDataType(name()).api());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    var other = (IdObjectTypeMapReference) obj;
    return Objects.equals(m_es6Class, other.m_es6Class)
        && Objects.equals(m_idObjectTypeMap, other.m_idObjectTypeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_es6Class, m_idObjectTypeMap);
  }
}
