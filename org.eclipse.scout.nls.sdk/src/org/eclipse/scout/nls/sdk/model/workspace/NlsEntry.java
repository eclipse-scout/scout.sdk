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
package org.eclipse.scout.nls.sdk.model.workspace;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * <h4>NlsEntry</h4>
 */
public class NlsEntry implements INlsEntry {
  private String m_key;
  private HashMap<Language, String> m_translations = new HashMap<Language, String>();
  private int m_referenceCount = -1;
  private final INlsProject m_project;

  /**
   * copy constructor used to apply modifications to the NlsProject
   * 
   * @param row
   */
  public NlsEntry(INlsEntry row) {
    m_project = row.getProject();
    update(row);
  }

  public NlsEntry(String key, INlsProject project) {
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

  /**
   * @param superRow
   */
  public void update(INlsEntry refEntry) {
    m_translations = new HashMap<Language, String>();
    m_key = refEntry.getKey();
    for (Entry<Language, String> entry : refEntry.getAllTranslations().entrySet()) {
      addTranslationInternal(entry.getKey(), entry.getValue());
    }
    m_referenceCount = refEntry.getReferenceCount();
  }

  @Override
  public String getKey() {
    return m_key;
  }

  @Override
  public Map<Language, String> getAllTranslations() {
    return new HashMap<Language, String>(m_translations);
  }

  protected void addTranslationInternal(Language language, String text) {
    if (text == null || text.equals("")) {
      text = null;
    }
    m_translations.put(language, text);
  }

  public void addTranslation(Language language, String text) {
    addTranslationInternal(language, text);
  }

  public void removeTranslation(Language language) {
    m_translations.remove(language);
  }

  @Override
  public String getTranslation(Language language) {
    return getTranslation(language, false);
  }

  @Override
  public String getTranslation(Language language, boolean defaultIfNotExist) {
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

  @Override
  public int getReferenceCount() {
    return m_referenceCount;
  }

  public void setReferenceCount(int refCount) {
    m_referenceCount = refCount;

  }

  @Override
  public String toString() {
    return m_key;
  }

  public void setKey(String key) {
    m_key = key;
  }
}
