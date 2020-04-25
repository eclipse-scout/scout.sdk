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

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link Language}</h3>
 *
 * @since 7.0.0
 */
public final class Language implements Comparable<Language> {

  /**
   * The regular expression of a valid {@link String} representation of a {@link Language}.
   */
  public static final String LANGUAGE_REGEX = "([^_]+)(?:_([\\w]{2}))?(?:_([\\w]{2}))?";

  /**
   * Represents the default language used if no other language matches.
   */
  public static final Language LANGUAGE_DEFAULT = new Language(new Locale("default"));

  private static final Pattern LANGUAGE_PATTERN = Pattern.compile('^' + LANGUAGE_REGEX + '$');

  private final Locale m_locale;
  private final String m_displayName; // cache display name for performance reasons!

  /**
   * Creates a new {@link Language} with specified {@link Locale}.
   *
   * @param locale
   *          The locale to use. Must not be {@code null}.
   */
  public Language(Locale locale) {
    m_locale = Ensure.notNull(locale);
    m_displayName = m_locale.getDisplayName(Locale.US);
  }

  /**
   * @return The {@link Locale} of this {@link Language}.
   */
  public Locale locale() {
    return m_locale;
  }

  /**
   * @return The display name of the {@link Language}. Is the same as {@link Locale#getDisplayName()}.
   */
  public String displayName() {
    return m_displayName;
  }

  @Override
  public int hashCode() {
    return displayName().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Language other = (Language) obj;
    return displayName().equals(other.displayName());
  }

  @Override
  public String toString() {
    return displayName();
  }

  /**
   * Parses the specified {@link CharSequence} to a {@link Language} or throws an {@link IllegalArgumentException} if it
   * is not valid according to {@link #LANGUAGE_REGEX}.
   *
   * @param name
   *          The name to parse. Must not be {@code null}.
   * @return The parsed {@link Language}.
   * @throws IllegalArgumentException
   *           if the specified name is not valid according to {@link #LANGUAGE_REGEX}.
   */
  public static Language parseThrowingOnError(CharSequence name) {
    return parse(name)
        .orElseThrow(() -> newFail("Invalid language name '{}'. Language cannot be parsed.", name));
  }

  /**
   * Tries to parse the specified name to a {@link Language}.
   *
   * @param name
   *          The name to parse. Must not be {@code null}.
   * @return the parsed {@link Language} or an empty {@link Optional} if it cannot be parsed according to
   *         {@link #LANGUAGE_REGEX}.
   */
  public static Optional<Language> parse(CharSequence name) {
    Matcher matcher = LANGUAGE_PATTERN.matcher(name);
    if (matcher.matches()) {
      if (matcher.group(2) == null
          && LANGUAGE_DEFAULT.locale().toString().equals(matcher.group(1))) {
        return Optional.of(LANGUAGE_DEFAULT);
      }

      String languageIso = matcher.group(1);
      String countryIso = matcher.group(2);
      if (countryIso == null) {
        countryIso = "";
      }
      String variantIso = matcher.group(3);
      if (variantIso == null) {
        variantIso = "";
      }
      return Optional.of(new Language(new Locale(languageIso, countryIso, variantIso)));
    }
    return Optional.empty();
  }

  @Override
  public int compareTo(Language o) {
    if (o == this) {
      return 0;
    }
    if (this == LANGUAGE_DEFAULT) {
      return -1;
    }
    if (o == LANGUAGE_DEFAULT) {
      return 1;
    }
    return displayName().compareTo(o.displayName());
  }
}
