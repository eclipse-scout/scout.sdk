/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.builder.java.member;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilderWrapper;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

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
   * {@link IJavaEnvironment} is re-used. Otherwise a new {@link IJavaBuilderContext} without a {@link IJavaEnvironment}
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
    return currentInstance();
  }
}
