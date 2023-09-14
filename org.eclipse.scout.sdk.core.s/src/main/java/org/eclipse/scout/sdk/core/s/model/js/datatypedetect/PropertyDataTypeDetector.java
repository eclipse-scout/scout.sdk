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

import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;

/**
 * Class to detect the {@link ScoutJsPropertyType} of a {@link ScoutJsProperty}.
 */
public class PropertyDataTypeDetector {

  private final List<IPropertyDataTypeOverride> m_overrides;

  /**
   * @param overrides
   *          The {@link IPropertyDataTypeOverride overrides} to use. Must not be {@code null}.
   */
  public PropertyDataTypeDetector(List<IPropertyDataTypeOverride> overrides) {
    m_overrides = overrides;
  }

  /**
   * Gets the {@link ScoutJsPropertyType} for the given {@link ScoutJsProperty}.<br>
   * It uses the type declared by an {@link IPropertyDataTypeOverride override} or uses the {@link IDataType} of the
   * {@link IField} declaring the property (see {@link ScoutJsProperty#field()}).
   * 
   * @param property
   *          The {@link ScoutJsProperty} for which the {@link ScoutJsPropertyType} should be detected. Must not be
   *          {@code null}.
   * @return The {@link ScoutJsPropertyType}. Never returns {@code null}.
   */
  public ScoutJsPropertyType detect(ScoutJsProperty property) {
    return m_overrides.stream()
        .flatMap(override -> override.getOverrideFor(property).stream())
        .findFirst()
        .orElseGet(() -> new ScoutJsPropertyType(property.field().dataType().orElse(null), property));
  }

  /**
   * Informs this detector that a property with given name has been found. This is necessary to detect if a property
   * override is unused (see {@link #unused()}).
   *
   * @param propertyName
   *          The property name. Must not be {@code null}.
   */
  public void markUsed(String propertyName) {
    m_overrides.forEach(o -> o.markUsed(propertyName));
  }

  /**
   * @return All the properties that are known to this detector for which {@link #markUsed(String)} has never been
   *         called. The map contains the property name as key and the {@link IDataType} to which this property would
   *         have been overridden (the override type).
   */
  public Map<String, IDataType> unused() {
    return m_overrides.stream()
        .flatMap(o -> o.unused().entrySet().stream())
        .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue, (a, b) -> a));
  }
}
