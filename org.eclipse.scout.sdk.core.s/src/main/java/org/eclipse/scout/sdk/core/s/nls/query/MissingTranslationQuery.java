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
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.allForModule;
import static org.eclipse.scout.sdk.core.util.SourceState.isInCode;
import static org.eclipse.scout.sdk.core.util.SourceState.isInString;

import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Translation;
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
public class MissingTranslationQuery implements IFileQuery {

  public static final String IGNORE_MARKER = "NO-NLS-CHECK";
  private static final Map<String, List<AbstractTranslationSearch>> SEARCH_PATTERNS;
  public static final String JAVA_TEXTS_FILE_NAME = JavaTypes.simpleName(IScoutRuntimeTypes.TEXTS) + JavaTypes.JAVA_FILE_SUFFIX;
  public static final String JS_TEXTS_FILE_NAME = "texts.js";

  private final Map<Path, Optional<Set<String>>> m_keysByModuleCache;
  private final Map<Path, Set<FileRange>> m_matches;

  static {
    SEARCH_PATTERNS = Stream.of(new JavaTextsGetSearch(), new JsSessionTextSearch(), new JsonTextKeySearch(), new HtmlScoutMessageSearch())
        .collect(groupingBy(AbstractTranslationSearch::fileExtension));
  }

  public static Set<String> supportedFileTypes() {
    return unmodifiableSet(SEARCH_PATTERNS.keySet());
  }

  public MissingTranslationQuery() {
    m_keysByModuleCache = new ConcurrentHashMap<>();
    m_matches = new ConcurrentHashMap<>();
  }

  @Override
  public String name() {
    return "Search for text keys that are used in the code but cannot be found";
  }

  protected static boolean acceptCandidate(FileQueryInput candidate) {
    String fullPath = candidate.file().toString().replace('\\', '/');
    if (fullPath.contains("/archetype-resources/") || fullPath.contains("/generated-resources/")) {
      return false;
    }
    return SEARCH_PATTERNS.containsKey(candidate.fileExtension())
        && !candidate.file().endsWith(JAVA_TEXTS_FILE_NAME)
        && !candidate.file().endsWith(JS_TEXTS_FILE_NAME);
  }

  @Override
  public void searchIn(FileQueryInput candidate, IEnvironment env, IProgress progress) {
    if (!acceptCandidate(candidate)) {
      return;
    }

    List<AbstractTranslationSearch> patterns = SEARCH_PATTERNS.get(candidate.fileExtension());
    CharSequence content = CharBuffer.wrap(candidate.fileContent());
    int ticksByPattern = 10000;
    progress.init(patterns.size() * ticksByPattern, "{}. File: {}", name(), candidate.file());
    for (AbstractTranslationSearch search : patterns) {
      Matcher matcher = search.pattern().matcher(content);
      while (matcher.find()) {
        checkMatch(matcher, search, candidate, env, progress.newChild(ticksByPattern));
      }
    }
  }

  protected void checkMatch(MatchResult match, AbstractTranslationSearch search, FileQueryInput fileQueryInput, IEnvironment env, IProgress progress) {
    int keyGroup;
    if (match.groupCount() > 1) {
      // pattern with optional literal delimiter: '"' for java, ''' for js
      // check if the literal delimiter is present. if not present: not possible to detect the real key -> add to error list.
      keyGroup = 2;
      boolean noLiteral = Strings.isEmpty(match.group(1)) || Strings.isEmpty(match.group(3));
      if (noLiteral) {
        // is no string literal. might be e variable or concatenation.
        if (!tryToResolveConstant(match.group(keyGroup), search, fileQueryInput, env, progress)) {
          // cannot be resolved as constant. register as match for manual review
          registerMatchIfNotIgnored(match, keyGroup, Level.INFO.intValue(), search, fileQueryInput);
        }
        return;
      }
    }
    else {
      // pattern without literal delimiter
      keyGroup = 1;
    }
    registerMatchIfKeyIsMissing(match, keyGroup, Level.WARNING.intValue(), search, fileQueryInput, env, progress);
  }

