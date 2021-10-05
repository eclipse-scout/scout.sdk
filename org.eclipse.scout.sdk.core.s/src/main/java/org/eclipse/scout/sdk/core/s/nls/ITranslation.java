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
package org.eclipse.scout.sdk.core.s.nls;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <h3>{@link ITranslation}</h3>
 * <p>
 * Represents a single translation. A translation consists of an unique identifier (key) and a text for each supported
 * {@link Language}.
 *
 * @since 7.0.0
 */
public interface ITranslation extends Comparable<ITranslation> {

  /**
   * Character set allowed for translation keys.
   */
  @SuppressWarnings("RegExpRedundantEscape")
  String KEY_ALLOWED_CHARACTER_SET = "a-zA-Z0-9_.\\-";

  /**
   * Regular expression matching translation keys (see {@link #key()}).
   */
  Pattern KEY_REGEX = Pattern.compile("[A-Za-z][" + KEY_ALLOWED_CHARACTER_SET + "]{0,200}");

  /**
   * The default comparator for {@link ITranslation}s comparing by key, then by text for the default language and
   * finally by all other texts.
   */
  Comparator<ITranslation> TRANSLATION_COMPARATOR = Comparator.comparing(ITranslation::key)
      .thenComparing(t -> t.text(Language.LANGUAGE_DEFAULT).orElse(null))
      .thenComparing(t -> String.join("", t.texts().values()));

  /**
   * @return The key as {@link String}. A key is unique within an {@link ITranslationStore}.
   */
  String key();

  /**
   * Creates a new {@link ITranslation} with the texts from this instance and the given one merged. The texts from the
   * provided {@link ITranslation} take precedence over the ones from this instance.
   * <p>
   * This instance remains untouched.
   *
   * @param translation
   *          The {@link ITranslation} whose texts should be merged with this one.
   */
  ITranslation merged(ITranslation translation);

  /**
   * Gets the translation text for the specified {@link Language}.
   *
   * @param language
   *          The {@link Language} for which the text should be returned. The language must match exactly (including
   *          country and variant of the {@link Language#locale()}). As alternative use {@link #bestText(Language)}.
   *          Must not be {@code null}.
   * @return The translation text or an empty {@link Optional}.
   * @see #bestText(Language)
   */
  Optional<String> text(Language language);

  /**
   * Gets the translation text that best suites the given {@link Language}.
   * 
   * @param language
   *          The {@link Language} for which the best matching text should be returned. May be {@code null}. In that
   *          case the text for the {@link Language#LANGUAGE_DEFAULT} is returned.
   * @return The best text of this translation that matches the given {@link Language}.
   * @see #text(Language)
   */
  Optional<String> bestText(Language language);

  /**
   * @return An unmodifiable view on all language-text mappings of this {@link ITranslation}.
   */
  Map<Language, String> texts();

  /**
   * @return A {@link Stream} holding all languages for which this {@link ITranslation} contains a text.
   */
  Stream<Language> languages();

  @Override
  default int compareTo(ITranslation o) {
    return TRANSLATION_COMPARATOR.compare(this, o);
  }
}
