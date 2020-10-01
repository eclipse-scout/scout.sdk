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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutChartApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.apidef.IClassNameSupplier;

/**
 * Main access for Scout translation related data.
 */
public final class TranslationStores {

  private TranslationStores() {
  }

  private static final Set<ITranslationStoreSupplier> SUPPLIERS = new HashSet<>();
  private static final Map<String /*npm dependency name*/, Set<String> /* UiTextContributor FQNs */> UI_TEXT_CONTRIBUTORS = new HashMap<>();
  static {
    ScoutApi.allKnown()
        .map(TranslationStores::getPredefinedTextContributorMappings)
        .map(Map::entrySet)
        .flatMap(Collection::stream)
        .distinct()
        .forEach(mapping -> registerUiTextContributor(mapping.getKey(), mapping.getValue().fqn()));
  }

  private static Map<String /* node module name */, IClassNameSupplier /* text contributor */> getPredefinedTextContributorMappings(IScoutApi api) {
    Map<String, IClassNameSupplier> mappings = new HashMap<>(2);
    mappings.put("@eclipse-scout/core", api.UiTextContributor());
    api.optApi(IScoutChartApi.class).ifPresent(chartApi -> mappings.put("@eclipse-scout/chart", chartApi.ChartUiTextContributor()));
    return mappings;
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
    return !UI_TEXT_CONTRIBUTORS.computeIfAbsent(ownerNodeModuleName, k -> new HashSet<>()).add(Ensure.notBlank(contributorFqn));
  }

  /**
   * Removes all UiTextContributor mappings for the given Node module.
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
  public static synchronized Map<String, Set<String>> uiTextContributorMappings() {
    Map<String, Set<String>> copy = new HashMap<>(UI_TEXT_CONTRIBUTORS.size());
    for (Entry<String, Set<String>> entry : UI_TEXT_CONTRIBUTORS.entrySet()) {
      copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }
    return copy;
  }

  /**
   * Registers a new {@link ITranslationStoreSupplier}
   * 
   * @param supplier
   *          The new supplier. Must not be {@code null}.
   * @return {@code true} if this element has not been registered yet. {@code false} if it was already registered and
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
   * Creates a {@link TranslationStoreStack} for a Java/JavaScript module.<br>
   * The {@link TranslationStoreStack} contains all {@link ITranslationStore}s of the module in the correct order
   * according to the @Order annotation of the corresponding Scout TextProviderService. It also handles translation
   * overriding and modifications to the {@link ITranslationStore}s.
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
  public static Optional<TranslationStoreStack> createStack(Path modulePath, IEnvironment env, IProgress progress) {
    Ensure.notNull(modulePath);
    Ensure.notNull(env);
    Ensure.notNull(progress);
    return Optional.of(new TranslationStoreStack(getAllStoresForModule(modulePath, env, progress)))
        .filter(stack -> stack.allStores().count() > 0);
  }

  /**
   * Gets all accessible {@link ITranslationStore}s for the module at the path given. The stores are in no particular
   * order. <br/>
   * For ordered stores use {@link #createStack(Path, IEnvironment, IProgress)}.
   * 
   * @param modulePath
   *          The modulePath for which the {@link ITranslationStore}s should be returned. Points to the root folder of
   *          the Java/JavaScript module. Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return all accessible {@link ITranslationStore}s for the module at the path given in no particular order.
   */
  public static Stream<ITranslationStore> allForModule(Path modulePath, IEnvironment env, IProgress progress) {
    Ensure.notNull(modulePath);
    Ensure.notNull(env);
    Ensure.notNull(progress);
    return getAllStoresForModule(modulePath, env, progress);
  }

  /**
   * Gets all accessible {@link ITranslationStore}s for the Java module at the path given.
   * 
   * @param modulePath
   *          The modulePath for which the {@link ITranslationStore}s should be returned. Points to the root folder of
   *          the Java module (the folder holding e.g. the source folder). Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return all {@link ITranslationStore}s accessible to the Java code of the module given in no particular order.
   */
  public static Stream<ITranslationStore> allForJavaModule(Path modulePath, IEnvironment env, IProgress progress) {
    Ensure.notNull(modulePath);
    Ensure.notNull(env);
    Ensure.notNull(progress);

    int ticksBySupplier = 1000;
    List<ITranslationStoreSupplier> suppliers = storeSuppliers();
    progress.init(suppliers.size() * ticksBySupplier, "Search translation stores for {}", modulePath);

    return suppliers.stream()
        .flatMap(supplier -> supplier.all(modulePath, env, progress.newChild(ticksBySupplier)))
        .filter(TranslationStores::isContentAvailable);
  }

  /**
   * Gets all accessible {@link ITranslationStore}s for the web (npm) module at the path given.
   * 
   * @param modulePath
   *          The modulePath for which the {@link ITranslationStore}s should be returned. Points to the root folder of
   *          the web module (the folder containing the package.json file). Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return all {@link ITranslationStore}s accessible to the EcmaScript code of the module given in no particular
   *         order.
   */
  public static Stream<ITranslationStore> allForWebModule(Path modulePath, IEnvironment env, IProgress progress) {
    Ensure.notNull(modulePath);
    Ensure.notNull(env);
    Ensure.notNull(progress);

    return WebModuleTranslationStores.allForModule(modulePath, env, progress)
        .filter(TranslationStores::isContentAvailable);
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
    progress.init(ticksBySupplier * suppliers.size(), "Creating translation store for service '{}'.", textService);
    return suppliers.stream()
        .map(supplier -> supplier.single(textService, progress.newChild(ticksBySupplier)))
        .flatMap(Optional::stream)
        .findFirst();
  }

  static Stream<ITranslationStore> getAllStoresForModule(Path modulePath, IEnvironment env, IProgress progress) {
    progress.init(20000, "Resolve all translation stores for module '{}'.", modulePath);
    Supplier<Stream<ITranslationStore>> storesVisibleInJavaDependencies = () -> allForJavaModule(modulePath, env, progress.newChild(10000));
    Supplier<Stream<ITranslationStore>> storesVisibleInWebDependencies = () -> allForWebModule(modulePath, env, progress.newChild(10000));
    return Stream.of(storesVisibleInJavaDependencies, storesVisibleInWebDependencies)
        .flatMap(Supplier::get)
        .collect(toMap(s -> s.service().type().name(), identity(), TranslationStores::keepLargerStore))
        .values()
        .stream();
  }

  /**
   * In case the same store is part of the java module list and the web module list: keep the one that contains more
   * elements (unfiltered)
   */
  private static ITranslationStore keepLargerStore(ITranslationStore a, ITranslationStore b) {
    if (a.size() >= b.size()) {
      return a;
    }
    return b;
  }

  static boolean isContentAvailable(ITranslationStore s) {
    if (s == null) {
      return false;
    }

    if (s.languages().findAny().isEmpty()) {
      SdkLog.warning("{} contains no languages! Please check the configuration.", s);
      return false;
    }

    if (s.languages().noneMatch(l -> l == Language.LANGUAGE_DEFAULT)) {
      SdkLog.warning("{} does not contain a default language!", s);
      return false;
    }

    return true;
  }
}
