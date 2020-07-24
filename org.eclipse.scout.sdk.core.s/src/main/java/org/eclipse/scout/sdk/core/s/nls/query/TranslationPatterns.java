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

import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.util.JavaTypes.simpleName;
import static org.eclipse.scout.sdk.core.util.SourceState.isInCode;
import static org.eclipse.scout.sdk.core.util.SourceState.isInString;
import static org.eclipse.scout.sdk.core.util.Strings.nextLineEnd;

import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

public final class TranslationPatterns {

  public static final String IGNORE_MARKER = "NO-NLS-CHECK";
  private static final String JS_FILE_EXTENSION = "js";

  private static final Map<Class<? extends AbstractTranslationPattern>, AbstractTranslationPattern> PATTERN_MAP = new HashMap<>();
  static {
    Stream.of(JavaTextsGetPattern.INSTANCE, JsSessionTextPattern.INSTANCE, JsonTextKeyPattern.INSTANCE, HtmlScoutMessagePattern.INSTANCE)
        .forEach(TranslationPatterns::registerPattern);
  }

  private TranslationPatterns() {
  }

  /**
   * Adds an {@link AbstractTranslationPattern} to the list of known patterns.
   * 
   * @param pattern
   *          The {@link AbstractTranslationPattern} to add. Must not be {@code null}.
   * @return {@code true} if the element was already registered (and was replaced), {@code false} if this pattern was
   *         not yet registered.
   */
  public static synchronized boolean registerPattern(AbstractTranslationPattern pattern) {
    return PATTERN_MAP.put(Ensure.notNull(pattern).getClass(), pattern) != null;
  }

  /**
   * Remove a pattern from the list of known patterns.
   * 
   * @param pattern
   *          The {@link AbstractTranslationPattern} to remove. Must not be {@code null}.
   * @return {@code true} if it was successfully removed, {@code false} if the element was not in the list and therefore
   *         was not removed.
   */
  public static synchronized boolean removePattern(AbstractTranslationPattern pattern) {
    return PATTERN_MAP.remove(Ensure.notNull(pattern).getClass()) != null;
  }

  /**
   * @return All {@link AbstractTranslationPattern} known.
   */
  public static synchronized Stream<AbstractTranslationPattern> all() {
    return new HashSet<>(PATTERN_MAP.values()).stream();
  }

  /**
   * @return A {@link Set} containing all file extensions in which Scout translation may exist.
   */
  public static Set<String> supportedFileExtensions() {
    return all()
        .map(AbstractTranslationPattern::fileExtension)
        .collect(toSet());
  }

  public abstract static class AbstractTranslationPattern {
    protected static final String NLS_KEY_PAT = ITranslation.KEY_REGEX.pattern();

    public abstract Pattern pattern();

    public abstract String fileExtension();

    public abstract Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput);

    protected static FileRange toFileRange(MatchResult match, FileQueryInput fileQueryInput, int keyGroup) {
      int startIndex = match.start(keyGroup);
      int endIndex = match.end(keyGroup);
      CharSequence key = CharBuffer.wrap(fileQueryInput.fileContent(), startIndex, endIndex - startIndex);
      return new FileRange(fileQueryInput.file(), key, startIndex, endIndex);
    }

    protected static boolean lineEndsWithIgnoreMarker(char[] content, int offset) {
      int lineEnd = Strings.nextLineEnd(content, offset); // because of the regex patterns the full content cannot be shorter than the ignore marker -> no need to check for the bounds
      CharBuffer end = CharBuffer.wrap(content, lineEnd - IGNORE_MARKER.length(), IGNORE_MARKER.length());
      return Strings.equals(IGNORE_MARKER, end, false);
    }

    protected static boolean isKeyInCode(char[] content, int offset) {
      /* the start index itself is inside of the string literal and therefore never in the code. subtract two (one to get to the string delimiter and one to get to the char before */
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

    protected static Optional<FileRange> keyRangeIfInCode(MatchResult match, FileQueryInput fileQueryInput, int keyGroup) {
      if (!isAcceptedCodeMatch(match, keyGroup, fileQueryInput.fileContent())) {
        return Optional.empty();
      }
      return Optional.of(toFileRange(match, fileQueryInput, keyGroup));
    }
  }

