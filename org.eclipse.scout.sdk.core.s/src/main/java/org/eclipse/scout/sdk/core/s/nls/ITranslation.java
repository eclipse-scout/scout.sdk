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

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

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
   * Regular expression matching translation keys (see {@link #key()}).
   */
  Pattern KEY_REGEX = Pattern.compile("[A-Za-z][a-zA-Z0-9_.\\-]{0,200}");

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
   *          country and variant of the {@link Language#locale()}). Must not be {@code null}.
   * @return The translation text or an empty {@link Optional}.
   */
  Optional<String> text(Language language);

  /**
   * @return An unmodifiable view on all language-text mappings of this {@link ITranslation}.
   */
  Map<Language, String> texts();

  @Override
  default int compareTo(ITranslation o) {
    return TRANSLATION_COMPARATOR.compare(this, o);
  }
}
