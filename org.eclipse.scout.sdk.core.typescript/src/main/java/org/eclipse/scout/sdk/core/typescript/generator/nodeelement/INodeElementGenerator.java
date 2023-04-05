/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.nodeelement;

import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;

/**
 * <h3>{@link INodeElementGenerator}</h3>
 *
 * @since 13.0
 */
public interface INodeElementGenerator<TYPE extends INodeElementGenerator<TYPE>> extends ITypeScriptElementGenerator<TYPE> {

  /**
   * Adds the specified modifiers to this {@link INodeElementGenerator}.
   *
   * @param modifiers
   *          The modifiers to add.
   * @return This generator.
   * @see Modifier
   */
  TYPE withModifiers(Collection<Modifier> modifiers);

  /**
   * Adds the specified modifier to this {@link INodeElementGenerator}.
   *
   * @param modifier
   *          The modifier to add.
   * @return This generator.
   * @see Modifier
   */
  default TYPE withModifier(Modifier modifier) {
    return withModifiers(singleton(modifier));
  }

  /**
   * @return The modifiers of this {@link INodeElementGenerator}.
   */
  Collection<Modifier> modifiers();

  /**
   * Removes the specified modifiers from this {@link INodeElementGenerator}.
   *
   * @param modifiers
   *          The modifiers to remove.
   * @return This generator.
   * @see Modifier
   */
  TYPE withoutModifiers(Collection<Modifier> modifiers);

  /**
   * Removes the specified modifier from this {@link INodeElementGenerator}.
   *
   * @param modifier
   *          The modifier to remove.
   * @return This generator.
   * @see Modifier
   */
  default TYPE withoutModifier(Modifier modifier) {
    return withoutModifiers(singleton(modifier));
  }

  /**
   * @return The {@link ITypeScriptElementGenerator} this {@link INodeElementGenerator} will be created in.
   */
  Optional<ITypeScriptElementGenerator<?>> declaringGenerator();
}