  public static final class JsSessionTextPattern extends AbstractTranslationPattern {

    public static final AbstractTranslationPattern INSTANCE = new JsSessionTextPattern();
    public static final Pattern REGEX = Pattern.compile("session\\.text\\((['`\"]?)(" + NLS_KEY_PAT + ")(['`\"]?)\\s*[,)]");

    private JsSessionTextPattern() {
    }

    @Override
    public Pattern pattern() {
      return REGEX;
    }

    @Override
    public String fileExtension() {
      return JS_FILE_EXTENSION;
    }

    @Override
    public Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput) {
      return keyRangeIfInCode(match, fileQueryInput, 2);
    }
  }

  public static final class JsonTextKeyPattern extends AbstractTranslationPattern {

    public static final String JSON_TEXT_KEY_PREFIX = "${textKey:";
    public static final String JSON_TEXT_KEY_SUFFIX = "}";
    public static final AbstractTranslationPattern INSTANCE = new JsonTextKeyPattern();
    public static final Pattern REGEX = Pattern.compile(Pattern.quote(JSON_TEXT_KEY_PREFIX) + '(' + NLS_KEY_PAT + ')');

    private JsonTextKeyPattern() {
    }

    @Override
    public Pattern pattern() {
      return REGEX;
    }

    @Override
    public String fileExtension() {
      return JS_FILE_EXTENSION;
    }

    @Override
    public Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput) {
      int keyGroup = 1;
      if (lineEndsWithIgnoreMarker(fileQueryInput.fileContent(), match.end(keyGroup))) {
        return Optional.empty();
      }

      int startIndex = match.start(1);
      if (!isInString(fileQueryInput.fileContent(), startIndex)) {
        return Optional.empty();
      }

      return Optional.of(toFileRange(match, fileQueryInput, keyGroup));
    }
  }

  public static final class JavaTextsGetPattern extends AbstractTranslationPattern {

    public static final String GET_METHOD_NAME = "get";
    public static final String GET_WITH_FALLBACK_METHOD_NAME = "getWithFallback";
    public static final AbstractTranslationPattern INSTANCE = new JavaTextsGetPattern();
    public static final Pattern REGEX = Pattern.compile(simpleName(IScoutRuntimeTypes.TEXTS) + "\\." + GET_METHOD_NAME + "\\((?:[a-zA-Z0-9_]+,\\s*)?(\")?(" + NLS_KEY_PAT + ")(\")?\\s*[,)]");

    private JavaTextsGetPattern() {
    }

    @Override
    public Pattern pattern() {
      return REGEX;
    }

    @Override
    public String fileExtension() {
      return JavaTypes.JAVA_FILE_EXTENSION;
    }

    @Override
    public Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput) {
      return keyRangeIfInCode(match, fileQueryInput, 2);
    }
  }

  public static final class HtmlScoutMessagePattern extends AbstractTranslationPattern {

    public static final String ATTRIBUTE_NAME = "key";
    public static final AbstractTranslationPattern INSTANCE = new HtmlScoutMessagePattern();
    public static final Pattern REGEX = Pattern.compile("\\s+" + ATTRIBUTE_NAME + "=['\"](" + NLS_KEY_PAT + ")[\"']"); // there is no 'key' attribute in html. so no need to check for the scout:message tag

    private HtmlScoutMessagePattern() {
    }

    @Override
    public Pattern pattern() {
      return REGEX;
    }

    @Override
    public String fileExtension() {
      return "html";
    }

    @Override
    public Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput) {
      int keyGroup = 1;
      boolean isIgnored = textToNextNewLine(fileQueryInput.fileContent(), match.end(keyGroup))
          .toUpperCase(Locale.ENGLISH)
          .endsWith(IGNORE_MARKER + " -->");
      if (isIgnored) {
        return Optional.empty();
      }
      return Optional.of(toFileRange(match, fileQueryInput, keyGroup));
    }

    public static String textToNextNewLine(char[] searchIn, int offset) {
      int lineEnd = nextLineEnd(searchIn, offset);
      return new String(searchIn, offset, lineEnd - offset);
    }
  }
}