  protected boolean tryToResolveConstant(String constantName, AbstractTranslationSearch search, FileQueryInput fileQueryInput, IEnvironment env, IProgress progress) {
    Pattern constantPat = Pattern.compile("\\s+" + constantName + "\\s*=\\s*[\"'](" + ITranslation.KEY_REGEX.pattern() + ")[\"'];");
    CharSequence content = CharBuffer.wrap(fileQueryInput.fileContent());
    Matcher constantMatcher = constantPat.matcher(content);
    boolean constantFound = false;
    while (constantMatcher.find()) {
      registerMatchIfKeyIsMissing(constantMatcher, 1, Level.WARNING.intValue(), search, fileQueryInput, env, progress);
      constantFound = true;
    }
    return constantFound;
  }

  protected void registerMatchIfKeyIsMissing(MatchResult match, int keyGroup, int severity, AbstractTranslationSearch search, FileQueryInput queryInput, IEnvironment env, IProgress progress) {
    String key = match.group(keyGroup);
    if (keyExistsForModule(queryInput.module(), key, env, progress)) {
      return;
    }
    registerMatchIfNotIgnored(match, keyGroup, severity, search, queryInput);
  }

  protected void registerMatchIfNotIgnored(MatchResult match, int keyGroup, int severity, AbstractTranslationSearch search, FileQueryInput queryInput) {
    if (!search.acceptMatch(match, keyGroup, queryInput)) {
      return;
    }
    int startIndex = match.start(keyGroup);
    int endIndex = match.end(keyGroup);
    m_matches.computeIfAbsent(queryInput.file(), f -> ConcurrentHashMap.newKeySet())
        .add(new FileRange(queryInput.file(), CharBuffer.wrap(queryInput.fileContent(), startIndex, endIndex - startIndex), startIndex, endIndex, severity));
  }

  protected boolean keyExistsForModule(Path modulePath, String key, IEnvironment env, IProgress progress) {
    return accessibleKeysForModule(modulePath, env, progress)
        .map(keys -> keys.contains(key))
        .orElse(true); // no stack could be created: the module is no scout module (no TextProviderService visible, but at least the Scout service should be available). In that case no markers should be created.
  }

  protected Optional<Set<String>> accessibleKeysForModule(Path modulePath, IEnvironment env, IProgress progress) {
    return m_keysByModuleCache.computeIfAbsent(modulePath, mp -> computeAccessibleKeysForModule(mp, env, progress));
  }

