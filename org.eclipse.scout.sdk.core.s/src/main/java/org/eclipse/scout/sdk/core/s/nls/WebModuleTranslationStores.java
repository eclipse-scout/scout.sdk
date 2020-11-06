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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.uiTextContributorMappings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

public final class WebModuleTranslationStores {

  private WebModuleTranslationStores() {
  }

  static Stream<ITranslationStore> allForModule(Path modulePath, IEnvironment env, IProgress progress) {
    progress.init(1, "Resolve translation stores visible in npm dependencies of module '{}'.", modulePath);
    IProgress childProgress = progress.newChild(0);
    Supplier<Stream<ITranslationStore>> packageJsonResolver = () -> resolveStoresReferencedInPackageJson(modulePath, env, childProgress);
    Supplier<Stream<ITranslationStore>> scoutJsBackendModuleResolver = () -> resolveScoutJsBackendModuleStores(modulePath, env, childProgress);
    return Stream.of(packageJsonResolver, scoutJsBackendModuleResolver).flatMap(Supplier::get);
  }

  static Stream<ITranslationStore> resolveStoresReferencedInPackageJson(Path modulePath, IEnvironment env, IProgress progress) {
    return loadPackageJson(modulePath)
        .map(WebModuleTranslationStores::getTextContributorsReferencedInPackageJson)
        .orElseGet(Stream::empty)
        .flatMap(name -> resolveStoresProvidingTranslationsOfContributor(name, env, progress));
  }

  static Stream<ITranslationStore> resolveScoutJsBackendModuleStores(Path modulePath, IEnvironment env, IProgress progress) {
    return Stream.of(new SimpleImmutableEntry<>(".ui", ".app"))
        .map(nameMapping -> findIncludedModuleBySuffixConvention(modulePath, nameMapping.getKey(), nameMapping.getValue()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(targetModulePath -> TranslationStores.allForModule(targetModulePath, env, progress));
  }

  static Optional<Path> findIncludedModuleBySuffixConvention(Path sourceModulePath, String sourceModuleSuffix, String targetModuleSuffix) {
    String sourceModuleFolderName = sourceModulePath.getFileName().toString().toLowerCase(Locale.ENGLISH);
    if (!sourceModuleFolderName.endsWith(sourceModuleSuffix)) {
      return Optional.empty();
    }
    String targetModuleName = sourceModuleFolderName.substring(0, sourceModuleFolderName.length() - sourceModuleSuffix.length()) + targetModuleSuffix;
    Path targetModulePath = sourceModulePath.getParent().resolve(targetModuleName);
    if (Files.isReadable(targetModulePath) && Files.isDirectory(targetModulePath)) {
      return Optional.of(targetModulePath);
    }
    return Optional.empty();
  }

  static Stream<ITranslationStore> resolveStoresProvidingTranslationsOfContributor(String contributorFqn, IEnvironment env, IProgress progress) {
    Map<Path, List<UiTextContributor>> textContributorsByModule = env.findType(contributorFqn)
        .map(type -> createUiTextContributor(type, progress))
        .collect(groupingBy(c -> moduleOfContributor(c, env)));
    return textContributorsByModule
        .entrySet().stream()
        .flatMap(entry -> resolveStoresProvidingTranslationsOfContributor(entry.getKey(), entry.getValue(), env, progress));
  }

  static Path moduleOfContributor(UiTextContributor contributor, IEnvironment env) {
    return env.rootOfJavaEnvironment(contributor.type().javaEnvironment());
  }

  static Stream<ITranslationStore> resolveStoresProvidingTranslationsOfContributor(Path modulePath, Collection<UiTextContributor> contributorsInModule, IEnvironment env, IProgress progress) {
    // collect the visible keys once for all stores (faster filter)
    Set<String> keysOfContributor = contributorsInModule.stream()
        .flatMap(UiTextContributor::keys)
        .collect(toSet());
    if (keysOfContributor.isEmpty()) {
      return Stream.empty();
    }
    return TranslationStores.allForJavaModule(modulePath, env, progress) // only calculate the visible text services once for each module.
        .<ITranslationStore> map(store -> new FilteredTranslationStore(store, keysOfContributor))
        .filter(store -> store.entries().count() > 0) // ignore stores that are not mentioned in the contributors
        .peek(store -> SdkLog.debug("Translation store '{}' found in module '{}' (referenced because of npm dependency).", store, modulePath));
  }

  static Stream<String> getTextContributorsReferencedInPackageJson(String packageJsonContent) {
    return uiTextContributorMappings().entrySet().stream()
        .filter(entry -> packageJsonContent.contains(entry.getKey()))
        .map(Entry::getValue);
  }

  static Optional<String> loadPackageJson(Path modulePath) {
    Path packageJsonFile = modulePath.resolve("package.json");
    if (!Files.isRegularFile(packageJsonFile) || !Files.isReadable(packageJsonFile)) {
      return Optional.empty();
    }
    try {
      return Optional.of(Strings.fromFileAsString(packageJsonFile, StandardCharsets.UTF_8));
    }
    catch (IOException e) {
      throw new SdkException("Cannot read package.json file to analyze dependency structure.", e);
    }
  }

  static UiTextContributor createUiTextContributor(IType contributorType, IProgress progress) {
    UiTextContributor contributor = new UiTextContributor(contributorType);
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
