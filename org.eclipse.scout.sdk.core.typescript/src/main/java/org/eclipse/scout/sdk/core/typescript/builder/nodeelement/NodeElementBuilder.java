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
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.TypeScriptSourceBuilderWrapper;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;

/**
 * <h3>{@link NodeElementBuilder}</h3>
 *
 * @since 13.0
 */
public class NodeElementBuilder<TYPE extends INodeElementBuilder<TYPE>> extends TypeScriptSourceBuilderWrapper<TYPE> implements INodeElementBuilder<TYPE> {

  protected NodeElementBuilder(ISourceBuilder<?> inner) {
    super(inner);
  }

  /**
   * Creates a new {@link INodeElementBuilder} wrapping the given inner {@link ISourceBuilder}.
   *
   * @param inner
   *          The inner {@link ISourceBuilder}. Must not be {@code null}.
   * @return A new {@link INodeElementBuilder}.
   */
  public static INodeElementBuilder<?> create(ISourceBuilder<?> inner) {
    return new NodeElementBuilder<>(inner);
  }

  @Override
  public TYPE appendModifiers(Collection<Modifier> modifiers) {
    if (modifiers != null && !modifiers.isEmpty()) {
      append(modifiers.stream()
          .map(Modifier::keyword)
          .collect(Collectors.joining(" ")))
              .space();
    }
    return thisInstance();
  }
}
