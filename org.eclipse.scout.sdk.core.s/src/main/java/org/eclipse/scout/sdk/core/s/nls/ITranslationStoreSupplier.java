/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScoutInterfaceApi.ITextProviderService;
import org.eclipse.scout.sdk.core.s.apidef.IScoutInterfaceApi.IUiTextContributor;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

/**
 * <h3>{@link ITranslationStoreSupplier}</h3>
 * <p>
 * Supplier of {@link ITranslationStore}s. For each type of store a corresponding supplier must be provided.
 * <p>
 * E.g. for stores based on properties files (resource bundle) a supplier may be provided. Or for stores based on
 * translations in the database a supplier may be provided.
 * <p>
 * Implementations of this interface must be registered using
 * {@link Translations#registerStoreSupplier(ITranslationStoreSupplier)}.
 *
 * @since 7.0.0
 */
public interface ITranslationStoreSupplier {

  /**
   * Gets all {@link ITranslationStore}s that are visible for the module at the {@link Path} specified.
   * 
   * @param modulePath
   *          The path for which all accessible stores should be returned. Points to the root folder of the
   *          Java/JavaScript module (the folder that contains the pom.xml/package.json). Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return A {@link Stream} with all stores the supplier can provide for the path given. Is never {@code null}.
   */
  Stream<ITranslationStore> visibleStoresForJavaModule(Path modulePath, IEnvironment env, IProgress progress);

  /**
   * Gets all {@link IUiTextContributor} types visible for the module at the {@link Path} specified.
   * 
   * @param modulePath
   *          The path for which all accessible contributors should be returned. Points to the root folder of the
   *          Java/JavaScript module (the folder that contains the pom.xml/package.json). Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return A {@link Stream} with all contributors accessible to the module given.
   */
  Stream<IType> visibleTextContributorsForJavaModule(Path modulePath, IEnvironment env, IProgress progress);

  /**
   * Creates a {@link ITranslationStore} holding all data of this single store.
   * 
   * @param textService
   *          The {@link ITextProviderService} for which the data should be loaded. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return An {@link Optional} holding the store if it could be parsed for the {@link IType} given.
   */
  Optional<ITranslationStore> createStoreForService(IType textService, IProgress progress);
}
