/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

/**
 * <h4>NlsEntry</h4>
 */
public class NlsEntry implements INlsEntry {

  private String m_key;
  private Map<Language, String> m_translations;
  private final INlsProject m_project;
  private final ReentrantReadWriteLock m_lock;

  /**
   * copy constructor used to apply modifications to the NlsProject
   *
   * @param row
   */
  public NlsEntry(INlsEntry row) {
    this(row, row.getProject());
  }

  public NlsEntry(INlsEntry row, INlsProject project) {
    m_lock = new ReentrantReadWriteLock(true);
    m_project = project;
    m_translations = new HashMap<>();
    update(row);
  }

  public NlsEntry(String key, INlsProject project) {
    m_lock = new ReentrantReadWriteLock(true);
    m_translations = new HashMap<>();
    m_project = project;
    m_key = key;
  }

  @Override
  public INlsProject getProject() {
    return m_project;
  }

  @Override
  public int getType() {
    return TYPE_LOCAL;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NlsEntry)) {
      return false;
    }
    NlsEntry other = (NlsEntry) obj;

    try {
      m_lock.readLock().lock();
      return Objects.equals(m_key, other.m_key)
          && Objects.equals(m_translations, other.m_translations)
          && Objects.equals(getType(), other.getType());
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public int hashCode() {
    try {
      m_lock.readLock().lock();
      int hash = 0;
      if (m_key != null) {
        hash ^= m_key.hashCode();
      }
      hash ^= m_translations.hashCode();
      hash ^= getType();
      return hash;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * @param superRow
   */
  public void update(INlsEntry refEntry) {
    try {
      m_lock.writeLock().lock();
      Map<Language, String> allTranslations = refEntry.getAllTranslations();
      m_translations = new HashMap<>(allTranslations.size());
      m_key = refEntry.getKey();
      for (Entry<Language, String> entry : allTranslations.entrySet()) {
        addTranslationInternal(entry.getKey(), entry.getValue());
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @Override
  public String getKey() {
    return m_key;
  }

  @Override
  public Map<Language, String> getAllTranslations() {
    try {
      m_lock.readLock().lock();
      return new HashMap<>(m_translations);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  protected void addTranslationInternal(Language language, String text) {
    try {
      m_lock.writeLock().lock();
      if (text == null || "".equals(text)) {
        text = null;
      }
      m_translations.put(language, text);
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  public void addTranslation(Language language, String text) {
    addTranslationInternal(language, text);
  }

  public void removeTranslation(Language language) {
    try {
      m_lock.writeLock().lock();
      m_translations.remove(language);
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @Override
  public String getTranslation(Language language) {
    return getTranslation(language, false);
  }

  @Override
  public String getTranslation(Language language, boolean defaultIfNotExist) {
    try {
      m_lock.readLock().lock();
      String translation = m_translations.get(language);
      if (translation == null && defaultIfNotExist) {
        Locale locale = new Locale(language.getLocale().getLanguage(), language.getLocale().getCountry());
        translation = m_translations.get(new Language(locale));
        if (translation == null) {
          locale = new Locale(locale.getLanguage());
          translation = m_translations.get(new Language(locale));
          if (translation == null) {
            translation = m_translations.get(Language.LANGUAGE_DEFAULT);
            if (translation == null) {
              translation = "!" + getKey() + "!";
            }
          }
        }
      }
      return translation;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public String toString() {
    return m_key;
  }

  public void setKey(String key) {
    try {
      m_lock.writeLock().lock();
      m_key = key;
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }
}
