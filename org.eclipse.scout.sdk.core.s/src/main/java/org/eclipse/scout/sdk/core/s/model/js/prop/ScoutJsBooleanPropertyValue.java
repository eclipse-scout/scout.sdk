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

/**
 * {@link Boolean} value for a {@link ScoutJsProperty} of type {@link ScoutJsPropertyType#isBoolean()}.
 * 
 * @param value
 *          The boolean value
 * @param property
 *          The owner property
 */
public record ScoutJsBooleanPropertyValue(boolean value, ScoutJsProperty property) implements IScoutJsPropertyValue {
  @Override
  public String name() {
    return Boolean.toString(value);
  }
}
