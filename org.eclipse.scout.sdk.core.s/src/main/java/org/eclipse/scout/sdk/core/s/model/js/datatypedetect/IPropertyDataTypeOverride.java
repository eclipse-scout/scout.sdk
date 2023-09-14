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

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

/**
 * Allows to override the {@link ScoutJsPropertyType} that is used for a {@link ScoutJsProperty} during parse.<br>
 * If an override for a property is available, it wins over the data type detected from the source.
 */
public interface IPropertyDataTypeOverride {
  /**
   * Gets the {@link ScoutJsPropertyType data type} override for the {@link ScoutJsProperty} given.
   * 
   * @param property
   *          The {@link ScoutJsProperty} for which the override should be returned. Must not be {@code null}.
   * @return the override type or an empty {@link Optional} if nothing should be overridden.
   */
  Optional<ScoutJsPropertyType> getOverrideFor(ScoutJsProperty property);

  /**
   * Notifies this {@link IPropertyDataTypeOverride} that a property with given name has been found. This is necessary
   * to detect if a property override is unused (see {@link #unused()}).
   * 
   * @param propertyName
   *          The property name. Must not be {@code null}.
   */
  default void markUsed(String propertyName) {
  }

  /**
   * @return All the properties that are known to this {@link IPropertyDataTypeOverride} for which
   *         {@link #markUsed(String)} has never been called. The map contains the property name as key and the
   *         {@link IDataType} to which this property would have been overridden (the override type).
   */
  default Map<String, IDataType> unused() {
    return emptyMap();
  }
}