  protected static Optional<Set<String>> computeAccessibleKeysForModule(Path modulePath, IEnvironment env, IProgress progress) {
    List<ITranslationStore> stores = allForModule(modulePath, env, progress).collect(toList());
    if (stores.isEmpty()) {
      return Optional.empty(); // no stores found: it is no scout module. This module will be ignored in the search (no missing translations).
    }
    return Optional.of(stores.stream()
        .flatMap(ITranslationStore::keys)
        .collect(toSet()));
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

  protected static abstract class AbstractTranslationSearch {

    protected static final String NLS_KEY_PAT = ITranslation.KEY_REGEX.pattern();
    protected static final String JS_FILE_EXTENSION = "js";

    protected abstract Pattern pattern();

    protected abstract String fileExtension();

    protected abstract boolean acceptMatch(MatchResult match, int keyGroup, FileQueryInput fileQueryInput);

    @SuppressWarnings("HardcodedLineSeparator")
    protected static int nextLineEnd(char[] content, int offset) {
      int nlPos = indexOf('\n', content, offset);
      if (nlPos < 0) {
        return content.length; // no more newline found: search to the end of the content
      }
      if (nlPos > 0 && content[nlPos - 1] == '\r') {
        nlPos--;
      }
      return nlPos;
    }

    protected static int indexOf(char toBeFound, char[] array, int start) {
      for (int i = start; i < array.length; ++i) {
        if (toBeFound == array[i]) {
          return i;
        }
      }
      return -1;
    }

    protected static String textToNextNewLine(char[] content, int offset) {
      int lineEnd = nextLineEnd(content, offset);
      return new String(content, offset, lineEnd - offset);
    }

    protected static boolean lineEndsWithIgnoreMarker(char[] content, int offset) {
      int lineEnd = nextLineEnd(content, offset); // because of the regex patterns the full content cannot be shorter than the ignore marker -> no need to check for the bounds
      String end = new String(content, lineEnd - IGNORE_MARKER.length(), IGNORE_MARKER.length());
      return IGNORE_MARKER.equalsIgnoreCase(end);
    }

    protected static boolean isKeyInCode(char[] content, int offset) {
      /*the start index itself is inside of the string literal and therefore never in the code. subtract two (one to get to the string delim and one to get to the char before*/
      int posBeforeKeyMatch = offset - 2;
      return isInCode(content, posBeforeKeyMatch);
    }

    protected static boolean isAcceptedCodeMatch(MatchResult match, int keyGroup, char[] content) {
      int endIndex = match.end(keyGroup);
      if (lineEndsWithIgnoreMarker(content, endIndex)) {
        return false;
      }

      int startIndex = match.start(keyGroup);
      return isKeyInCode(content, startIndex);
    }
  }

  protected static class JsSessionTextSearch extends AbstractTranslationSearch {

    private static final Pattern JS_SESSION_TEXT_PAT = Pattern.compile("session\\.text\\(('?)(" + NLS_KEY_PAT + ")('?)\\s*[,)]");

    @Override
    protected Pattern pattern() {
      return JS_SESSION_TEXT_PAT;
    }

    @Override
    protected String fileExtension() {
      return JS_FILE_EXTENSION;
    }

    @Override
    protected boolean acceptMatch(MatchResult match, int keyGroup, FileQueryInput fileQueryInput) {
      return isAcceptedCodeMatch(match, keyGroup, fileQueryInput.fileContent());
    }
  }

  protected static class JsonTextKeySearch extends AbstractTranslationSearch {

    private static final Pattern JSON_PAT = Pattern.compile("\\$\\{textKey:(" + NLS_KEY_PAT + ')');

    @Override
    protected Pattern pattern() {
      return JSON_PAT;
    }

    @Override
    protected String fileExtension() {
      return JS_FILE_EXTENSION;
    }

    @Override
    protected boolean acceptMatch(MatchResult match, int keyGroup, FileQueryInput fileQueryInput) {
      int endIndex = match.end(keyGroup);
      if (lineEndsWithIgnoreMarker(fileQueryInput.fileContent(), endIndex)) {
        return false;
      }

      int startIndex = match.start(keyGroup);
      return isInString(fileQueryInput.fileContent(), startIndex);
    }
  }

  protected static class JavaTextsGetSearch extends AbstractTranslationSearch {

    private static final Pattern TEXTS_GET_PAT = Pattern.compile("TEXTS\\.get\\((?:[a-zA-Z0-9_]+,\\s*)?(\")?(" + NLS_KEY_PAT + ")(\")?\\s*[,)]");

    @Override
    protected Pattern pattern() {
      return TEXTS_GET_PAT;
    }

    @Override
    protected String fileExtension() {
      return JavaTypes.JAVA_FILE_EXTENSION;
    }

    @Override
    protected boolean acceptMatch(MatchResult match, int keyGroup, FileQueryInput fileQueryInput) {
      return isAcceptedCodeMatch(match, keyGroup, fileQueryInput.fileContent());
    }
  }

  protected static class HtmlScoutMessageSearch extends AbstractTranslationSearch {
    private static final Pattern HTML_PAT = Pattern.compile("\\s+key=\"(" + NLS_KEY_PAT + ")\""); // there is no 'key' attribute in html. so no need to check for the scout:message tag

    @Override
    protected Pattern pattern() {
      return HTML_PAT;
    }

    @Override
    protected String fileExtension() {
      return "html";
    }

    @Override
    protected boolean acceptMatch(MatchResult match, int keyGroup, FileQueryInput fileQueryInput) {
      int endOfMatch = match.end(keyGroup);
      return !textToNextNewLine(fileQueryInput.fileContent(), endOfMatch).toUpperCase(Locale.ENGLISH).endsWith(IGNORE_MARKER + " -->");
    }
  }
}
