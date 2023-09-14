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

import org.eclipse.scout.sdk.core.s.model.js.enums.ConstantValueUnionScoutEnum;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;

/**
 * Possible constant value for a {@link ConstantValueUnionScoutEnum}
 * 
 * @param value
 *          The constant. Must not be {@code null}.
 * @param property
 *          The owner property. Must not be {@code null}.
 */
public record ScoutJsConstantValuePropertyValue(IConstantValue value, ScoutJsProperty property) implements IScoutJsPropertyValue {
  @Override
  public String name() {
    return value.value().map(Object::toString).orElse(null);
  }
}
