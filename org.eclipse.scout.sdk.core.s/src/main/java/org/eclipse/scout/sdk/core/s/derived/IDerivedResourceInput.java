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
