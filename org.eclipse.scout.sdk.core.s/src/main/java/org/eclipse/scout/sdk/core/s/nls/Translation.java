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
  private final Map<Language, String> m_texts;

  public Translation(ITranslation template) {
    m_key = Ensure.notNull(Ensure.notNull(template).key());
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
    if (text == null) {
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
   * Replaces all translations with the specified ones.
   *
   * @param newTranslations
   *          The new translation entries.
   */
  public void setTexts(Map<Language, String> newTranslations) {
    textsMap().clear();
    if (newTranslations == null || newTranslations.isEmpty()) {
      return;
    }
    textsMap().putAll(newTranslations);
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
  public Map<Language, String> texts() {
    return unmodifiableMap(textsMap());
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
