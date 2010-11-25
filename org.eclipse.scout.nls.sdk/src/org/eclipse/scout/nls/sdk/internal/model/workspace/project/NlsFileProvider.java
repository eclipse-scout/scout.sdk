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
package org.eclipse.scout.nls.sdk.internal.model.workspace.project;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;

/** <h4>NlsFileProvider</h4> */
public class NlsFileProvider {

  private Language[] m_languageOrder;
  private HashMap<Language, ITranslationFile> m_fileMap = new HashMap<Language, ITranslationFile>();

  public NlsFileProvider() {
    m_languageOrder = new Language[0];
  }

  public void addFile(ITranslationFile file) {
    Language language = file.getLanguage();
    if (m_fileMap.containsKey(language)) {
      NlsCore.logWarning("language file already exists! filename: " + file.getName());
    }
    else {
      m_fileMap.put(language, file);
      m_languageOrder = getOrderedLanguages(m_fileMap.keySet());
    }
  }

  private Language[] getOrderedLanguages(Set<Language> languages) {
    TreeMap<CompositeObject, Language> orderedLanguages = new TreeMap<CompositeObject, Language>();
    for (Language l : languages) {
      int index = 3;
      if (l.equals(Language.LANGUAGE_KEY)) {
        index = 1;
      }
      else if (l.equals(Language.LANGUAGE_DEFAULT)) {
        index = 2;
      }
      orderedLanguages.put(new CompositeObject(index, l.getDispalyName()), l);
    }
    return orderedLanguages.values().toArray(new Language[orderedLanguages.size()]);
  }

  /**
   * @param file
   */
  public void remove(ITranslationFile file) {
    if (!m_fileMap.containsValue(file)) {
      NlsCore.logWarning("trying to remove a non existing file! filename: " + file.getName());
    }
    else {

      m_fileMap.remove(file.getLanguage());
      m_languageOrder = getOrderedLanguages(m_fileMap.keySet());
    }
  }

  public ITranslationFile[] getSortedFiles() {
    LinkedList<ITranslationFile> files = new LinkedList<ITranslationFile>();
    for (Language lang : m_languageOrder) {
      files.add(m_fileMap.get(lang));
    }
    return files.toArray(new ITranslationFile[files.size()]);
  }

  public ITranslationFile[] getFiles() {
    return m_fileMap.values().toArray(new ITranslationFile[m_fileMap.size()]);
  }

  /**
   * @param language
   * @return
   */
  public ITranslationFile getFile(Language language) {
    return m_fileMap.get(language);
  }

  /**
   * @param language
   * @return
   */
  public boolean containsFile(Language language) {
    return m_fileMap.containsKey(language);
  }

  /**
   * @return
   */
  public Language[] getAllLanguages() {
    return m_languageOrder;
  }

}
