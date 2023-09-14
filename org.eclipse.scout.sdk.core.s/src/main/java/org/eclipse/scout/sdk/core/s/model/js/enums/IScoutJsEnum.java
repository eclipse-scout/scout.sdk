/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.enums;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.IScoutJsElement;
import org.eclipse.scout.sdk.core.s.model.js.prop.IScoutJsPropertyValue;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsEnumPropertyValue;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable;

/**
 * Represents a Scout enum. There are several possible notation types in JavaScript or TypeScript (static JavaScript
 * object, enum, EnumObject<>, unions with literals, etc.) for enums.
 */
public interface IScoutJsEnum extends IScoutJsElement {

  /**
   * @return The name that references this enum in the source. E.g. {@code FormField.LabelPosition} or
   *         {@code Widget.Style} or {@code Alignment}. May be {@code null} for constant value enums (like
   *         {@code orientation: 'top' | 'right' | 'bottom' | 'left';}).
   */
  default String referenceName() {
    return name();
  }

  /**
   * @return The declaring {@link IES6Class} or the declaring {@link IVariable} or {@code null} if no top level
   *         {@link INodeElement} is available (e.g. for constant value enums like
   *         "{@code orientation: 'top' | 'right' | 'bottom' | 'left';}").
   */
  INodeElement topLevelReference();

  /**
   * @return An unmodifiable {@link List} holding all enum value names.
   */
  List<String> constants();

  /**
   * Computes all possible values of this {@link IScoutJsEnum} as {@link IScoutJsPropertyValue}. The possible values
   * basically depends on the {@link #constants() enum values} available.
   * 
   * @param property
   *          The owner {@link ScoutJsProperty}. Its {@link ScoutJsProperty#type()} must contain this
   *          {@link IScoutJsEnum} (see {@link ScoutJsPropertyType#scoutJsEnums()}). Must not be {@code null}.
   * @return All possible values of this {@link IScoutJsEnum} as {@link IScoutJsPropertyValue}.
   */
  default Stream<? extends IScoutJsPropertyValue> createPropertyValues(ScoutJsProperty property) {
    return constants().stream().map(c -> new ScoutJsEnumPropertyValue(this, c, property));
  }

  /**
   * Checks if this {@link IScoutJsEnum} fulfills the {@link IDataType} given. In other words: Checks if a value of this
   * {@link IScoutJsEnum} can be assigned to e.g. an {@link IField} having the {@link IDataType} given.<br>
   * It can be used to check if the {@link #constants()} of this {@link IScoutJsEnum} are possible values for the
   * {@link IDataType} given.
   * 
   * @param dataType
   *          The {@link IDataType} to check or {@code null} (in that case always {@code false} is returned).
   * @return {@code true} if this {@link IScoutJsEnum} fulfills the requirements of the given {@link IDataType}.
   */
  boolean fulfills(IDataType dataType);
}
