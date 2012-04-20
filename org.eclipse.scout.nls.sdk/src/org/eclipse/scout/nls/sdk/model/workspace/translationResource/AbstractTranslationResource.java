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
package org.eclipse.scout.nls.sdk.model.workspace.translationResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.util.Language;

public abstract class AbstractTranslationResource implements ITranslationResource {

  private HashMap<String/* key */, String/* translation */> m_entries;

  private final Language m_language;

  private EventListenerList m_eventListeners;

  /**
   * the language of the resource
   * 
   * @param language
   */
  public AbstractTranslationResource(Language language) {
    m_language = language;
    m_entries = new HashMap<String, String>();
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
   * @throws InvalidTranslationResourceException
   *           once a key is used twice.
   */
  protected void parseResource(InputStream stream) throws IOException, InvalidTranslationResourceException {
    TranslationResourceEvent multiEvent = new TranslationResourceEvent(this);
    HashMap<String, String> newEntries = new HashMap<String, String>();

    Properties prop = new Properties();
    prop.load(stream);
    for (Entry<Object, Object> entry : prop.entrySet()) {
      if (newEntries.get(entry.getKey()) != null) {
        NlsCore.getDefault().getLog().log(new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, "Doubled entry for key: " + entry.getKey() + " skipping this entry", new Exception()));
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
        else if (oldEntry != null && StringUtility.isNullOrEmpty(translation)) {
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
      catch (Throwable t) {
        NlsCore.logError("error during listener notification.", t);
      }
    }
  }

  /**
   * @return true when the resource is the default translation (without a language)
   */
  @Override
  public boolean isDefaultLanguage() {
    return m_language.equals(Language.LANGUAGE_DEFAULT);
  }

  /**
   * @return the language of the underlying resource
   */
  @Override
  public Language getLanguage() {
    return m_language;
  }

  @Override
  public String[] getAllKeys() {
    return m_entries.keySet().toArray(new String[m_entries.size()]);
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
      NlsCore.logWarning("reload should be overwritten in writeable resources! " + this.getClass().getName());
    }
  }

  @Override
  public void commitChanges(IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

  @Override
  public void updateText(String key, String text, IProgressMonitor monitor) {
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
