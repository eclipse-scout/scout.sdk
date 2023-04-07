/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.field;

import java.util.Optional;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

/**
 * <h3>{@link IFieldGenerator}</h3>
 * <p>
 * An {@link ISourceGenerator} that creates TypeScript fields.
 *
 * @since 13.0
 */
public interface IFieldGenerator<TYPE extends IFieldGenerator<TYPE>> extends INodeElementGenerator<TYPE> {

  /**
   * @return The data type reference of this {@link IFieldGenerator}.
   */
  Optional<IDataType> dataType();

  /**
   * Sets the data type of this {@link IFieldGenerator}.
   *
   * @param type
   *          The data type of the field or {@code null}.
   * @return This generator.
   */
  TYPE withDataType(IDataType type);
}
