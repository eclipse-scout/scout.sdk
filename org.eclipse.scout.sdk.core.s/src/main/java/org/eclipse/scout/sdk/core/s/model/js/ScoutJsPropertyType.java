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

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ScoutJsPropertyType {

  private final IDataType m_dataType;
  private final ScoutJsPropertySubType m_subType;

  public ScoutJsPropertyType(IDataType dataType, ScoutJsPropertySubType subType) {
    m_dataType = dataType;
    m_subType = Ensure.notNull(subType);
  }

  public ScoutJsPropertyType(IDataType dataType) {
    this(dataType, ScoutJsPropertySubType.NO);
  }

  @Override
  public String toString() {
    var toStringBuilder = new StringBuilder(dataType().map(IDataType::name).orElse("unknown"));
    if (subType() != ScoutJsPropertySubType.NO) {
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
}
