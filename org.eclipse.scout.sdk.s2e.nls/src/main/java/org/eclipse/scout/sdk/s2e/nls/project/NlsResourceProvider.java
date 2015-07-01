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
package org.eclipse.scout.sdk.s2e.nls.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.resource.ITranslationResource;

public class NlsResourceProvider {

  private List<Language> m_languagesOrdered;
  private final Map<Language, ITranslationResource> m_resourceMap = new HashMap<>();

  public NlsResourceProvider() {
    m_languagesOrdered = new ArrayList<>(0);
  }

  public void addResource(ITranslationResource r) {
    Language language = r.getLanguage();
    if (m_resourceMap.containsKey(language)) {
      NlsCore.logWarning("language already exists!");
    }
    else {
      m_resourceMap.put(language, r);
      m_languagesOrdered = getOrderedLanguages(m_resourceMap.keySet());
    }
  }

  @SuppressWarnings("rawtypes")
  private static List<Language> getOrderedLanguages(Set<Language> languages) {
    TreeMap<Comparable[], Language> orderedLanguages = new TreeMap<>(new Comparator<Comparable[]>() {
      @Override
      public int compare(Comparable[] o1, Comparable[] o2) {
        for (int i = 0; i < o1.length; i++) {
          @SuppressWarnings("unchecked")
          int dif = o1[i].compareTo(o2[i]);
          if (dif != 0) {
            return dif;
          }
        }
        return 0;
      }
    });
    for (Language l : languages) {
      int index = 3;
      if (l.equals(Language.LANGUAGE_KEY)) {
        index = 1;
      }
      else if (l.equals(Language.LANGUAGE_DEFAULT)) {
        index = 2;
      }
      orderedLanguages.put(new Comparable[]{Integer.valueOf(index), l.getDispalyName(), l.hashCode()}, l);
    }
    return new ArrayList<>(orderedLanguages.values());
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
      m_languagesOrdered = getOrderedLanguages(m_resourceMap.keySet());
    }
  }

  public List<ITranslationResource> getSortedResources() {
    List<ITranslationResource> resources = new ArrayList<>(m_languagesOrdered.size());
    for (Language lang : m_languagesOrdered) {
      resources.add(m_resourceMap.get(lang));
    }
    return resources;
  }

  public List<ITranslationResource> getResources() {
    return new ArrayList<>(m_resourceMap.values());
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
  public List<Language> getAllLanguages() {
    return new ArrayList<>(m_languagesOrdered);
  }
}
