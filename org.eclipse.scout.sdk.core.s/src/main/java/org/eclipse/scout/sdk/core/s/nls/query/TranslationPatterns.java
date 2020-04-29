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
import static org.eclipse.scout.sdk.core.util.SourceState.isInCode;
import static org.eclipse.scout.sdk.core.util.SourceState.isInString;

import java.nio.CharBuffer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryInput;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.util.Chars;
import org.eclipse.scout.sdk.core.util.JavaTypes;

public final class TranslationPatterns {

  public static final String IGNORE_MARKER = "NO-NLS-CHECK";
  private static final String JS_FILE_EXTENSION = "js";

  public static Stream<AbstractTranslationPattern> all() {
    return Stream.of(JavaTextsGetSearch.INSTANCE,
        JsSessionTextSearch.INSTANCE,
        JsonTextKeySearch.INSTANCE,
        HtmlScoutMessageSearch.INSTANCE);
  }

  public static Set<String> supportedFileExtensions() {
    return all()
        .map(AbstractTranslationPattern::fileExtension)
        .collect(toSet());
  }

  public abstract static class AbstractTranslationPattern {
    protected static final String NLS_KEY_PAT = ITranslation.KEY_REGEX.pattern();

    public abstract Pattern buildPattern(String keyPart);

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
      int lineEnd = Chars.nextLineEnd(content, offset); // because of the regex patterns the full content cannot be shorter than the ignore marker -> no need to check for the bounds
      CharBuffer end = CharBuffer.wrap(content, lineEnd - IGNORE_MARKER.length(), IGNORE_MARKER.length());
      return Chars.equals(IGNORE_MARKER, end, false);
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

  public static final class JsSessionTextSearch extends AbstractTranslationPattern {

    public static final AbstractTranslationPattern INSTANCE = new JsSessionTextSearch();
    public static final Pattern REGEX = INSTANCE.buildPattern(NLS_KEY_PAT);

    private JsSessionTextSearch() {
    }

    @Override
    public Pattern buildPattern(String keyPart) {
      return Pattern.compile("session\\.text\\((['`\"]?)(" + NLS_KEY_PAT + ")(['`\"]?)\\s*[,)]");
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

  public static final class JsonTextKeySearch extends AbstractTranslationPattern {

    public static final String JSON_TEXT_KEY_PREFIX = "${textKey:";
    public static final String JSON_TEXT_KEY_SUFFIX = "}";
    public static final AbstractTranslationPattern INSTANCE = new JsonTextKeySearch();
    public static final Pattern REGEX = INSTANCE.buildPattern(NLS_KEY_PAT);

    private JsonTextKeySearch() {
    }

    @Override
    public Pattern buildPattern(String keyPart) {
      return Pattern.compile(Pattern.quote(JSON_TEXT_KEY_PREFIX) + '(' + keyPart + ')');
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

  public static final class JavaTextsGetSearch extends AbstractTranslationPattern {

    public static final AbstractTranslationPattern INSTANCE = new JavaTextsGetSearch();
    public static final Pattern REGEX = INSTANCE.buildPattern(NLS_KEY_PAT);

    private JavaTextsGetSearch() {
    }

    @Override
    public Pattern buildPattern(String keyPart) {
      return Pattern.compile("TEXTS\\.get\\((?:[a-zA-Z0-9_]+,\\s*)?(\")?(" + keyPart + ")(\")?\\s*[,)]");
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

  public static final class HtmlScoutMessageSearch extends AbstractTranslationPattern {

    public static final AbstractTranslationPattern INSTANCE = new HtmlScoutMessageSearch();
    public static final Pattern REGEX = INSTANCE.buildPattern(NLS_KEY_PAT);

    private HtmlScoutMessageSearch() {
    }

    @Override
    public Pattern buildPattern(String keyPart) {
      return Pattern.compile("\\s+key=['\"](" + keyPart + ")[\"']"); // there is no 'key' attribute in html. so no need to check for the scout:message tag
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
          .toString()
          .toUpperCase(Locale.ENGLISH)
          .endsWith(IGNORE_MARKER + " -->");
      if (isIgnored) {
        return Optional.empty();
      }
      return Optional.of(toFileRange(match, fileQueryInput, keyGroup));
    }

    public static CharSequence textToNextNewLine(char[] searchIn, int offset) {
      int lineEnd = Chars.nextLineEnd(searchIn, offset);
      return CharBuffer.wrap(searchIn, offset, lineEnd - offset);
    }
  }
}
