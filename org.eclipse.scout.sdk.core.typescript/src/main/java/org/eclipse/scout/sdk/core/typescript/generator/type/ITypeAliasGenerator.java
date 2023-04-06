/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.type;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;

/**
 * <h3>{@link ITypeAliasGenerator}</h3>
 * <p>
 * An {@link ITypeScriptElementGenerator} that creates type aliases.
 *
 * @since 13.0
 */
public interface ITypeAliasGenerator<TYPE extends ITypeAliasGenerator<TYPE>> extends INodeElementGenerator<TYPE> {

  /**
   * @return The aliased type of this {@link ITypeAliasGenerator}.
   */
  Optional<IAliasedTypeGenerator<?>> aliasedType();

  /**
   * Adds the specified aliased type to this {@link ITypeAliasGenerator}.
   *
   * @param generator
   *          Must not be {@code null}.
   * @return This generator.
   * @see TypeGenerator#create()
   */
  TYPE withAliasedType(IAliasedTypeGenerator<?> generator);
}
