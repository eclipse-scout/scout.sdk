/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link Translation}</h3>
 *
 * @since 7.0.0
 */
public class Translation implements ITranslation {

  private String m_key;
  private final Map<Language, String> m_texts;

  public Translation(ITranslation template) {
    m_key = Ensure.notNull(template.key());
    m_texts = new TreeMap<>(template.texts());
  }

  public Translation(String key) {
    m_key = Ensure.notNull(key);
    m_texts = new TreeMap<>();
  }

  protected Map<Language, String> textsMap() {
    return m_texts;
  }

  /**
   * Adds or removes a text entry.
   *
   * @param lang
   *          The {@link Language} that should be modified. Must not be {@code null}.
   * @param text
   *          If the text is {@code null} the specified {@link Language} is removed for this {@link ITranslation}.
   *          Otherwise the specified text is stored for the given {@link Language}. If there was already a text entry
   *          for this {@link Language} it is replaced.
   */
  public void putText(Language lang, String text) {
    if (Strings.isEmpty(text)) {
      textsMap().remove(Ensure.notNull(lang));
    }
    else {
      textsMap().put(Ensure.notNull(lang), text);
    }
  }

  @Override
  public Translation merged(ITranslation translation) {
    var merged = new Translation(this);
    if (translation != null) {
      translation.texts().forEach(merged::putText);
    }
    return merged;
  }

  /**
   * Changes the key of this {@link Translation}.
   *
   * @param newKey
   *          The new key. Must not be {@code null}.
   */
  public void setKey(String newKey) {
    m_key = Ensure.notNull(newKey);
  }

  @Override
  public String key() {
    return m_key;
  }

  @Override
  public Optional<String> text(Language language) {
    return Optional.ofNullable(textsMap().get(Ensure.notNull(language)));
  }

  @Override
  public Optional<String> bestText(Language language) {
    if (language == null) {
      return text(Language.LANGUAGE_DEFAULT);
    }

    var availableLocales = textsMap().keySet().stream()
        .collect(toMap(Language::locale, identity()));
    var range = new LanguageRange(language.locale().toLanguageTag(), LanguageRange.MAX_WEIGHT);
    var result = Locale.lookup(singletonList(range), availableLocales.keySet());

    return Optional.ofNullable(result)
        .map(availableLocales::get)
        .flatMap(this::text)
        .or(() -> text(Language.LANGUAGE_DEFAULT));
  }

  @Override
  public Map<Language, String> texts() {
    return unmodifiableMap(textsMap());
  }

  @Override
  public Stream<Language> languages() {
    return textsMap().keySet().stream();
  }

  @Override
  public int hashCode() {
    return 31 * key().hashCode() + textsMap().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    var other = (Translation) obj;
    return key().equals(other.key())
        && textsMap().equals(other.textsMap());
  }

  @Override
  public String toString() {
    return key() + '=' + textsMap();
  }
}
