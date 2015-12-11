/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.event.EventListenerList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.model.Language;

public abstract class AbstractTranslationResource implements ITranslationResource {

  private Map<String/* key */, String/* translation */> m_entries;

  private final Language m_language;

  private EventListenerList m_eventListeners;

  /**
   * the language of the resource
   *
   * @param language
   */
  public AbstractTranslationResource(Language language) {
    m_language = language;
    m_entries = new HashMap<>();
    m_eventListeners = new EventListenerList();
  }

  @Override
  public void addTranslationResourceListener(ITranslationResourceListener listener) {
    m_eventListeners.add(ITranslationResourceListener.class, listener);
  }

  @Override
  public void removeTranslationResourceListener(ITranslationResourceListener listener) {
    m_eventListeners.remove(ITranslationResourceListener.class, listener);
  }

  /**
   * parses the content of the input stream for key value translation pairs.
   *
   * @param stream
   * @throws IOException
   *           once a key is used twice.
   */
  protected void parseResource(InputStream stream) throws IOException {
    TranslationResourceEvent multiEvent = new TranslationResourceEvent(this);

    // load items
    Properties prop = new Properties();
    prop.load(stream);
    Set<Entry<Object, Object>> entrySet = prop.entrySet();

    Map<String, String> newEntries = new HashMap<>(entrySet.size());
    for (Entry<Object, Object> entry : entrySet) {
      if (newEntries.get(entry.getKey()) != null) {
        SdkLog.error("Doubled entry for key: " + entry.getKey() + " skipping this entry", new Exception());
      }
      else {
        String key = (String) entry.getKey();
        String translation = (String) entry.getValue();
        if (translation != null) {
          newEntries.put(key, translation);
        }
        String oldEntry = m_entries.remove(key);
        if (oldEntry == null) {
          multiEvent.addEvent(new TranslationResourceEvent(this, key, translation, TranslationResourceEvent.TYPE_ENTRY_ADD));
        }
        else if (translation == null || translation.length() < 1) {
          multiEvent.addEvent(new TranslationResourceEvent(this, key, translation, TranslationResourceEvent.TYPE_ENTRY_REMOVE));
        }
        else if (!oldEntry.equals(translation)) {
          multiEvent.addEvent(new TranslationResourceEvent(this, key, translation, TranslationResourceEvent.TYPE_ENTRY_MODIFY));
        }
      }
    }
    // add all removed
    for (Entry<String, String> entry : m_entries.entrySet()) {
      multiEvent.addEvent(new TranslationResourceEvent(this, entry.getKey(), entry.getValue(), TranslationResourceEvent.TYPE_ENTRY_REMOVE));
    }
    m_entries = newEntries;
    // fire diff
    if (multiEvent.getSubEvents().length == 1) {
      fireTranslationResourceChanged(multiEvent.getSubEvents()[0]);
    }
    else if (multiEvent.getSubEvents().length > 1) {
      fireTranslationResourceChanged(multiEvent);
    }
  }

  protected Map<String, String> getAllTranslations() {
    return m_entries;
  }

  protected void fireTranslationResourceChanged(TranslationResourceEvent event) {
    for (ITranslationResourceListener listener : m_eventListeners.getListeners(ITranslationResourceListener.class)) {
      try {
        listener.translationResourceChanged(event);
      }
      catch (Exception t) {
        SdkLog.error("error during listener notification.", t);
      }
    }
  }

  /**
   * @return true when the resource is the default translation (without a language)
   */
  @Override
  public boolean isDefaultLanguage() {
    return Language.LANGUAGE_DEFAULT.equals(m_language);
  }

  /**
   * @return the language of the underlying resource
   */
  @Override
  public Language getLanguage() {
    return m_language;
  }

  @Override
  public Set<String> getAllKeys() {
    return new HashSet<>(m_entries.keySet());
  }

  protected void setTranslation(String key, String translation, IProgressMonitor monitor) {
    setTranslation(key, translation, true, monitor);
  }

  protected void setTranslation(String key, String translation, boolean fireEvent, IProgressMonitor monitor) {
    TranslationResourceEvent event = null;
    String oldVal = m_entries.get(key);
    if (oldVal == null) {
      if (translation != null) {
        m_entries.put(key, translation);
        event = new TranslationResourceEvent(this, key, translation, TranslationResourceEvent.TYPE_ENTRY_ADD);
      }
    }
    else if (translation == null || "".equals(translation)) {
      m_entries.remove(key);
      event = new TranslationResourceEvent(this, key, translation, TranslationResourceEvent.TYPE_ENTRY_REMOVE);
    }
    else if (!oldVal.equals(translation)) {
      m_entries.put(key, translation);
      event = new TranslationResourceEvent(this, key, translation, TranslationResourceEvent.TYPE_ENTRY_MODIFY);
    }
    if (event != null && fireEvent) {
      commitChanges(monitor);
      fireTranslationResourceChanged(event);
    }
  }

  @Override
  public String getTranslation(String key) {
    return m_entries.get(key);
  }

  /**
   * should be overwritten when a resource is not read only
   */
  @Override
  public void reload(IProgressMonitor monitor) {
    if (!isReadOnly()) {
      SdkLog.warning("reload should be overwritten in writeable resources! " + this.getClass().getName());
    }
  }

  @Override
  public void commitChanges(IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

  @Override
  public void updateText(String key, String text, boolean fireEvent, IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

  @Override
  public IStatus updateKey(String oldKey, String newKey, IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

  @Override
  public IStatus remove(String key, IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }
}
