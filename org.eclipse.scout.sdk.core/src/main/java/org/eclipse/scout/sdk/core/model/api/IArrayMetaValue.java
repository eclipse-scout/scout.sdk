/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

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
