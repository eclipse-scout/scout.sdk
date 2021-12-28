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
package org.eclipse.scout.sdk.core.s.nls.query;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.s.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.Translations;
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope;
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns.AbstractTranslationPattern;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryMatch;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link MissingTranslationQuery}</h3>
 * <p>
 * Finds occurrences in the source which refer to {@link Translation} keys that cannot be found.
 * </p>
 *
 * @since 10.0.0
 */
public class MissingTranslationQuery implements IFileQuery {

  public static final String JS_TEXTS_FILE_NAME = "texts.js";

  private final Set<String> m_javaTextsFileName = ScoutApi.allKnown()
      .map(IScoutVariousApi::TEXTS)
      .map(ITypeNameSupplier::simpleName)
      .map(name -> name + JavaTypes.JAVA_FILE_SUFFIX)
      .collect(toSet());
  private final Map<String, List<AbstractTranslationPattern>> m_searchPatterns;
  private final Map<CompositeObject, Optional<Set<String>>> m_keysByModuleCache;
  private final Map<Path, Set<FileQueryMatch>> m_matches;
  private final BiFunction<Path, DependencyScope, List<ITranslationStore>> m_storeSupplier;

  public MissingTranslationQuery(IEnvironment env, IProgress progress) {
    this((p, s) -> Translations.storesForModule(p, env, progress, s).collect(toList()));
  }

  public MissingTranslationQuery(BiFunction<Path, DependencyScope, List<ITranslationStore>> storeSupplier) {
    m_storeSupplier = Ensure.notNull(storeSupplier);
    m_keysByModuleCache = new ConcurrentHashMap<>();
    m_matches = new ConcurrentHashMap<>();
    m_searchPatterns = TranslationPatterns.all().collect(groupingBy(AbstractTranslationPattern::fileExtension));
  }

  @Override
  public String name() {
    return "Search for text keys that are used in the code but cannot be found";
  }

  protected boolean acceptCandidate(FileQueryInput candidate) {
    if (!m_searchPatterns.containsKey(candidate.fileExtension())) {
      return false;
    }

    var fullPath = candidate.file();
    if (pathContainsSegment(fullPath, "archetype-resources")
        || pathContainsSegment(fullPath, "generated-resources")
        || fullPath.endsWith(JS_TEXTS_FILE_NAME)) {
      return false;
    }

    var fileName = fullPath.getFileName();
    if (fileName == null) {
      return false;
    }
    var fileNameAsString = fileName.toString();
    return !m_javaTextsFileName.contains(fileNameAsString);
  }

