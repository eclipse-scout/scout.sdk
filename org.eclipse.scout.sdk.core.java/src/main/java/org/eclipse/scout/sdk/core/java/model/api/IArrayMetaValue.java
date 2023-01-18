/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

/**
 * <h3>{@link IArrayMetaValue}</h3> Describes an array value.
 *
 * @since 5.1.0
 */
public interface IArrayMetaValue extends IMetaValue {
  /**
   * Gets the array values of a {@link IMetaValue} of type {@link MetaValueType#Array}.
   *
   * @return The segments of the array value.
   */
  IMetaValue[] metaValueArray();
}
