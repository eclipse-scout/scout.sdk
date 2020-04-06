/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.query;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.s.util.search.IFileQuery;
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
@SuppressWarnings("HardcodedFileSeparator")
public class MissingTranslationQuery implements IFileQuery {

  public static final String IGNORE_MARKER = "NO-NLS-CHECK";
  @SuppressWarnings("PublicStaticCollectionField")
  public static final Map<String, Collection<Pattern>> PATTERNS_BY_FILE_EXTENSION;
  public static final String TEXTS_FILE_NAME = JavaTypes.simpleName(IScoutRuntimeTypes.TEXTS) + JavaTypes.JAVA_FILE_SUFFIX;

  private final Map<Path, Set<String>> m_keysByModuleCache;
  private final Map<Path, Set<FileRange>> m_matches;

  static {
    String nlsKeyPattern = ITranslation.KEY_REGEX.pattern();
    Pattern jsTextKeyPat1 = Pattern.compile("\\$\\{textKey:(" + nlsKeyPattern + ')');
    Pattern jsTextKeyPat2 = Pattern.compile("session\\.text\\(('?)(" + nlsKeyPattern + ")('?)");

    Map<String, Collection<Pattern>> patternsByFile = new HashMap<>(3);
    patternsByFile.put(JavaTypes.JAVA_FILE_EXTENSION, singletonList(Pattern.compile("TEXTS\\.get\\((?:[a-zA-Z0-9_]+,\\s*)?(\")?(" + nlsKeyPattern + ")(\")?")));
    patternsByFile.put("js", unmodifiableList(asList(jsTextKeyPat1, jsTextKeyPat2)));
    patternsByFile.put("html", singletonList(Pattern.compile("<scout:message key=\"(" + nlsKeyPattern + ")\"\\s*/?>")));

    PATTERNS_BY_FILE_EXTENSION = unmodifiableMap(patternsByFile);
  }

  public MissingTranslationQuery() {
    m_keysByModuleCache = new ConcurrentHashMap<>();
    m_matches = new ConcurrentHashMap<>();
  }

  @Override
  public String name() {
    return "Search for text keys that are used in the code but cannot be found in the TextProviderServices";
  }

  protected static boolean acceptCandidate(FileQueryInput candidate) {
    String fullPath = candidate.file().toString().replace('\\', '/');
    if (fullPath.contains("/src/test/")
        || fullPath.contains("/archetype-resources/")
        || fullPath.contains("/generated-resources/")
        || fullPath.contains("/org.eclipse.scout.rt/eclipse-scout-core/")) {
      return false;
    }
    return PATTERNS_BY_FILE_EXTENSION.containsKey(candidate.fileExtension()) && !candidate.file().endsWith(TEXTS_FILE_NAME);
  }

  @Override
  public void searchIn(FileQueryInput candidate, IEnvironment env, IProgress progress) {
    if (!acceptCandidate(candidate)) {
      return;
    }

    Collection<Pattern> patterns = PATTERNS_BY_FILE_EXTENSION.get(candidate.fileExtension());
    CharSequence content = CharBuffer.wrap(candidate.fileContent());
    progress.init(name(), patterns.size());
    for (Pattern p : patterns) {
      Matcher matcher = p.matcher(content);
      while (matcher.find()) {
        useMatch(matcher, candidate, env, progress.newChild(1));
      }
    }
  }

  protected void useMatch(MatchResult match, FileQueryInput fileQueryInput, IEnvironment env, IProgress progress) {
    int keyGroup;
    if (match.groupCount() > 1) {
      // pattern with optional literal delimiter: '"' for java, ''' for js
      // check if the literal delimiter is present. if not present: not possible to detect the real key -> add to error list.
      keyGroup = 2;
      boolean noLiteral = Strings.isEmpty(match.group(1)) || Strings.isEmpty(match.group(3));
      if (noLiteral) {
        // is no string literal. might be e variable or concatenation.
        if (!tryToResolveConstant(match.group(keyGroup), fileQueryInput, env, progress)) {
          // cannot be resolved as constant. register as match for manual review
          registerMatch(match, fileQueryInput, m_matches, keyGroup);
        }
        return;
      }
    }
    else {
      // pattern without literal delimiter
      keyGroup = 1;
    }
    registerMatchIfNlsKeyIsMissing(match, keyGroup, fileQueryInput, env, progress);
  }