  protected static boolean pathContainsSegment(Iterable<Path> path, String name) {
    var toSearch = Paths.get(name);
    for (var segment : path) {
      if (segment.equals(toSearch)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void searchIn(FileQueryInput input) {
    if (!acceptCandidate(input)) {
      return;
    }

    var fileExtension = input.fileExtension();
    var dependencyScope = DependencyScope.forFileExtension(fileExtension)
        .orElseThrow(() -> newFail("Unsupported file extension: {}.", fileExtension));
    var patterns = m_searchPatterns.get(fileExtension);
    var content = input.fileContent();
    for (var search : patterns) {
      var matcher = search.pattern().matcher(content);
      while (matcher.find()) {
        checkMatch(matcher, search, input, dependencyScope);
      }
    }
  }

  protected void checkMatch(MatchResult match, AbstractTranslationPattern search, FileQueryInput fileQueryInput, DependencyScope scope) {
    int keyGroup;

    if (match.groupCount() > 1) {
      // pattern with optional literal delimiter: '"' for java, ''' for js
      // check if the literal delimiter is present. if not present: not possible to detect the real key -> add to error list.
      keyGroup = 2;
      var noLiteral = Strings.isEmpty(match.group(1)) || Strings.isEmpty(match.group(3));
      if (noLiteral) {
        // is no string literal. might be e variable or concatenation.
        if (!tryToResolveConstant(match.group(keyGroup), fileQueryInput, scope)) {
          // cannot be resolved as constant. register as match for manual review
          registerMatchIfNotIgnored(match, Level.INFO.intValue(), search, fileQueryInput);
        }
        return;
      }
    }
    else {
      // pattern without literal delimiter
      keyGroup = 1;
    }
    registerMatchIfKeyIsMissing(match, keyGroup, Level.WARNING.intValue(), search, scope, fileQueryInput);
  }

  protected boolean tryToResolveConstant(String constantName, FileQueryInput fileQueryInput, DependencyScope scope) {
    var findAssignment = new AssignmentPattern(constantName);
    var content = fileQueryInput.fileContent();
    var constantMatcher = findAssignment.pattern().matcher(content);
    var constantFound = false;
    while (constantMatcher.find()) {
      registerMatchIfKeyIsMissing(constantMatcher, 1, Level.WARNING.intValue(), findAssignment, scope, fileQueryInput);
      constantFound = true;
    }
    return constantFound;
  }

  protected void registerMatchIfKeyIsMissing(MatchResult match, int keyGroup, int severity, AbstractTranslationPattern pattern, DependencyScope scope, FileQueryInput queryInput) {
    var key = match.group(keyGroup);
    if (keyExistsForModule(queryInput.module(), key, scope)) {
      return;
    }
    registerMatchIfNotIgnored(match, severity, pattern, queryInput);
  }

  protected void registerMatchIfNotIgnored(MatchResult match, int severity, AbstractTranslationPattern pattern, FileQueryInput queryInput) {
    pattern.keyRangeIfAccept(match, queryInput)
        .map(range -> FileQueryMatch.fromFileRange(range, severity))
        .ifPresent(m -> m_matches.computeIfAbsent(queryInput.file(), file -> newKeySet()).add(m));
  }

  protected boolean keyExistsForModule(Path modulePath, String key, DependencyScope scope) {
    return accessibleKeysForModule(modulePath, scope)
        .map(keys -> keys.contains(key))
        .orElse(true); // no manager could be created: the module is no scout module (no TextProviderService visible, but at least the Scout service should be available). In that case no markers should be created.
  }

  protected Optional<Set<String>> accessibleKeysForModule(Path modulePath, DependencyScope scope) {
    var key = new CompositeObject(modulePath, scope);
    return m_keysByModuleCache.computeIfAbsent(key, mp -> computeAccessibleKeysForModule(modulePath, scope));
  }

  protected Optional<Set<String>> computeAccessibleKeysForModule(Path modulePath, DependencyScope scope) {
    var stores = m_storeSupplier.apply(modulePath, scope);
    if (stores.isEmpty()) {
      return Optional.empty(); // no stores found: it is no scout module. This module will be ignored in the search (no missing translations).
    }
    return Optional.of(stores.stream()
        .flatMap(ITranslationStore::keys)
        .collect(toSet()));
  }

  @Override
  public Stream<FileQueryMatch> result() {
    return m_matches.values().stream().flatMap(Set::stream);
  }

  @Override
  public Set<FileQueryMatch> result(Path file) {
    return getFromResultMap(m_matches, file);
  }

  protected static <K, V> Set<V> getFromResultMap(Map<K, Set<V>> map, K key) {
    var ranges = map.get(key);
    if (ranges == null || ranges.isEmpty()) {
      return emptySet();
    }
    return unmodifiableSet(ranges);
  }

  private static final class AssignmentPattern extends AbstractTranslationPattern {

    private final Pattern m_pattern;

    private AssignmentPattern(String constantName) {
      m_pattern = Pattern.compile("\\s+" + constantName + "\\s*=\\s*[\"`'](" + NLS_KEY_PAT + ")[\"`'];");
    }

    @Override
    public Pattern pattern() {
      return m_pattern;
    }

    @Override
    public String fileExtension() {
      return null;
    }

    @Override
    public Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput) {
      return keyRangeIfInCode(match, fileQueryInput, 1);
    }
  }
}
