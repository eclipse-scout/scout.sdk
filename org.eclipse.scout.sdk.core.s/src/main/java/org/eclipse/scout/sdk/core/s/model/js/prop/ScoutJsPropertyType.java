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

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ScoutJsPropertyType {

  private final IDataType m_dataType;
  private final ScoutJsPropertySubType m_subType;

  public ScoutJsPropertyType(IDataType dataType, ScoutJsPropertySubType subType) {
    m_dataType = dataType; // dataType may be null in case the property is based on a Field and the field has no datatype (cannot be detected. e.g. in JavaScript: this.myField = null)
    m_subType = Ensure.notNull(subType);
  }

  public ScoutJsPropertyType(IDataType dataType) {
    this(dataType, ScoutJsPropertySubType.NOTHING);
  }

  @Override
  public String toString() {
    var toStringBuilder = new StringBuilder(dataType().map(IDataType::name).orElse("unknown"));
    if (subType() != ScoutJsPropertySubType.NOTHING) {
      toStringBuilder.append(" (sub-type=").append(subType()).append(")");
    }
    return toStringBuilder.toString();
  }

  public ScoutJsPropertySubType subType() {
    return m_subType;
  }

  public Optional<IDataType> dataType() {
    return Optional.ofNullable(m_dataType);
  }

  public boolean isEnumLike() {
    var datatype = dataType().orElse(null);
    if (datatype == null) {
      return false;
    }
    if (datatype.flavor() != DataTypeFlavor.Single) {
      return false;
    }
    if (datatype instanceof IES6Class es6Class) {
      return es6Class.isEnum();
    }
    // FIXME model: add enum support
    return false;
  }

  public boolean isArray() {
    return m_dataType != null && m_dataType.flavor() == DataTypeFlavor.Array;
  }

  public boolean hasLeafClasses() {
    return leafClasses().findAny().isPresent();
  }

  public Stream<IES6Class> leafClasses() {
    return dataType()
        .map(IDataType::leafTypes)
        .orElseGet(Stream::empty)
        .filter(t -> t instanceof IES6Class)
        .map(IES6Class.class::cast);
  }

  public boolean isBoolean() {
    return TypeScriptTypes._boolean.equals(dataTypeName());
  }

  protected String dataTypeName() {
    return dataType().map(IDataType::name).orElse(null);
  }
}
