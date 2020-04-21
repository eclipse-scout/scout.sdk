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
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

/**
 * <h3>{@link ITranslationStoreSupplier}</h3>
 * <p>
 * Supplier of {@link ITranslationStore}s.
 * <p>
 * Implementations of this interface must be registered using
 * {@link TranslationStores#registerStoreSupplier(ITranslationStoreSupplier)}.
 *
 * @since 7.0.0
 */
public interface ITranslationStoreSupplier {

  /**
   * Gets all {@link ITranslationStore}s that are available for the module at the {@link Path} specified.
   * 
   * @param modulePath
   *          The path for which all accessible stores should be returned. Points to the root folder of the
   *          Java/JavaScript module. Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return
   */
  Stream<? extends ITranslationStore> all(Path modulePath, IEnvironment env, IProgress progress);

  /**
   * Creates a {@link ITranslationStore} holding all data of this single store.
   * 
   * @param textService
   *          The text provider service for which the data should be loaded. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return An {@link Optional} holding the store if it could be parsed for the {@link IType} given.
   */
  Optional<? extends ITranslationStore> single(IType textService, IProgress progress);
}