  protected boolean tryToResolveConstant(String constantName, FileQueryInput fileQueryInput, IEnvironment env, IProgress progress) {
    Pattern constantPat = Pattern.compile("final\\s+[\\w]+\\s+" + constantName + "\\s*=\\s*\"(" + ITranslation.KEY_REGEX.pattern() + ")\"");
    CharSequence content = CharBuffer.wrap(fileQueryInput.fileContent());
    Matcher constantMatcher = constantPat.matcher(content);
    boolean constantFound = false;
    while (constantMatcher.find()) {
      registerMatchIfNlsKeyIsMissing(constantMatcher, 1, fileQueryInput, env, progress);
      constantFound = true;
    }
    return constantFound;
  }

  protected void registerMatchIfNlsKeyIsMissing(MatchResult match, int keyGroup, FileQueryInput fileQueryInput, IEnvironment env, IProgress progress) {
    String key = match.group(keyGroup);
    if (!keyExists(fileQueryInput.module(), key, env, progress)) {
      registerMatch(match, fileQueryInput, m_matches, keyGroup);
    }
  }

  protected static void registerMatch(MatchResult match, FileQueryInput fileQueryInput, Map<Path, Set<FileRange>> targetList, int keyGroup) {
    int index = match.start(keyGroup);
    if (isIgnored(fileQueryInput.fileContent(), index)) {
      return;
    }

    targetList.computeIfAbsent(fileQueryInput.file(), f -> ConcurrentHashMap.newKeySet())
        .add(new FileRange(fileQueryInput.file(), index, match.end(keyGroup)));
  }

  protected boolean keyExists(Path modulePath, String key, IEnvironment env, IProgress progress) {
    return m_keysByModuleCache
        .computeIfAbsent(modulePath, mp -> keysVisibleForModule(mp, env, progress))
        .contains(key);
  }

  @Override
  public Stream<FileRange> result() {
    return m_matches.values().stream().flatMap(Set::stream);
  }

  @Override
  public Set<FileRange> result(Path file) {
    return getFromResultMap(m_matches, file);
  }

  protected static <K, V> Set<V> getFromResultMap(Map<K, Set<V>> map, K key) {
    Set<V> ranges = map.get(key);
    if (ranges == null || ranges.isEmpty()) {
      return emptySet();
    }
    return unmodifiableSet(ranges);
  }

  protected Set<String> keysVisibleForModule(Path modulePath, IEnvironment env, IProgress progress) {
    return TranslationStoreStack.create(modulePath, env, progress)
        .map(stack -> stack.allEntries()
            .map(ITranslation::key)
            .collect(toSet()))
        .orElse(emptySet());
  }

  @SuppressWarnings("HardcodedLineSeparator")
  protected static boolean isIgnored(char[] content, int offset) {
    int nlPos = indexOf('\n', content, offset);
    if (nlPos < IGNORE_MARKER.length()) {
      nlPos = content.length; // no more newline found: search to the end of the content
    }
    if (content[nlPos - 1] == '\r') {
      nlPos--;
    }
    String end = new String(content, nlPos - IGNORE_MARKER.length(), IGNORE_MARKER.length());
    return IGNORE_MARKER.equalsIgnoreCase(end);
  }

  protected static int indexOf(char toBeFound, char[] array, int start) {
    for (int i = start; i < array.length; ++i) {
      if (toBeFound == array[i]) {
        return i;
      }
    }
    return -1;
  }
}
