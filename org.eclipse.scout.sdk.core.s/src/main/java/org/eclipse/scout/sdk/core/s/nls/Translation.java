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

import static java.util.Collections.unmodifiableMap;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link Translation}</h3>
 *
 * @since 7.0.0
 */
public class Translation implements ITranslation {

  private String m_key;
  private final Map<Language, String> m_translations;

  public Translation(ITranslation template) {
    m_key = Ensure.notNull(Ensure.notNull(template).key());
    m_translations = new TreeMap<>(template.translations());
  }

  public Translation(String key) {
    m_key = Ensure.notNull(key);
    m_translations = new TreeMap<>();
  }

  protected Map<Language, String> translationsMap() {
    return m_translations;
  }

  /**
   * Adds or removes an entry.
   *
   * @param lang
   *          The {@link Language} that should be modified. Must not be {@code null}.
   * @param text
   *          If the text is {@code null} the specified language is removed. Otherwise the specified text is stored for
   *          the specified language. If there was already a text entry for this language it is replaced.
   */
  public void putTranslation(Language lang, String text) {
    if (text == null) {
      translationsMap().remove(Ensure.notNull(lang));
    }
    else {
      translationsMap().put(Ensure.notNull(lang), text);
    }
  }

  /**
   * Replaces all translations with the specified ones.
   *
   * @param newTranslations
   *          The new translation entries.
   */
  public void setTranslations(Map<Language, String> newTranslations) {
    translationsMap().clear();
    if (newTranslations == null || newTranslations.isEmpty()) {
      return;
    }
    translationsMap().putAll(newTranslations);
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
  public Optional<String> translation(Language language) {
    return Optional.ofNullable(translationsMap().get(Ensure.notNull(language)));
  }

  @Override
  public Map<Language, String> translations() {
    return unmodifiableMap(translationsMap());
  }

  @Override
  public int hashCode() {
    return 31 * key().hashCode() + translationsMap().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Translation other = (Translation) obj;
    return key().equals(other.key())
        && translationsMap().equals(other.translationsMap());
  }

  @Override
  public String toString() {
    return key() + '=' + translationsMap();
  }
}
