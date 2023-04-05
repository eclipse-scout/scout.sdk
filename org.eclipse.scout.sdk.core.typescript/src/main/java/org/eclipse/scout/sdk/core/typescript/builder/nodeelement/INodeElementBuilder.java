/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder.nodeelement;

import java.util.Collection;

import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;

/**
 * <h3>{@link INodeElementBuilder}</h3>
 *
 * @since 13.0
 */
public interface INodeElementBuilder<TYPE extends INodeElementBuilder<TYPE>> extends ITypeScriptSourceBuilder<TYPE> {

  /**
   * Appends the source representation of the given modifiers including a trailing space.
   *
   * @param modifiers
   *          The modifiers to append.
   * @return This builder
   */
  TYPE appendModifiers(Collection<Modifier> modifiers);
}
