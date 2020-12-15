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

import static java.util.stream.Collectors.joining;
import static org.eclipse.scout.sdk.core.util.SourceState.isInCode;
import static org.eclipse.scout.sdk.core.util.SourceState.isInString;
import static org.eclipse.scout.sdk.core.util.Strings.nextLineEnd;

import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.IWebConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

public final class TranslationPatterns {

  public static final String IGNORE_MARKER = "NO-NLS-CHECK";

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

  public abstract static class AbstractTranslationPattern {
    protected static final String NLS_KEY_PAT = ITranslation.KEY_REGEX.pattern();

    public abstract Pattern pattern();

    public abstract String fileExtension();

    public abstract Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput);

    protected static FileRange toFileRange(MatchResult match, FileQueryInput fileQueryInput, int keyGroup) {
      var startIndex = match.start(keyGroup);
      var endIndex = match.end(keyGroup);
      var key = CharBuffer.wrap(fileQueryInput.fileContent(), startIndex, endIndex);
      return new FileRange(fileQueryInput.file(), fileQueryInput.module(), key, startIndex, endIndex);
    }

    protected static boolean lineEndsWithIgnoreMarker(CharSequence content, int offset) {
      var lineEnd = nextLineEnd(content, offset); // because of the regex patterns the full content cannot be shorter than the ignore marker -> no need to check for the bounds
      var end = CharBuffer.wrap(content, lineEnd - IGNORE_MARKER.length(), lineEnd);
      return Strings.equals(IGNORE_MARKER, end, false);
    }

    protected static boolean isKeyInCode(CharSequence content, int offset) {
      /* the start index itself is inside of the string literal and therefore never in the code. subtract two (one to get to the string delimiter and one to get to the char before */
      var posBeforeKeyMatch = offset - 2;
      return isInCode(content, posBeforeKeyMatch);
    }

    protected static boolean isAcceptedCodeMatch(MatchResult match, int keyGroup, CharSequence content) {
      var endIndex = match.end(keyGroup);
      if (lineEndsWithIgnoreMarker(content, endIndex)) {
        return false;
      }

      var startIndex = match.start(keyGroup);
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
      return IWebConstants.JS_FILE_EXTENSION;
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
      return IWebConstants.JS_FILE_EXTENSION;
    }

    @Override
    public Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput) {
      var keyGroup = 1;
      if (lineEndsWithIgnoreMarker(fileQueryInput.fileContent(), match.end(keyGroup))) {
        return Optional.empty();
      }

      var startIndex = match.start(1);
      if (!isInString(fileQueryInput.fileContent(), startIndex)) {
        return Optional.empty();
      }

      return Optional.of(toFileRange(match, fileQueryInput, keyGroup));
    }
  }

  public static final class JavaTextsGetPattern extends AbstractTranslationPattern {

    public static final AbstractTranslationPattern INSTANCE = new JavaTextsGetPattern();
    public static final Pattern REGEX = Pattern.compile(computeTextsGetRegex());

    private static String computeTextsGetRegex() {
      return ScoutApi.allKnown()
          .map(JavaTextsGetPattern::toTextsGetRegex)
          .distinct()
          .collect(joining("|"));
    }

    private static String toTextsGetRegex(IScoutVariousApi api) {
      var texts = api.TEXTS();
      return texts.simpleName() + "\\." + texts.getMethodName() + "\\((?:[a-zA-Z0-9_]+,\\s*)?(\")?(" + NLS_KEY_PAT + ")(\")?\\s*[,)]";
    }

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
    public static final String SCOUT_MESSAGE_TAG_NAME = "scout:message";
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
      return IWebConstants.HTML_FILE_EXTENSION;
    }

    @Override
    public Optional<FileRange> keyRangeIfAccept(MatchResult match, FileQueryInput fileQueryInput) {
      var keyGroup = 1;
      var textToNextNewLine = textToNextNewLine(fileQueryInput.fileContent(), match.end(keyGroup));
      var isIgnored = Strings.endsWith(textToNextNewLine, IGNORE_MARKER + " -->", false);
      if (isIgnored) {
        return Optional.empty();
      }
      return Optional.of(toFileRange(match, fileQueryInput, keyGroup));
    }

    public static CharSequence textToNextNewLine(CharSequence searchIn, int offset) {
      var lineEnd = nextLineEnd(searchIn, offset);
      return searchIn.subSequence(offset, lineEnd);
    }
  }
}
