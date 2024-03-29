/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls.query;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope;
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns.JsModelTextKeyPattern;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryMatch;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
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

  private static final String LITERAL_DELIMITER = "['`\"]";
  private static final Pattern TRANSLATION_LITERAL_PATTERN = Pattern.compile(LITERAL_DELIMITER + '(' + ITranslation.KEY_REGEX.pattern() + ')' + LITERAL_DELIMITER);
  @SuppressWarnings("StaticCollection")
  private static final Set<String> ACCEPTED_EXTENSIONS = DependencyScope.supportedFileExtensions().keySet();

  private final Map<Path, Set<FileQueryMatch>> m_result = new HashMap<>();
  private final String m_name;

  public TranslationKeysQuery() {
    this(null);
  }

  public TranslationKeysQuery(String name) {
    m_name = Strings.notBlank(name).orElse("Search all translation keys");
  }

  public static boolean acceptFileExtension(String extension) {
    return ACCEPTED_EXTENSIONS.contains(extension);
  }

  @Override
  public void searchIn(FileQueryInput input) {
    if (!acceptFileExtension(input.fileExtension())) {
      return;
    }

    var fileContent = input.fileContent();
    Stream.of(TRANSLATION_LITERAL_PATTERN, JsModelTextKeyPattern.REGEX)
        .flatMap(pat -> pat.matcher(fileContent).results())
        .map(r -> toMatch(input, r))
        .forEach(m -> m_result.computeIfAbsent(input.file(), k -> new HashSet<>()).add(m));
  }

  protected static FileQueryMatch toMatch(FileQueryInput input, MatchResult result) {
    return new FileQueryMatch(input.file(), input.module(), result.group(1), result.start(1), result.end(1));
  }

  /**
   * @return All {@link ITranslation} keys found.
   */
  public Stream<String> keysFound() {
    return result()
        .map(FileQueryMatch::text)
        .map(Objects::toString)
        .distinct();
  }

  /**
   * @return A {@link Map} holding all findings grouped by {@link ITranslation} key.
   */
  public Map<String, Set<FileQueryMatch>> resultByKey() {
    return result()
        .collect(groupingBy(m -> m.text().toString(), toSet()));
  }

  /**
   * @param nlsKey
   *          The {@link ITranslation} key for which the {@link FileQueryMatch matches} should be returned.
   * @return All findings for a specific {@link ITranslation} key.
   */
  public Stream<FileQueryMatch> result(String nlsKey) {
    return result()
        .filter(m -> Objects.equals(nlsKey, m.text().toString()));
  }

  @Override
  public Set<FileQueryMatch> result(Path file) {
    var set = m_result.get(file);
    if (set == null) {
      return emptySet();
    }
    return unmodifiableSet(set);
  }

  @Override
  public Stream<FileQueryMatch> result() {
    return m_result.values().stream()
        .flatMap(Collection::stream);
  }

  @Override
  public String name() {
    return m_name;
  }
}
