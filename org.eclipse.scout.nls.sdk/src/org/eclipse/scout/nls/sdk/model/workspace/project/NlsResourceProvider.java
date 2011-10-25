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
package org.eclipse.scout.nls.sdk.model.workspace.project;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.ITranslationResource;

/** <h4>NlsResourceProvider</h4> */
public class NlsResourceProvider {

  private Language[] m_languageOrder;
  private HashMap<Language, ITranslationResource> m_resourceMap = new HashMap<Language, ITranslationResource>();

  public NlsResourceProvider() {
    m_languageOrder = new Language[0];
  }

  public void addResource(ITranslationResource r) {
    Language language = r.getLanguage();
    if (m_resourceMap.containsKey(language)) {
      NlsCore.logWarning("language already exists!");
    }
    else {
      m_resourceMap.put(language, r);
      m_languageOrder = getOrderedLanguages(m_resourceMap.keySet());
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
   * @param Resource
   */
  public void remove(ITranslationResource r) {
    if (!m_resourceMap.containsValue(r)) {
      NlsCore.logWarning("trying to remove a non existing resource!");
    }
    else {

      m_resourceMap.remove(r.getLanguage());
      m_languageOrder = getOrderedLanguages(m_resourceMap.keySet());
    }
  }

  public ITranslationResource[] getSortedResources() {
    LinkedList<ITranslationResource> resources = new LinkedList<ITranslationResource>();
    for (Language lang : m_languageOrder) {
      resources.add(m_resourceMap.get(lang));
    }
    return resources.toArray(new ITranslationResource[resources.size()]);
  }

  public ITranslationResource[] getResources() {
    return m_resourceMap.values().toArray(new ITranslationResource[m_resourceMap.size()]);
  }

  /**
   * @param language
   * @return
   */
  public ITranslationResource getResource(Language language) {
    return m_resourceMap.get(language);
  }

  /**
   * @param language
   * @return
   */
  public boolean containsResource(Language language) {
    return m_resourceMap.containsKey(language);
  }

  /**
   * @return
   */
  public Language[] getAllLanguages() {
    return m_languageOrder;
  }

}
