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
package org.eclipse.scout.sdk.s2e.nls.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.event.EventListenerList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.util.OptimisticLock;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.internal.model.InheritedNlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.resource.ITranslationResource;
import org.eclipse.scout.sdk.s2e.nls.resource.ITranslationResourceListener;
import org.eclipse.scout.sdk.s2e.nls.resource.TranslationResourceEvent;

public abstract class AbstractNlsProject implements INlsProject {

  private final EventListenerList m_listeners;
  private final ITranslationResourceListener m_translationResourceListener;
  private final P_ParentListener m_parentProjectListener;
  private final OptimisticLock m_translationResourceEventLock;
  private final NlsResourceProvider m_resourceProvider;
  private final IType m_nlsAccessorType;
  private final ReadWriteLock m_lock;
  private volatile Map<String, NlsEntry> m_entries;
  private Language m_developerLanguage;
  private INlsProject m_parent;

  /**
   * @param type
   *          the class that is used to access nls texts.
   */
  public AbstractNlsProject(IType type) {
    m_parent = null;
    m_listeners = new EventListenerList();
    m_parentProjectListener = new P_ParentListener();
    m_translationResourceListener = new P_TranslationResourceChangedListener();
    m_translationResourceEventLock = new OptimisticLock();
    m_resourceProvider = new NlsResourceProvider();
    m_lock = new ReentrantReadWriteLock();
    m_entries = null;
    m_developerLanguage = null;
    m_nlsAccessorType = type;
  }

  @Override
  public IType getNlsAccessorType() {
    return m_nlsAccessorType;
  }

  @Override
  public String getName() {
    return m_nlsAccessorType.getElementName();
  }

