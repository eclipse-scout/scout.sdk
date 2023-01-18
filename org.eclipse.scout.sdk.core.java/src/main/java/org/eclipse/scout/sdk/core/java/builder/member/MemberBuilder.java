/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.builder.member;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaSourceBuilderWrapper;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link MemberBuilder}</h3>
 *
 * @since 6.1.0
 */
public class MemberBuilder<TYPE extends IMemberBuilder<TYPE>> extends JavaSourceBuilderWrapper<TYPE> implements IMemberBuilder<TYPE> {

  protected MemberBuilder(ISourceBuilder<?> inner) {
    super(inner);
  }

  /**
   * Creates a new {@link IMemberBuilder} wrapping the given inner {@link ISourceBuilder}.
   * <p>
   * If the context of the inner {@link ISourceBuilder} is an {@link IJavaBuilderContext}, this context and its
   * {@link IJavaEnvironment} is re-used. Otherwise, a new {@link IJavaBuilderContext} without a {@link IJavaEnvironment}
   * is created.
   *
   * @param inner
   *          The inner {@link ISourceBuilder}. Must not be {@code null}.
   * @return A new {@link IMemberBuilder}.
   */
  public static IMemberBuilder<?> create(ISourceBuilder<?> inner) {
    return new MemberBuilder<>(inner);
  }

  @Override
  public TYPE appendFlags(int flags) {
    append(Flags.toString(flags, true));
    return thisInstance();
  }
}
