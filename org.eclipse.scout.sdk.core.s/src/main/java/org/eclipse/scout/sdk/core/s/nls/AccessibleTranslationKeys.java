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

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.createFullStack;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.uiTextContributorMappings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Represents {@link ITranslation} keys that are accessible in a JavaScript and/or Java module.<br>
 * Obtain instances using {@link TranslationStores#keysAccessibleForModule(Path, IEnvironment, IProgress)}.
 */
public class AccessibleTranslationKeys {

  private final Path m_modulePath;
  private final Set<String> m_visibleInJava;
  private final Set<String> m_visibleInJs;

  protected AccessibleTranslationKeys(Path modulePath, Set<String> visibleInJava, Set<String> visibleInJs) {
    m_modulePath = modulePath;
    m_visibleInJava = visibleInJava;
    m_visibleInJs = visibleInJs;
  }

  protected static Optional<AccessibleTranslationKeys> create(Path modulePath, IEnvironment env, IProgress progress) {
    progress.init("Computing available translation keys", 200);
    Optional<Stream<String>> javaModuleKeys = loadKeysForJavaModule(modulePath, env, progress.newChild(100));
    Optional<Stream<String>> jsModuleKeys = loadKeysForJsModule(modulePath, env, progress.newChild(100));
    if (!javaModuleKeys.isPresent() && !jsModuleKeys.isPresent()) {
      // no dependencies to Scout found, neither using Java nor using JS dependencies: no scout module
      return Optional.empty();
    }

    Set<String> visibleInJava = javaModuleKeys
        .map(keys -> keys.collect(toSet()))
        .orElseGet(Collections::emptySet);
    Set<String> visibleInJs = jsModuleKeys
        .map(keys -> keys.collect(toSet()))
        .orElseGet(Collections::emptySet);
    return Optional.of(new AccessibleTranslationKeys(modulePath, visibleInJava, visibleInJs));
  }

  static Optional<Stream<String>> loadKeysForJavaModule(Path javaModulePath, IEnvironment env, IProgress progress) {
    return createFullStack(javaModulePath, env, progress)
        .map(stack -> stack.allEntries().map(ITranslation::key));
  }

  static Optional<Stream<String>> loadKeysForJsModule(Path scoutJsModulePath, IEnvironment env, IProgress progress) {
    return loadPackageJson(scoutJsModulePath)
        .flatMap(AccessibleTranslationKeys::findVisibleUiTextContributorNames)
        .map(names -> resolveUiTextContributors(names, env, progress));
  }

  static Stream<String> resolveUiTextContributors(Collection<String> uiTextContributorFqn, IEnvironment env, IProgress progress) {
    progress.init("Resolving available UI Text Contributors", uiTextContributorFqn.size());
    return uiTextContributorFqn.stream()
        .flatMap(env::findType)
        .map(contributorType -> createUiTextContributor(contributorType, progress.newChild(1)))
        .flatMap(UiTextContributor::keys);
  }

  static Optional<Set<String>> findVisibleUiTextContributorNames(String packageJsonContent) {
    Set<String> availableTextContributors = uiTextContributorMappings().entrySet().stream()
        .filter(entry -> packageJsonContent.contains(entry.getKey()))
        .map(Entry::getValue)
        .collect(toSet());
    if (availableTextContributors.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(availableTextContributors);
  }

  static Optional<String> loadPackageJson(Path scoutRtUiHtmlModulePath) {
    Path packageJsonFile = scoutRtUiHtmlModulePath.resolve("package.json");
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
      contributor.load(progress);
    }
    catch (SdkException e) {
      SdkLog.warning("Cannot calculate available text keys for ui contributor.", e);
    }
    return contributor;
  }

  /**
   * @return The {@link Path} of the module for which this result set has been calculated.
   */
  public Path modulePath() {
    return m_modulePath;
  }

  /**
   * @return A {@link Set} holding the keys visible for Java code.
   */
  public Set<String> forJava() {
    return unmodifiableSet(m_visibleInJava);
  }

  /**
   * @return A {@link Set} holding the keys visible for JavaScript code.
   */
  public Set<String> forJs() {
    return unmodifiableSet(m_visibleInJs);
  }

  /**
   * @return A {@link Set} holding all keys found.
   */
  public Set<String> all() {
    Set<String> allKeys = new HashSet<>(m_visibleInJava.size() + m_visibleInJs.size());
    allKeys.addAll(m_visibleInJava);
    allKeys.addAll(m_visibleInJs);
    return allKeys;
  }

  /**
   * Gets the key {@link Set} matching the file type of the file specified.
   * 
   * @param file
   *          The file for which the matching key {@link Set} should be returned.
   * @return The {@link Set} for the file given.
   */
  public Set<String> forFile(Path file) {
    String extension = CoreUtils.extensionOf(Ensure.notNull(file));
    switch (extension) {
      case "json":
      case "js":
        return forJs();
      case "html": // scout:message tags in html files are processed on the backend
      case "java":
        return forJava();
      default:
        return emptySet();
    }
  }
}
