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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.uiTextContributorMappings;
import static org.eclipse.scout.sdk.core.util.StreamUtils.firstBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.TranslationStores.DependencyScope;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

public final class WebModuleTranslationStores {

  private WebModuleTranslationStores() {
  }

  static Stream<ITranslationStore> allForNodeModule(Path modulePath, IEnvironment env, IProgress progress) {
    progress.init(20, "Resolve translation stores visible in npm and UiTextContributor dependencies of module '{}'.", modulePath);
    return Stream.of(
        resolveStoresReferencedInUiTextContributors(modulePath, env, progress.newChild(10)), // translations supplied by UiTextContributors (Scout classic case)
        resolveStoresFromScoutJsBackend(modulePath, env, progress.newChild(10))) // translation stores from Java classpath of a corresponding backend module (ScoutJS case)
        .flatMap(identity());
  }

  static Stream<ITranslationStore> resolveStoresReferencedInUiTextContributors(Path modulePath, IEnvironment env, IProgress progress) {
    var textContributorTypes = Stream.of(
        resolveTextContributorsReferencedInPom(modulePath, env, progress),
        resolveTextContributorsReferencedInPackageJson(modulePath, env));
    var textContributorsByModule = textContributorTypes
        .flatMap(identity())
        .filter(firstBy(IType::name))
        .map(type -> createUiTextContributor(type, progress))
        .collect(groupingBy(c -> moduleOfContributor(c, env)));
    return textContributorsByModule
        .entrySet().stream()
        .flatMap(entry -> resolveStoresProvidingTranslationsOfContributor(entry.getKey(), entry.getValue(), env, progress));
  }

  static Stream<IType> resolveTextContributorsReferencedInPom(Path modulePath, IEnvironment env, IProgress progress) {
    return TranslationStores.storeSuppliers().stream()
        .flatMap(supplier -> supplier.visibleTextContributorsForJavaModule(modulePath, env, progress));
  }

  static Stream<IType> resolveTextContributorsReferencedInPackageJson(Path modulePath, IEnvironment env) {
    return loadPackageJson(modulePath)
        .map(WebModuleTranslationStores::getTextContributorsReferencedInPackageJson)
        .orElseGet(Stream::empty)
        .flatMap(env::findType);
  }

  static Stream<ITranslationStore> resolveStoresFromScoutJsBackend(Path modulePath, IEnvironment env, IProgress progress) {
    var includedModulesByNamingConventions = Stream.of(
        new SimpleImmutableEntry<>(".ui", ".app"),
        new SimpleImmutableEntry<>(".ui.html", ".app"),
        new SimpleImmutableEntry<>(".ui.html", ".ui.html.app"))
        .map(nameMapping -> findIncludedModuleByNamingConvention(modulePath, nameMapping.getKey(), nameMapping.getValue()))
        .filter(Objects::nonNull);
    return Stream.concat(includedModulesByNamingConventions, Stream.of(modulePath) /* always include own module (blocks) */)
        .flatMap(targetModulePath -> TranslationStores.forModule(targetModulePath, env, progress, DependencyScope.JAVA));
  }

  static Path findIncludedModuleByNamingConvention(Path sourceModulePath, String sourceModuleSuffix, String targetModuleSuffix) {
    var fileName = sourceModulePath.getFileName();
    if (fileName == null) {
      return null;
    }
    var sourceModuleFolderName = fileName.toString().toLowerCase(Locale.US);
    if (!sourceModuleFolderName.endsWith(sourceModuleSuffix)) {
      return null;
    }
    var targetModuleName = sourceModuleFolderName.substring(0, sourceModuleFolderName.length() - sourceModuleSuffix.length()) + targetModuleSuffix;
    var parent = sourceModulePath.getParent();
    if (parent == null) {
      return null;
    }
    var targetModulePath = parent.resolve(targetModuleName);
    if (Files.isReadable(targetModulePath) && Files.isDirectory(targetModulePath)) {
      return targetModulePath;
    }
    return null;
  }

  static Path moduleOfContributor(UiTextContributor contributor, IEnvironment env) {
    return env.rootOfJavaEnvironment(contributor.type().javaEnvironment());
  }

  static Stream<ITranslationStore> resolveStoresProvidingTranslationsOfContributor(Path modulePath, Collection<UiTextContributor> contributorsInModule, IEnvironment env, IProgress progress) {
    // collect the visible keys once for all stores (faster filter)
    var keysOfContributor = contributorsInModule.stream()
        .flatMap(UiTextContributor::keys)
        .collect(toSet());
    if (keysOfContributor.isEmpty()) {
      return Stream.empty();
    }
    return TranslationStores.forModule(modulePath, env, progress, DependencyScope.JAVA) // only calculate the visible text services once for each module.
        .<ITranslationStore> map(store -> new FilteredTranslationStore(store, keysOfContributor))
        .filter(store -> store.entries().findAny().isPresent()) // ignore stores that are not mentioned in the contributors
        .peek(store -> SdkLog.debug("Translation store '{}' found in module '{}' (referenced from accessible UiTextContributor).", store, modulePath));
  }

  static Stream<String> getTextContributorsReferencedInPackageJson(CharSequence packageJsonContent) {
    return uiTextContributorMappings().entrySet().stream()
        .filter(entry -> Strings.indexOf(entry.getKey(), packageJsonContent) >= 0)
        .flatMap(entry -> entry.getValue().stream())
        .distinct();
  }

  static Optional<CharSequence> loadPackageJson(Path modulePath) {
    var packageJsonFile = modulePath.resolve("package.json");
    if (!Files.isRegularFile(packageJsonFile) || !Files.isReadable(packageJsonFile)) {
      return Optional.empty();
    }
    try {
      return Optional.of(Strings.fromFile(packageJsonFile, StandardCharsets.UTF_8));
    }
    catch (IOException e) {
      throw new SdkException("Cannot read package.json file to analyze dependency structure.", e);
    }
  }

  static UiTextContributor createUiTextContributor(IType contributorType, IProgress progress) {
    var contributor = new UiTextContributor(contributorType);
    try {
      SdkLog.debug("loading ui text contributor '{}'.", contributorType.name());
      contributor.load(progress);
    }
    catch (SdkException e) {
      SdkLog.warning("Cannot calculate available text keys for ui text contributor '{}'.", contributorType.name(), e);
    }
    return contributor;
  }
}
