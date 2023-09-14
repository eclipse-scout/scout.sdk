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

import org.eclipse.scout.sdk.core.s.model.js.enums.IScoutJsEnum;

/**
 * {@link IScoutJsPropertyValue} for an {@link IScoutJsEnum} element.
 * 
 * @param scoutJsEnum
 *          The owning {@link IScoutJsEnum}. Must not be {@code null}.
 * @param enumConstant
 *          The constant element name. Must not be {@code null}.
 * @param property
 *          The owner {@link ScoutJsProperty}. Must not be {@code null}.
 */
public record ScoutJsEnumPropertyValue(IScoutJsEnum scoutJsEnum, String enumConstant, ScoutJsProperty property) implements IScoutJsPropertyValue {
  @Override
  public String name() {
    return scoutJsEnum.referenceName() + '.' + enumConstant;
  }
}
