/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.datatypedetect;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;

public class PropertyDataTypeDetector {

  private final List<IPropertyDataTypeOverride> m_overrides;

  public PropertyDataTypeDetector(List<IPropertyDataTypeOverride> overrides) {
    m_overrides = overrides;
  }

  public ScoutJsPropertyType detect(IField field) {
    return m_overrides.stream()
        .flatMap(override -> override.overrideType(field).stream())
        .findFirst()
        .orElseGet(() -> new ScoutJsPropertyType(field.dataType().orElse(null)));
  }

  public Map<String, IDataType> unused() {
    return m_overrides.stream()
        .flatMap(o -> o.unused().entrySet().stream())
        .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue, (a, b) -> a));
  }
}
