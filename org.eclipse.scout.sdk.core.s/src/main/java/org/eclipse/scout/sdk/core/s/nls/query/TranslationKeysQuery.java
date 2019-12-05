/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.query;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

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
  private final Map<Path, Map<String /* key */, Set<FileRange>>> m_result;
  private final Set<String> m_acceptedFileExtensions;

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
    m_name = Ensure.notBlank(queryName);

    List<String> searchPats = new ArrayList<>(Ensure.notNull(searchKeys).size() * 2);
    for (String key : searchKeys) {
      searchPats.add('"' + key + '"');
      searchPats.add('\'' + key + '\''); // e.g. for search in .js files
    }
    m_searchKeys = unmodifiableList(searchPats);
    m_acceptedFileExtensions = unmodifiableSet(new HashSet<>(Arrays.asList(JavaTypes.JAVA_FILE_EXTENSION, "js", "html")));
    m_result = new ConcurrentHashMap<>();
  }

  protected static List<String> getEditableKeys(TranslationStoreStack project) {
    return project.allEntries()
        .filter(e -> e.store().isEditable())
        .map(ITranslationEntry::key)
        .collect(toList());
  }

  protected boolean acceptCandidate(FileQueryInput candidate) {
    String actualExtension = candidate.fileExtension();
    return Strings.hasText(actualExtension) && m_acceptedFileExtensions.contains(actualExtension);
  }

  @Override
  public void searchIn(FileQueryInput candidate, IEnvironment env, IProgress progress) {
    if (!acceptCandidate(candidate)) {
      return;
    }

    progress.init(name(), m_searchKeys.size());
    String fileContent = new String(candidate.fileContent());
    for (String search : m_searchKeys) {
      int pos = 0;
      int index;
      while ((index = fileContent.indexOf(search, pos)) >= 0) {
        FileRange match = new FileRange(candidate.file(), index, index + search.length());
        String key = search.substring(1, search.length() - 1); // remove starting and ending quotes
        acceptNlsKeyMatch(key, match);
        pos = index + search.length();
      }
      progress.worked(1);
    }
  }

  protected void acceptNlsKeyMatch(String nlsKey, FileRange match) {
    m_result
        .computeIfAbsent(match.file(), file -> new ConcurrentHashMap<>())
        .computeIfAbsent(nlsKey, k -> ConcurrentHashMap.newKeySet())
        .add(match);
  }

  @Override
  public Set<FileRange> result(Path file) {
    Map<String, Set<FileRange>> result = m_result.get(file);
    if (result == null) {
      return emptySet();
    }
    return result
        .values().stream()
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  @Override
  public Stream<FileRange> result() {
    return m_result
        .values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .flatMap(Collection::stream);
  }

  /**
   * @return A {@link Map} holding all findings grouped by {@link Translation} key.
   */
  public Map<String, Set<FileRange>> resultByKey() {
    Map<String, Set<FileRange>> resultByKey = new HashMap<>();
    for (Map<String, Set<FileRange>> l : m_result.values()) {
      for (Entry<String, Set<FileRange>> e : l.entrySet()) {
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
  public Stream<FileRange> result(String nlsKey) {
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