  protected void resetCache() {
    m_lock.writeLock().lock();
    try {
      m_entries = null;
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @Override
  public boolean isReadOnly() {
    for (Language l : getAllLanguages()) {
      ITranslationResource resource = getTranslationResource(l);
      if (resource != null && resource.isReadOnly()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Provides all languages supported by this project and all its ancestors.
   *
   * @return a sorted list of all supported languages
   */
  @Override
  public List<Language> getAllLanguages() {
    return m_resourceProvider.getAllLanguages();
  }

  @Override
  public ITranslationResource getTranslationResource(Language language) {
    return m_resourceProvider.getResource(language);
  }

  public List<ITranslationResource> getAllTranslationResources() {
    return m_resourceProvider.getSortedResources();
  }

  private void cache() {
    if (m_entries == null) {
      m_lock.writeLock().lock();
      try {
        Map<String, NlsEntry> entries = new HashMap<>();

        // inherited entries
        if (getParent() != null) {
          for (String parentKey : getParent().getAllKeys()) {
            entries.put(parentKey, new InheritedNlsEntry(getParent().getEntry(parentKey)));
          }
        }

        // local rows
        for (ITranslationResource r : getAllTranslationResources()) {
          for (String key : r.getAllKeys()) {
            NlsEntry nlsEntry = entries.get(key);
            if (nlsEntry == null) {
              nlsEntry = new NlsEntry(key, this);
              entries.put(key, nlsEntry);
            }

            if (nlsEntry.getType() == INlsEntry.TYPE_INHERITED) {
              // the key exists in a parent project and here -> change the type from inherited to local
              entries.remove(key);
              nlsEntry = new NlsEntry(nlsEntry);
              entries.put(key, nlsEntry);
            }

            nlsEntry.addTranslation(r.getLanguage(), r.getTranslation(key));
          }
        }

        m_entries = entries;
      }
      finally {
        m_lock.writeLock().unlock();
      }
    }
  }

  @Override
  public Set<String> getAllKeys() {
    cache();
    m_lock.readLock().lock();
    try {
      return new HashSet<>(m_entries.keySet());
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public IStatus removeEntries(Collection<INlsEntry> entries, IProgressMonitor m) {
    if (entries == null || entries.isEmpty()) {
      return Status.OK_STATUS;
    }

    for (ITranslationResource r : getAllTranslationResources()) {
      for (INlsEntry e : entries) {
        IStatus status = r.remove(e.getKey(), m);
        if (!status.isOK()) {
          SdkLog.error(status.getMessage(), status.getException());
        }
      }
      r.commitChanges(m);
    }
    refresh();
    return Status.OK_STATUS;
  }

  @Override
  public INlsEntry getEntry(String key) {
    cache();
    m_lock.readLock().lock();
    try {
      return m_entries.get(key);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public List<INlsEntry> getEntries(String prefix, boolean caseSensitive) {
    cache();

    m_lock.readLock().lock();
    try {
      if (prefix != null && prefix.length() > 0) {
        String compareablePrefix = prefix;
        if (!caseSensitive) {
          compareablePrefix = compareablePrefix.toLowerCase();
        }
        Set<Entry<String, NlsEntry>> keySet = m_entries.entrySet();
        List<INlsEntry> entries = new ArrayList<>(keySet.size());
        for (Entry<String, NlsEntry> entry : keySet) {
          String compareKey = entry.getKey();
          if (!caseSensitive) {
            compareKey = compareKey.toLowerCase();
          }
          if (compareKey.startsWith(compareablePrefix)) {
            entries.add(entry.getValue());
          }
        }
        return entries;
      }
      List<INlsEntry> result = new ArrayList<>(m_entries.size());
      for (INlsEntry e : m_entries.values()) {
        result.add(e);
      }
      return result;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public List<INlsEntry> getAllEntries() {
    cache();
    m_lock.readLock().lock();
    try {
      List<INlsEntry> result = new ArrayList<>(m_entries.size());
      for (INlsEntry e : m_entries.values()) {
        result.add(e);
      }
      return result;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * Returns the parent NlsProject or null if no parent has been defined.
   *
   * @return
   */
  @Override
  public INlsProject getParent() {
    return m_parent;
  }

  @Override
  public String generateKey(String baseText) {
    return generateKey(baseText, false);
  }

  @Override
  public String generateNewKey(String baseText) {
    return generateKey(baseText, true);
  }

  /**
   * The output of this method must fulfill the regex for key-validation defined in
   * org.eclipse.scout.nls.sdk.ui.InputValidator#REGEX_NLS_KEY_NAME
   */
  protected String generateKey(String baseText, boolean appendFreeNumSuffix) {
    cache();
    if (baseText == null || baseText.length() < 1) {
      return null;
    }

    StringBuilder ret = new StringBuilder(baseText.length());

    // remove not allowed characters
    baseText = baseText.replaceAll("[^a-zA-Z0-9_\\.\\- ]*", "").trim();

    // camel case multiple words
    String[] split = baseText.split(" ");
    for (int i = 0; i < split.length; i++) {
      String splitValue = split[i];
      if (splitValue.length() > 0) {
        char first = splitValue.charAt(0);
        if (split.length > 1) {
          first = Character.toUpperCase(first);
        }
        ret.append(first);
        if (splitValue.length() > 1) {
          ret.append(splitValue.substring(1));
        }
      }
    }

    // remove not allowed characters from the start
    while (ret.length() > 0 && (ret.charAt(0) == '.' || ret.charAt(0) == '_' || ret.charAt(0) == '-')) {
      ret.deleteCharAt(0);
    }

    // remove not allowed characters from the end
    while (ret.length() > 0 && (ret.charAt(ret.length() - 1) == '.' || ret.charAt(ret.length() - 1) == '-')) {
      ret.deleteCharAt(ret.length() - 1);
    }

    // ensure max length
    int maxLength = 190;
    String newKey;
    if (ret.length() > maxLength) {
      newKey = ret.substring(0, maxLength);
    }
    else {
      newKey = ret.toString();
    }

    // add unique ending number if requested
    String result = newKey;
    if (appendFreeNumSuffix) {
      int i = 0;
      m_lock.readLock().lock();
      try {
        while (m_entries.containsKey(result)) {
          result = newKey + i++;
        }
      }
      finally {
        m_lock.readLock().unlock();
      }
    }
    return result;
  }

  protected void setParent(INlsProject newParent) {
    if (m_parent != null) {
      m_parent.removeProjectListener(m_parentProjectListener);
    }
    m_parent = newParent;
    m_parent.addProjectListener(m_parentProjectListener);
  }

  @Override
  public Language getDevelopmentLanguage() {
    if (m_developerLanguage == null) {
      Language lang = new Language(Locale.getDefault());
      if (!containsLanguage(lang)) {
        lang = new Language(new Locale(lang.getLocale().getLanguage(), lang.getLocale().getCountry()));
        if (!containsLanguage(lang)) {
          lang = new Language(new Locale(lang.getLocale().getLanguage()));
          if (!containsLanguage(lang)) {
            lang = Language.LANGUAGE_DEFAULT;
          }
        }
      }
      m_developerLanguage = lang;
    }
    return m_developerLanguage;
  }

  @Override
  public void updateKey(INlsEntry entry, String newKey, IProgressMonitor monitor) {
    cache();
    NlsProjectEvent multiEvent = new NlsProjectEvent(this);
    m_lock.writeLock().lock();
    try {
      m_translationResourceEventLock.acquire();
      monitor.beginTask("update Key", IProgressMonitor.UNKNOWN);
      NlsEntry originalEntry = m_entries.remove(entry.getKey());
      if (originalEntry == null) {
        SdkLog.error("The nls entry with the key '" + entry.getKey() + "' can not be found");
        return;
      }
      else if (originalEntry.getType() == INlsEntry.TYPE_INHERITED) {
        SdkLog.error("The inherited NLS entry '" + originalEntry.getKey() + "' can not be modified");
        return;
      }
      else {
        multiEvent.addChildEvent(new NlsProjectEvent(this, originalEntry, NlsProjectEvent.TYPE_ENTRY_REMOVED));
        for (Entry<Language, String> e : entry.getAllTranslations().entrySet()) {
          ITranslationResource r = getTranslationResource(e.getKey());
          if (r != null) {
            r.updateKey(entry.getKey(), newKey, monitor);
          }
        }
        NlsEntry newEntry = new NlsEntry(originalEntry);
        newEntry.setKey(newKey);
        m_entries.put(newEntry.getKey(), newEntry);
        multiEvent.addChildEvent(new NlsProjectEvent(this, newEntry, NlsProjectEvent.TYPE_ENTRY_ADDED));
      }
    }
    finally {
      m_lock.writeLock().unlock();
      m_translationResourceEventLock.release();
    }
  }

  @Override
  public void updateRow(INlsEntry row, IProgressMonitor monitor) {
    updateRow(row, true, monitor);
  }

  @Override
  public void flush(IProgressMonitor monitor) {
    for (ITranslationResource r : m_resourceProvider.getResources()) {
      r.commitChanges(monitor);
    }
  }

  @Override
  public void updateRow(INlsEntry row, boolean flush, IProgressMonitor monitor) {
    String key = row.getKey();
    if (key == null || key.length() < 1) {
      throw new IllegalArgumentException("a text key cannot be null.");
    }
    cache();
    m_lock.writeLock().lock();
    try {
      m_translationResourceEventLock.acquire();
      NlsEntry existingEntry = m_entries.get(key);
      if (existingEntry != null) {
        if (existingEntry.getType() == INlsEntry.TYPE_INHERITED) {
          createNewRowInternal(row, flush, monitor); // override
        }
        else {
          updateExistingRowInternal(existingEntry, row, flush, monitor);
        }
      }
      else {
        createNewRowInternal(row, flush, monitor);
      }
    }
    finally {
      m_lock.writeLock().unlock();
      m_translationResourceEventLock.release();
    }
  }

  private void createNewRowInternal(INlsEntry row, boolean flush, IProgressMonitor monitor) {
    NlsEntry newRow = new NlsEntry(row);
    m_entries.put(newRow.getKey(), newRow);
    for (Entry<Language, String> entry : newRow.getAllTranslations().entrySet()) {
      ITranslationResource r = getTranslationResource(entry.getKey());
      if (r != null) {
        r.updateText(newRow.getKey(), entry.getValue(), flush, monitor);
      }
    }
    fireNlsProjectEvent(new NlsProjectEvent(this, newRow, NlsProjectEvent.TYPE_ENTRY_ADDED));
  }

  private void updateExistingRowInternal(NlsEntry existingRow, INlsEntry row, boolean flush, IProgressMonitor monitor) {
    boolean updated = false;
    for (Entry<Language, String> entry : row.getAllTranslations().entrySet()) {
      String existingTranslation = existingRow.getTranslation(entry.getKey());
      if (!Objects.equals(existingTranslation, entry.getValue())) {
        ITranslationResource r = getTranslationResource(entry.getKey());
        if (r != null) {
          updated = true;
          r.updateText(row.getKey(), entry.getValue(), flush, monitor);
          existingRow.addTranslation(entry.getKey(), entry.getValue());
        }
      }
    }
    if (updated) {
      fireNlsProjectEvent(new NlsProjectEvent(this, existingRow, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
    }
  }

  private void handleParentRowAdded(INlsEntry superRow) {
    if (m_entries != null) {
      m_lock.writeLock().lock();
      try {
        NlsEntry entry = m_entries.get(superRow.getKey());
        if (entry == null || entry.getType() == INlsEntry.TYPE_INHERITED) {
          entry = new InheritedNlsEntry(superRow);
          m_entries.put(entry.getKey(), entry);
          fireNlsProjectEvent(new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_ADDED));
        }
      }
      finally {
        m_lock.writeLock().unlock();
      }
    }
  }

  private void handleParentRowRemoved(INlsEntry superRow) {
    if (m_entries != null) {
      m_lock.writeLock().lock();
      try {
        NlsEntry existing = m_entries.get(superRow.getKey());
        if (existing.getType() == INlsEntry.TYPE_INHERITED) {
          NlsEntry removedEntry = m_entries.remove(superRow.getKey());
          if (removedEntry != null) {
            fireNlsProjectEvent(new NlsProjectEvent(this, removedEntry, NlsProjectEvent.TYPE_ENTRY_REMOVED));
          }
        }
      }
      finally {
        m_lock.writeLock().unlock();
      }
    }
  }

  private void handleParentRowModified(INlsEntry superRow) {
    if (m_entries != null) {
      m_lock.writeLock().lock();
      try {
        NlsEntry entry = m_entries.get(superRow.getKey());
        if (entry == null) {
          SdkLog.error("NLS entry with key:'" + superRow.getKey() + "' not found");
          return;
        }
        if (entry.getType() == INlsEntry.TYPE_INHERITED) {
          entry.update(superRow);
          fireNlsProjectEvent(new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
        }
      }
      finally {
        m_lock.writeLock().unlock();
      }
    }
  }

  protected abstract List<ITranslationResource> loadTranslationResources() throws CoreException;

  protected void updateTranslationResourceLocation() {
    try {
      List<ITranslationResource> translationResources = loadTranslationResources();
      for (ITranslationResource r : translationResources) {
        if (r.getLanguage() != null) {
          addTranslationResource(r);
        }
      }
    }
    catch (CoreException e) {
      SdkLog.error("could not load tranlstion property resources.", e);
    }
  }

  protected void addTranslationResource(ITranslationResource r) {
    r.getLanguage().setLocal(true);
    r.addTranslationResourceListener(m_translationResourceListener);
    m_resourceProvider.addResource(r);
    resetCache();
    fireNlsProjectEvent(new NlsProjectEvent(this, r, NlsProjectEvent.TYPE_TRANSLATION_RESOURCE_ADDED));
  }

  @Override
  public void refresh() {
    resetCache();
    for (ITranslationResource r : m_resourceProvider.getResources()) {
      r.removeTranslationResourceListener(m_translationResourceListener);
      m_resourceProvider.remove(r);
    }
    updateTranslationResourceLocation();
    fireNlsProjectEvent(new NlsProjectEvent(this, NlsProjectEvent.TYPE_FULL_REFRESH));
  }

  @Override
  public Language getBestMatchingProjectLanguage(Language language) {
    if (containsLanguage(language)) {
      return language;
    }
    Locale locale = new Locale(language.getLocale().getLanguage(), language.getLocale().getCountry());
    Language bestMatch = new Language(locale);
    if (containsLanguage(bestMatch)) {
      return bestMatch;
    }
    locale = new Locale(language.getLocale().getLanguage());
    bestMatch = new Language(locale);
    if (containsLanguage(bestMatch)) {
      return bestMatch;
    }
    return Language.LANGUAGE_DEFAULT;

  }

  @Override
  public boolean containsLanguage(Language languge) {
    return m_resourceProvider.containsResource(languge);
  }

  @Override
  public void addProjectListener(INlsProjectListener listener) {
    m_listeners.add(INlsProjectListener.class, listener);
  }

  @Override
  public void removeProjectListener(INlsProjectListener listener) {
    m_listeners.remove(INlsProjectListener.class, listener);
  }

  protected void fireNlsProjectEvent(NlsProjectEvent event) {
    for (INlsProjectListener listener : m_listeners.getListeners(INlsProjectListener.class)) {
      try {
        listener.notifyProjectChanged(event);
      }
      catch (Exception t) {
        SdkLog.error("error during listener notification.", t);
      }
    }
  }

  private void handleTranlationResourceChanged(TranslationResourceEvent event) {
    try {
      if (m_translationResourceEventLock.acquire()) {
        Map<INlsEntry, NlsProjectEvent> addEvents = new HashMap<>();
        Map<INlsEntry, NlsProjectEvent> modifyEvents = new HashMap<>();
        Map<INlsEntry, NlsProjectEvent> removeEvents = new HashMap<>();
        handleTranslationResourceChangedRec(addEvents, modifyEvents, removeEvents, event);
        // fire
        NlsProjectEvent multiEvent = new NlsProjectEvent(this);
        for (NlsProjectEvent e : addEvents.values()) {
          multiEvent.addChildEvent(e);
        }
        for (NlsProjectEvent e : removeEvents.values()) {
          multiEvent.addChildEvent(e);
        }
        for (NlsProjectEvent e : modifyEvents.values()) {
          multiEvent.addChildEvent(e);
        }
        if (multiEvent.getChildEvents().length == 1) {
          fireNlsProjectEvent(multiEvent.getChildEvents()[0]);
        }
        else if (multiEvent.getChildEvents().length > 1) {
          fireNlsProjectEvent(multiEvent);
        }
      }
    }
    finally {
      m_translationResourceEventLock.release();
    }
  }

  private void handleTranslationResourceChangedRec(Map<INlsEntry, NlsProjectEvent> addEvents, Map<INlsEntry, NlsProjectEvent> modifyEvents, Map<INlsEntry, NlsProjectEvent> removeEvents,
      TranslationResourceEvent event) {
    if (event.isMulti()) {
      for (TranslationResourceEvent e : event.getSubEvents()) {
        handleTranslationResourceChangedRec(addEvents, modifyEvents, removeEvents, e);
      }
    }
    else {
      String key = event.getKey();
      String translation = event.getTranslation();
      cache();
      NlsEntry entry = m_entries.get(key);
      switch (event.getType()) {
        case TranslationResourceEvent.TYPE_ENTRY_ADD:
          if (entry == null) {
            m_lock.writeLock().lock();
            try {
              entry = new NlsEntry(key, this);
              entry.addTranslation(event.getSource().getLanguage(), translation);
              m_entries.put(entry.getKey(), entry);
            }
            finally {
              m_lock.writeLock().unlock();
            }

            // always an event
            addEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_ADDED));
          }
          else {
            modifyEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
          }
          break;
        case TranslationResourceEvent.TYPE_ENTRY_REMOVE:
          if (entry != null && entry.getType() == INlsEntry.TYPE_LOCAL) {
            entry.addTranslation(event.getSource().getLanguage(), null);

            boolean isEmpty = true;
            for (ITranslationResource r : getAllTranslationResources()) {
              if (r.getTranslation(entry.getKey()) != null) {
                // the current resources still contain values for that key.
                isEmpty = false;
                break;
              }
            }

            if (isEmpty) {
              m_lock.writeLock().lock();
              try {
                // remove
                m_entries.remove(entry.getKey());
                if (m_parent != null) {
                  INlsEntry e = m_parent.getEntry(entry.getKey());
                  if (e != null) {
                    m_entries.put(entry.getKey(), new InheritedNlsEntry(e));
                  }
                }
              }
              finally {
                m_lock.writeLock().unlock();
              }
              removeEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_REMOVED));
              modifyEvents.remove(entry);
            }
            else {
              // modify
              modifyEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
            }
          }
          break;
        case TranslationResourceEvent.TYPE_ENTRY_MODIFY:
          if (entry != null) {
            if (entry.getType() == INlsEntry.TYPE_LOCAL) {
              entry.addTranslation(event.getSource().getLanguage(), translation);
            }
            modifyEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
          }
          break;
        case TranslationResourceEvent.TYPE_ENTRY_REMOVED:
          refresh();
          break;
        default:
          break;
      }
    }
  }

  /**
   * listens to changes of the parent project
   */
  private class P_ParentListener implements INlsProjectListener {
    @Override
    public void notifyProjectChanged(NlsProjectEvent event) {
      if (event.isMultiEvent()) {
        for (NlsProjectEvent subEvent : event.getChildEvents()) {
          notifyProjectChanged(subEvent);
        }
      }
      else {
        switch (event.getType()) {
          case NlsProjectEvent.TYPE_ENTRY_ADDED: {
            handleParentRowAdded(event.getEntry());
            break;
          }
          case NlsProjectEvent.TYPE_ENTRY_REMOVED: {
            handleParentRowRemoved(event.getEntry());
            break;
          }
          case NlsProjectEvent.TYPE_ENTRY_MODIFYED: {
            handleParentRowModified(event.getEntry());
            break;
          }
          case NlsProjectEvent.TYPE_TRANSLATION_RESOURCE_ADDED: {
            break;
          }
        }
      }
    }
  } // end class P_ParentListener

  private class P_TranslationResourceChangedListener implements ITranslationResourceListener {
    @Override
    public void translationResourceChanged(TranslationResourceEvent event) {
      handleTranlationResourceChanged(event);
    }
  } // end class P_TranslationResourceChangedListener
}
