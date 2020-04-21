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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Main access for Scout translation related data.
 */
public final class TranslationStores {

  private TranslationStores() {
  }

  private static final Set<ITranslationStoreSupplier> SUPPLIERS = new HashSet<>();
  private static final Map<String /*npm dependency name*/, String /* UiTextContributor FQN */> UI_TEXT_CONTRIBUTORS = new HashMap<>();
  static {
    //noinspection HardcodedFileSeparator
    UI_TEXT_CONTRIBUTORS.put("@eclipse-scout/core", IScoutRuntimeTypes.UiTextContributor);
  }

  /**
   * Registers a new UiTextContributor mapping
   * 
   * @param ownerNodeModuleName
   *          The Node module name for which the contributor is created
   * @param contributorFqn
   *          The fully qualified Java class name of the contributor.
   * @return {@code true} if there was already a mapping for this Node module registered (and has been replaced).
   */
  public static synchronized boolean registerUiTextContributor(String ownerNodeModuleName, String contributorFqn) {
    return UI_TEXT_CONTRIBUTORS.put(Ensure.notBlank(ownerNodeModuleName), Ensure.notBlank(contributorFqn)) != null;
  }

  /**
   * Removes the UiTextContributor mapping for the given Node module.
   * 
   * @param ownerNodeModuleName
   *          The Node module name for which the contributor should be removed.
   * @return {@code true} if there was a mapping and has been removed. {@code false} if there was no mapping already.
   */
  public static synchronized boolean removeUiTextContributor(String ownerNodeModuleName) {
    return UI_TEXT_CONTRIBUTORS.remove(ownerNodeModuleName) != null;
  }

  /**
   * @return A copy of all currently registered 'Node module name' to 'UiTextContributor' mappings.
   */
  public static synchronized Map<String, String> uiTextContributorMappings() {
    return new HashMap<>(UI_TEXT_CONTRIBUTORS);
  }

  /**
   * Registers a new {@link ITranslationStoreSupplier}
   * 
   * @param supplier
   *          The new supplier. Must not be {@code null}.
   * @return {@code true} if this element has not been registred yet. {@code false} if it was already registered and
   *         therefore has been replaced.
   */
  public static synchronized boolean registerStoreSupplier(ITranslationStoreSupplier supplier) {
    return SUPPLIERS.add(supplier);
  }

  /**
   * Removes a {@link ITranslationStoreSupplier}.
   * 
   * @param supplier
   *          The supplier to remove.
   * @return {@code true} if supplier was removed. {@code false} if it was not registered and therefore could not be
   *         removed.
   */
  public static synchronized boolean removeStoreSupplier(ITranslationStoreSupplier supplier) {
    return SUPPLIERS.remove(supplier);
  }

  /**
   * @return A copy of all currently registered {@link ITranslationStoreSupplier}s.
   */
  public static synchronized List<ITranslationStoreSupplier> storeSuppliers() {
    return new ArrayList<>(SUPPLIERS);
  }

  /**
   * Creates a {@link TranslationStoreStack} for a Java/JavaScript module.
   * 
   * @param modulePath
   *          The modulePath for which the stack (containing all accessible stores) should be returned. Points to the
   *          root folder of the Java/JavaScript module. Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return A new {@link TranslationStoreStack} for the specified {@link Path}.
   * @see TranslationStoreStack
   */
  public static Optional<TranslationStoreStack> createFullStack(Path modulePath, IEnvironment env, IProgress progress) {
    Ensure.notNull(modulePath);
    Ensure.notNull(env);
    Ensure.notNull(progress);
    return TranslationStoreStack.create(modulePath, env, progress);
  }

  /**
   * Creates a {@link ITranslationStore} holding all data of this single store.
   * 
   * @param textService
   *          The text provider service for which the data should be loaded. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return An {@link Optional} holding the store if it could be parsed for the {@link IType} given.
   */
  public static Optional<? extends ITranslationStore> create(IType textService, IProgress progress) {
    Ensure.notNull(textService);
    Ensure.notNull(progress);
    int ticksBySupplier = 1000;
    List<ITranslationStoreSupplier> suppliers = storeSuppliers();
    progress.init("Creating translation store", ticksBySupplier * suppliers.size());
    return suppliers.stream()
        .map(supplier -> supplier.single(textService, progress.newChild(ticksBySupplier)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  /**
   * Calculates all {@link ITranslation} keys accessible for a specific module. It takes Scout UiTextContributors into
   * account for JavaScript modules and TextProviderServices for Java modules.
   * 
   * @param modulePath
   *          The path for which the stack (containing all accessible stores) should be returned. Points to the root
   *          folder of the Java/JavaScript module. Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return An {@link Optional} holding the {@link AccessibleTranslationKeys}. The {@link Optional} is empty in case it
   *         is not Scout module (no TextProviderService and no UiTextContributor could be found).
   * @see AccessibleTranslationKeys
   */
  public static Optional<AccessibleTranslationKeys> keysAccessibleForModule(Path modulePath, IEnvironment env, IProgress progress) {
    Ensure.notNull(modulePath);
    Ensure.notNull(env);
    Ensure.notNull(modulePath);
    return AccessibleTranslationKeys.create(modulePath, env, progress);
  }
}
