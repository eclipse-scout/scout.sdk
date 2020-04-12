/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

/**
 * <h3>{@link ITranslationStoreSupplier}</h3>
 * <p>
 * Supplier of {@link ITranslationStore}s for a specific {@link Path}.
 * <p>
 * Implementations of this interface must be registered using {@link TranslationStoreStack#SUPPLIERS}.
 *
 * @since 7.0.0
 */
@FunctionalInterface
public interface ITranslationStoreSupplier {
  /**
   * Gets all {@link ITranslationStore}s that are available on specified file {@link Path}. All accessible stores must
   * be returned.
   *
   * @param file
   *          The path for which all accessible stores should be returned. Is never {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Is never {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Is never {@code null}.
   * @return A {@link Stream} holding all {@link ITranslationStore}s that are accessible from the specified path.
   */
  Stream<? extends ITranslationStore> get(Path file, IEnvironment env, IProgress progress);
}
