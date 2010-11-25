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
package org.eclipse.scout.nls.sdk.internal.model.workspace.translationfile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.INlsResource;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFileListener;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.InvalidTranslationFileException;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.TranslationFileEvent;

public abstract class AbstractTranslationFile implements INlsResource, ITranslationFile {

  private HashMap<String/* key */, String/* translation */> m_entries;

  private final Language m_language;

  private EventListenerList m_eventListeners;

  /**
   * creates a new accesser to a *.properties file containing translations
   * 
   * @param language
   * @param language
   *          the languge of the file, depending on _xy.properties
   * @param stram
   *          an input stram of the underlying file
   */
  public AbstractTranslationFile(Language language) {
    m_language = language;
    m_entries = new HashMap<String, String>();
    m_eventListeners = new EventListenerList();
  }

  public void addTranslationFileListener(ITranslationFileListener listener) {
    m_eventListeners.add(ITranslationFileListener.class, listener);
  }

  public void removeTranslationFileListener(ITranslationFileListener listener) {
    m_eventListeners.remove(ITranslationFileListener.class, listener);
  }

  /**
   * parses the content of the input stram for key value translation pairs.
   * 
   * @param stream
   * @throws IOException
   * @throws InvalidTranslationFileException
   *           once a key is used twice.
   */
  protected void parseFile(InputStream stream) throws IOException, InvalidTranslationFileException {
    TranslationFileEvent multiEvent = new TranslationFileEvent(this);
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
          multiEvent.addEvent(new TranslationFileEvent(this, key, translation, TranslationFileEvent.TYPE_ENTRY_ADD));
        }
        else if (oldEntry != null && StringUtility.isNullOrEmpty(translation)) {
          multiEvent.addEvent(new TranslationFileEvent(this, key, translation, TranslationFileEvent.TYPE_ENTRY_REMOVE));
        }
        else if (!oldEntry.equals(translation)) {
          multiEvent.addEvent(new TranslationFileEvent(this, key, translation, TranslationFileEvent.TYPE_ENTRY_MODIFY));
        }
      }
    }
    // add all removed
    for (Entry<String, String> entry : m_entries.entrySet()) {
      multiEvent.addEvent(new TranslationFileEvent(this, entry.getKey(), entry.getValue(), TranslationFileEvent.TYPE_ENTRY_REMOVE));
    }
    m_entries = newEntries;
    // fire diff
    if (multiEvent.getSubEvents().length == 1) {
      fireTranslationFileChanged(multiEvent.getSubEvents()[0]);
    }
    else if (multiEvent.getSubEvents().length > 1) {
      fireTranslationFileChanged(multiEvent);
    }
  }

  protected Map<String, String> getAllTranslations() {
    return m_entries;
  }

  protected void fireTranslationFileChanged(TranslationFileEvent event) {
    for (ITranslationFileListener listener : m_eventListeners.getListeners(ITranslationFileListener.class)) {
      listener.translationFileChanged(event);
    }
  }

  /**
   * @return true when the file is the default translation file (without a languege)
   */
  public boolean isDefaultLanguage() {
    return m_language.equals(Language.LANGUAGE_DEFAULT);
  }

  /**
   * @return the language of the underlying file
   */
  public Language getLanguage() {
    return m_language;
  }

  public String[] getAllKeys() {
    return m_entries.keySet().toArray(new String[m_entries.size()]);
  }

  protected void setTranslation(String key, String translation, IProgressMonitor monitor) {
    setTranslation(key, translation, true, monitor);
  }

  protected void setTranslation(String key, String translation, boolean fireEvent, IProgressMonitor monitor) {
    TranslationFileEvent event = null;
    String oldVal = m_entries.get(key);
    if (oldVal == null) {
      if (translation != null) {
        m_entries.put(key, translation);
        event = new TranslationFileEvent(this, key, translation, TranslationFileEvent.TYPE_ENTRY_ADD);
      }
    }
    else if (translation == null || "".equals(translation)) {
      m_entries.remove(key);
      event = new TranslationFileEvent(this, key, translation, TranslationFileEvent.TYPE_ENTRY_REMOVE);
    }
    else if (!oldVal.equals(translation)) {
      m_entries.put(key, translation);
      event = new TranslationFileEvent(this, key, translation, TranslationFileEvent.TYPE_ENTRY_MODIFY);
    }
    if (event != null && fireEvent) {
      commitChanges(monitor);
      fireTranslationFileChanged(event);
    }

  }

  public String getTranslation(String key) {
    return m_entries.get(key);
  }

  /**
   * should be overwritten when a file is not read only
   */
  public void reload(IProgressMonitor monitor) {
    if (!isReadOnly()) {
      NlsCore.logWarning("reload should be overwritten in writeable files! " + this.getClass().getName());
    }
  }

  public void commitChanges(IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

  public void updateText(String key, String text, IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

  public IStatus updateKey(String oldKey, String newKey, IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

  public IStatus remove(String key, IProgressMonitor monitor) {
    throw new UnsupportedOperationException("this method is not supported on : " + this.getClass().getSimpleName() + " readOnly=" + isReadOnly());
  }

}
