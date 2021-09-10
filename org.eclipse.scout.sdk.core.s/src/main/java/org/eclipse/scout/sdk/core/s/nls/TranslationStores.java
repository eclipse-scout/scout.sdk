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

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IWebConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutChartApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Main access for Scout translation related data.
 */
public final class TranslationStores {

  private TranslationStores() {
  }

  private static final DependencyScope[] ALL_SCOPES = Arrays.stream(DependencyScope.values())
      .filter(scope -> scope != DependencyScope.ALL)
      .toArray(DependencyScope[]::new);

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
    mappings.put(IWebConstants.SCOUT_JS_CORE_MODULE_NAME, api.UiTextContributor());
    api.api(IScoutChartApi.class).ifPresent(chartApi -> mappings.put(IWebConstants.SCOUT_JS_CHART_MODULE_NAME, chartApi.ChartUiTextContributor()));
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
    for (var entry : UI_TEXT_CONTRIBUTORS.entrySet()) {
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
   *          root folder of the Java/JavaScript module (the folder that contains the pom.xml/package.json). Must not be
   *          {@code null}.
   * @param scope
   *          The {@link DependencyScope} to consider when resolving the visible {@link ITranslationStore} instances.
   *          The resulting {@link TranslationStoreStack} contains the stores as found according to the scope given. If
   *          {@code null}, all scopes are searched.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return A new {@link TranslationStoreStack} for the specified {@link Path}.
   * @see TranslationStoreStack
   */
  public static Optional<TranslationStoreStack> createStack(Path modulePath, IEnvironment env, IProgress progress, DependencyScope scope) {
    return createStack(forModule(modulePath, env, progress, scope));
  }

  /**
   * Creates a {@link TranslationStoreStack} for a Java/JavaScript module.<br>
   * The {@link TranslationStoreStack} contains all {@link ITranslationStore}s of the module in the correct order
   * according to the @Order annotation of the corresponding Scout TextProviderService. It also handles translation
   * overriding and modifications to the {@link ITranslationStore}s.
   *
   * @param modulePath
   *          The modulePath for which the stack (containing all accessible stores) should be returned. Points to the
   *          root folder of the Java/JavaScript module (the folder that contains the pom.xml/package.json). Must not be
   *          {@code null}.
   * @param scopes
   *          The {@link DependencyScope scopes} to consider when resolving the visible {@link ITranslationStore}
   *          instances. The resulting {@link TranslationStoreStack} contains the stores as found according to the
   *          scopes given. If {@code null}, all scopes are searched.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return A new {@link TranslationStoreStack} for the specified {@link Path}.
   * @see TranslationStoreStack
   */
  public static Optional<TranslationStoreStack> createStack(Path modulePath, IEnvironment env, IProgress progress, DependencyScope... scopes) {
    return createStack(forModule(modulePath, env, progress, scopes));
  }

  /**
   * Creates a {@link TranslationStoreStack} for the {@link ITranslationStore stores} given.
   * 
   * @param stores
   *          The {@link ITranslationStore} instances that should be added to the stack. Must not be {@code null}.
   * @return A {@link TranslationStoreStack} if the {@link Stream} given contains any stores.
   */
  public static Optional<TranslationStoreStack> createStack(Stream<ITranslationStore> stores) {
    return Optional.of(new TranslationStoreStack(stores))
        .filter(stack -> stack.allStores().findAny().isPresent());
  }

  /**
   * An implicit override of a translation happens if stores share the same bean {@code @Order} value and have common
   * translation keys. This results in an implicit override of a translation based on the fully qualified name of the
   * service. As this is not refactoring safe each service that overwrites other keys should have an explicit unique
   * order to ensure a stable override.
   *
   * @return A {@link Stream} returning {@link Set sets} of {@link ITranslationStore TranslationStores} that share the
   *         same {@code @Order} annotation value and have common translation keys.
   */
  public static Stream<Set<ITranslationStore>> havingImplicitOverrides(Collection<ITranslationStore> allStores) {
    return allStores.stream()
        .distinct()
        .collect(groupingBy(s -> s.service().order(), toList()))
        .values().stream()
        .filter(group -> group.size() > 1)
        .map(TranslationStores::storesWithCommonKeys)
        .filter(group -> !group.isEmpty());
  }

  static Set<ITranslationStore> storesWithCommonKeys(List<ITranslationStore> group) {
    var result = new HashSet<ITranslationStore>(group.size());
    for (var i = 0; i < group.size(); i++) {
      var storeToCheck = group.get(i);
      for (var j = i + 1; j < group.size(); j++) {
        var storeToCompare = group.get(j);
        if (storeToCheck.keys().anyMatch(storeToCompare::containsKey)) {
          result.add(storeToCheck);
          result.add(storeToCompare);
        }
      }
    }
    return result;
  }

  /**
   * Computes all {@link ITranslationStore} instances accessible for the given module.
   * 
   * @param modulePath
   *          The modulePath for which the stores should be returned. Points to the root folder of the Java/JavaScript
   *          module (the folder that contains the pom.xml/package.json). Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @param scopes
   *          The {@link DependencyScope scopes} to consider when resolving the visible {@link ITranslationStore}
   *          instances. If {@code null}, all scopes are searched.
   * @return All {@link ITranslationStore} instances accessible in the module given according to the requested
   *         {@link DependencyScope scopes}.
   */
  public static Stream<ITranslationStore> forModule(Path modulePath, IEnvironment env, IProgress progress, DependencyScope... scopes) {
    return combineSameStores(computeStoresForModule(modulePath, env, progress, scopes));
  }

  /**
   * Computes all {@link ITranslationStore} instances accessible for the given module.
   *
   * @param modulePath
   *          The modulePath for which the stores should be returned. Points to the root folder of the Java/JavaScript
   *          module (the folder that contains the pom.xml/package.json). Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @param scope
   *          The {@link DependencyScope} to consider when resolving the visible {@link ITranslationStore} instances. If
   *          {@code null}, all scopes are searched.
   * @return All {@link ITranslationStore} instances accessible in the module given according to the requested
   *         {@link DependencyScope}.
   */
  public static Stream<ITranslationStore> forModule(Path modulePath, IEnvironment env, IProgress progress, DependencyScope scope) {
    return combineSameStores(computeStoresForModule(modulePath, env, progress, scope));
  }

  static Stream<ITranslationStore> computeStoresForModule(Path modulePath, IEnvironment env, IProgress progress, DependencyScope... scopes) {
    if (scopes == null || scopes.length < 1) {
      return computeStoresForModule(modulePath, env, progress, DependencyScope.ALL);
    }

    var ticksByScope = 10000;
    progress.init(ticksByScope * scopes.length, "Resolve translation stores for module '{}'.", modulePath);
    return Arrays.stream(scopes)
        .flatMap(scope -> computeStoresForModule(modulePath, env, progress.newChild(ticksByScope), scope));
  }

  static Stream<ITranslationStore> computeStoresForModule(Path modulePath, IEnvironment env, IProgress progress, DependencyScope scope) {
    if (scope == null || scope == DependencyScope.ALL) {
      return computeStoresForModule(modulePath, env, progress, ALL_SCOPES);
    }

    switch (scope) {
      case JAVA:
        return forJavaModule(modulePath, env, progress);
      case NODE:
        return forNodeModule(modulePath, env, progress);
      default:
        throw newFail("Scope not implemented: {}", scope);
    }
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
  static Stream<ITranslationStore> forJavaModule(Path modulePath, IEnvironment env, IProgress progress) {
    var ticksBySupplier = 1000;
    var suppliers = storeSuppliers();
    progress.init(suppliers.size() * ticksBySupplier, "Search translation stores for Java module at '{}'.", modulePath);
    return suppliers.stream()
        .flatMap(supplier -> supplier.visibleStoresForJavaModule(modulePath, env, progress.newChild(ticksBySupplier)))
        .filter(TranslationStores::isContentAvailable);
  }

  /**
   * Gets all accessible {@link ITranslationStore}s for the Node module at the path given.
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
  static Stream<ITranslationStore> forNodeModule(Path modulePath, IEnvironment env, IProgress progress) {
    SdkLog.debug("Search translation stores for Node module at '{}'.", modulePath);
    return WebModuleTranslationStores.allForNodeModule(modulePath, env, progress)
        .filter(TranslationStores::isContentAvailable);
  }

  static Stream<ITranslationStore> combineSameStores(Stream<ITranslationStore> stores) {
    return stores.collect(toMap(s -> s.service().type().name(), identity(), TranslationStores::mergeStores))
        .values()
        .stream();
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

    var ticksBySupplier = 1000;
    var suppliers = storeSuppliers();
    progress.init(ticksBySupplier * suppliers.size(), "Creating translation store for service '{}'.", textService);
    return suppliers.stream()
        .map(supplier -> supplier.createStoreForService(textService, progress.newChild(ticksBySupplier)))
        .flatMap(Optional::stream)
        .findFirst();
  }

  static ITranslationStore mergeStores(ITranslationStore a, ITranslationStore b) {
    var aIsFiltered = a instanceof FilteredTranslationStore;
    var bIsFiltered = b instanceof FilteredTranslationStore;
    if (!aIsFiltered && !bIsFiltered) {
      // both are unfiltered. Usually they have the same content so it doesn't matter which one to use
      return a.size() >= b.size() ? a : b;
    }
    if (aIsFiltered && !bIsFiltered) {
      // b is unfiltered. It contains all elements: use it
      return b;
    }
    if (!aIsFiltered) {
      // a is unfiltered. It contains all elements: use it
      return a;
    }
    // here a and b are filtered. combine them to a new store holding the union of both filters
    var filteredA = (FilteredTranslationStore) a;
    var filteredB = (FilteredTranslationStore) b;
    var filterA = filteredA.keysFilter();
    var filterB = filteredB.keysFilter();
    Set<String> newFilter = new HashSet<>(filterA.size() + filterB.size());
    newFilter.addAll(filterA);
    newFilter.addAll(filterB);
    return new FilteredTranslationStore(filteredA.nestedStore(), newFilter);
  }

  static boolean isContentAvailable(ITranslationStore s) {
    if (s == null) {
      return false;
    }

    if (s.languages().findAny().isEmpty()) {
      SdkLog.warning("{} contains no languages! Please check the configuration.", s);
      return false;
    }
    return true;
  }

  /**
   * A translation dependency scope
   */
  public enum DependencyScope {
    /**
     * Represents the Java classpath dependency scope typically defined in a Maven pom.xml
     */
    JAVA,

    /**
     * Represents the node_modules dependency scope typically defined in a package.json.
     */
    NODE,

    /**
     * Represents all possible dependency scopes.
     */
    ALL;

    private static final Map<String, DependencyScope> FILE_TYPE_MAPPING = new HashMap<>(3);
    static {
      FILE_TYPE_MAPPING.put(JavaTypes.JAVA_FILE_EXTENSION, DependencyScope.JAVA);
      FILE_TYPE_MAPPING.put(IWebConstants.JS_FILE_EXTENSION, DependencyScope.NODE);
      FILE_TYPE_MAPPING.put(IWebConstants.HTML_FILE_EXTENSION, DependencyScope.JAVA); // message tags are evaluated on the Java backend
    }

    /**
     * Gets the {@link DependencyScope} to be used for a file type.
     * 
     * @param name
     *          A file name, file path or file extension (with or without dot). E.g. "MyClass.java" or "java" or "html"
     *          or "path/to/MyFile.js".
     * @return An {@link Optional} holding the {@link DependencyScope} if the given name is supported. An empty
     *         {@link Optional} otherwise.
     */
    public static Optional<DependencyScope> forFileExtension(CharSequence name) {
      return FILE_TYPE_MAPPING
          .entrySet().stream()
          .filter(entry -> isOrHasExtension(name, entry.getKey()))
          .findAny()
          .map(Entry::getValue);
    }

    private static boolean isOrHasExtension(CharSequence name, String extension) {
      return Strings.equals(name, extension, false)
          || Strings.endsWith(name, '.' + extension, false);
    }

    /**
     * @return An unmodifiable {@link Map} containing all file extensions in which Scout translation may exist and the
     *         corresponding {@link DependencyScope}.
     */
    public static Map<String, DependencyScope> supportedFileExtensions() {
      return unmodifiableMap(FILE_TYPE_MAPPING);
    }
  }
}
