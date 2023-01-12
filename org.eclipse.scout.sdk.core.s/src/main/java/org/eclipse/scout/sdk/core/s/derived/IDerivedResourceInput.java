/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.derived;

import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;

/**
 * <h3>{@link IDerivedResourceInput}</h3>
 *
 * @since 7.0.0
 */
public interface IDerivedResourceInput {

  Optional<IType> getSourceType(IEnvironment env);

  /**
   * Does not search in the {@link IJavaEnvironment} of the given {@link IType}. Instead returns the "closest" (minimal)
   * {@link IJavaEnvironment} in which the given {@link IType} is included.
   *
   * @param t
   *          The {@link IType} for which the source folder should be returned.
   * @param env
   *          The {@link IEnvironment} to use. Must not be {@code null}.
   * @return The source folder in which the specified type exists.
   */
  Optional<IClasspathEntry> getSourceFolderOf(IType t, IEnvironment env);
}
