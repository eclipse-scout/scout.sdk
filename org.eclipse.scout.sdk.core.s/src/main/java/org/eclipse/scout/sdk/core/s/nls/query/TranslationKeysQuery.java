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
package org.eclipse.scout.sdk.core.s.nls.query;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.util.Strings.hasText;
import static org.eclipse.scout.sdk.core.util.Strings.indexOf;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns.JsonTextKeyPattern;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryMatch;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link TranslationKeysQuery}</h3>
 * <p>
 * Finds occurrences of NLS keys in the source code.
 * </p>
 *
 * @since 10.0.0
 */
public class TranslationKeysQuery implements IFileQuery {

  private final String m_name;
  private final List<String> m_searchKeys;
  private final Map<Path, Map<String /* key */, Set<FileQueryMatch>>> m_result;
  private final Set<String> m_acceptedFileExtensions;
  private final Map<char[], char[]> m_suffixAndPrefix;

  /**
   * @param nlsKey
   *          The nls key to find.
   * @param queryName
   *          The description of the query. The description is used in the progress monitor.
   */
  public TranslationKeysQuery(String nlsKey, String queryName) {
    this(singletonList(Ensure.notBlank(nlsKey)), queryName);
  }

  /**
   * @param nlsStoreStack
   *          The keys of all editable translations of this stack are searched.
   * @param queryName
   *          The description of the query. The description is used in the progress monitor.
   */
  public TranslationKeysQuery(TranslationStoreStack nlsStoreStack, String queryName) {
    this(getEditableKeys(nlsStoreStack), queryName);
  }

  protected TranslationKeysQuery(Collection<String> searchKeys, String queryName) {
    Ensure.notNull(searchKeys);
    m_name = Ensure.notBlank(queryName);

    m_suffixAndPrefix = new HashMap<>();
    m_suffixAndPrefix.put(new char[]{'"'}, new char[]{'"'});
    m_suffixAndPrefix.put(new char[]{'`'}, new char[]{'`'});
    m_suffixAndPrefix.put(new char[]{'\''}, new char[]{'\''});
    m_suffixAndPrefix.put(JsonTextKeyPattern.JSON_TEXT_KEY_PREFIX.toCharArray(), JsonTextKeyPattern.JSON_TEXT_KEY_SUFFIX.toCharArray());

    m_searchKeys = new ArrayList<>(searchKeys);
    m_acceptedFileExtensions = TranslationPatterns.supportedFileExtensions();
    m_result = new ConcurrentHashMap<>();
  }

  protected static Set<String> getEditableKeys(TranslationStoreStack project) {
    return project.allEntries()
        .filter(e -> e.store().isEditable())
        .map(ITranslationEntry::key)
        .collect(toSet());
  }

  protected boolean acceptCandidate(FileQueryInput candidate) {
    var actualExtension = candidate.fileExtension();
    return hasText(actualExtension) && m_acceptedFileExtensions.contains(actualExtension);
  }

  @Override
  public void searchIn(FileQueryInput candidate, IEnvironment env, IProgress progress) {
    if (!acceptCandidate(candidate)) {
      return;
    }

    progress.init(m_suffixAndPrefix.size(), name());
    var fileContent = candidate.fileContent();
    for (var suffixAndPrefix : m_suffixAndPrefix.entrySet()) {
      for (var key : m_searchKeys) {
        var suffix = suffixAndPrefix.getKey();
        var search = buildSearchPattern(suffix, key, suffixAndPrefix.getValue());
        var pos = 0;
        int index;
        while ((index = indexOf(search, fileContent, pos)) >= 0) {
          var match = new FileRange(candidate.file(), key, index + suffix.length, index + suffix.length + key.length());
          acceptNlsKeyMatch(key, match);
          pos = index + search.length;
        }
      }
      progress.worked(1);
    }
  }

  private static char[] buildSearchPattern(char[] prefix, String key, char[] suffix) {
    var pat = new char[prefix.length + key.length() + suffix.length];
    System.arraycopy(prefix, 0, pat, 0, prefix.length);
    System.arraycopy(key.toCharArray(), 0, pat, prefix.length, key.length());
    System.arraycopy(suffix, 0, pat, prefix.length + key.length(), suffix.length);
    return pat;
  }

  protected void acceptNlsKeyMatch(String nlsKey, FileRange match) {
    m_result
        .computeIfAbsent(match.file(), file -> new ConcurrentHashMap<>())
        .computeIfAbsent(nlsKey, k -> ConcurrentHashMap.newKeySet())
        .add(FileQueryMatch.fromFileRange(match));
  }

  @Override
  public Set<FileQueryMatch> result(Path file) {
    var result = m_result.get(file);
    if (result == null) {
      return emptySet();
    }
    return result
        .values().stream()
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  @Override
  public Stream<FileQueryMatch> result() {
    return m_result
        .values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .flatMap(Collection::stream);
  }

  /**
   * @return A {@link Map} holding all findings grouped by {@link Translation} key.
   */
  public Map<String, Set<FileQueryMatch>> resultByKey() {
    Map<String, Set<FileQueryMatch>> resultByKey = new HashMap<>();
    for (var l : m_result.values()) {
      for (var e : l.entrySet()) {
        resultByKey.computeIfAbsent(e.getKey(), k -> new HashSet<>()).addAll(e.getValue());
      }
    }
    return resultByKey;
  }

  /**
   * @param nlsKey
   *          The {@link Translation} key for which the findings should be returned.
   * @return All findings for a specific {@link Translation} key.
   */
  public Stream<FileQueryMatch> result(String nlsKey) {
    return m_result.values().stream()
        .map(map -> map.get(nlsKey))
        .filter(Objects::nonNull)
        .flatMap(Collection::stream);
  }

  @Override
  public String name() {
    return m_name;
  }
}
