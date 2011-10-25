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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.workspace.InheritedNlsEntry;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.NlsStatusDialog;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.ITranslationResource;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.ITranslationResourceListener;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.TranslationResourceEvent;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractNlsProject implements INlsProject {

  private final EventListenerList m_listeners;
  private final ITranslationResourceListener m_translationResourceListener;
  private final OptimisticLock m_translationResourceEventLock;
  private final NlsResourceProvider m_resourceProvider;
  private final IType m_nlsAccessorType;

  private Map<String, NlsEntry> m_entries;
  private Language m_developerLanguage;
  private INlsProject m_parent;

  /**
   * @param type
   *          the class that is used to access nls texts.
   */
  public AbstractNlsProject(IType type) {
    m_parent = null;
    m_listeners = new EventListenerList();
    m_translationResourceListener = new P_TranslationResourceChangedListener();
    m_translationResourceEventLock = new OptimisticLock();
    m_resourceProvider = new NlsResourceProvider();
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
    m_entries = null;
  }

  /**
   * Provides all languages supported by this project and all its ancestors.
   * 
   * @return a sorted list of all supported languages
   */
  @Override
  public Language[] getAllLanguages() {
    return m_resourceProvider.getAllLanguages();
  }

  public ITranslationResource getTranslationResource(Language language) {
    return m_resourceProvider.getResource(language);
  }

  public ITranslationResource[] getAllTranslationResources() {
    return m_resourceProvider.getSortedResources();
  }

  private void cache() {
    if (m_entries == null) {
      m_entries = new HashMap<String, NlsEntry>();

      // inherited entries
      if (getParent() != null) {
        for (String parentKey : getParent().getAllKeys()) {
          m_entries.put(parentKey, new InheritedNlsEntry(getParent().getEntry(parentKey), this));
        }
      }

      // local rows
      for (ITranslationResource r : getAllTranslationResources()) {
        for (String key : r.getAllKeys()) {
          NlsEntry nlsEntry = m_entries.get(key);
          if (nlsEntry == null) {
            nlsEntry = new NlsEntry(key, this);
            m_entries.put(key, nlsEntry);
          }
          if (nlsEntry.getType() == INlsEntry.TYPE_LOCAL) {
            nlsEntry.addTranslation(r.getLanguage(), r.getTranslation(key));
          }
        }
      }
    }
  }

  @Override
  public String[] getAllKeys() {
    cache();
    return m_entries.keySet().toArray(new String[m_entries.size()]);
  }

  @Override
  public IStatus removeEntries(INlsEntry[] entries) {
    NullProgressMonitor m = new NullProgressMonitor();
    for (ITranslationResource r : getAllTranslationResources()) {
      for (INlsEntry e : entries) {
        IStatus status = r.remove(e.getKey(), m);
        if (!status.isOK()) {
          new NlsStatusDialog(Display.getDefault().getActiveShell(), status).open();
          return status;
        }
      }
    }
    return Status.OK_STATUS;
  }

  @Override
  public INlsEntry getEntry(String key) {
    cache();
    return m_entries.get(key);
  }

  @Override
  public INlsEntry[] getEntries(String prefix, boolean caseSensitive) {
    cache();

    if (StringUtility.hasText(prefix)) {
      String compareablePrefix = prefix;
      if (!caseSensitive) {
        compareablePrefix = compareablePrefix.toLowerCase();
      }
      ArrayList<INlsEntry> entries = new ArrayList<INlsEntry>();
      for (String key : m_entries.keySet()) {
        String compareKey = key;
        if (!caseSensitive) {
          compareKey = compareKey.toLowerCase();
        }
        if (compareKey.startsWith(compareablePrefix)) {
          entries.add(m_entries.get(key));
        }
      }
      return entries.toArray(new INlsEntry[entries.size()]);
    }
    else {
      return m_entries.values().toArray(new INlsEntry[m_entries.values().size()]);
    }
  }

  @Override
  public INlsEntry[] getAllEntries() {
    cache();
    return m_entries.values().toArray(new INlsEntry[m_entries.size()]);
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

  protected void setParent(INlsProject newParent) {
    m_parent = newParent;
    m_parent.addProjectListener(new P_ParentListener());
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
    if (m_entries != null) {
      NlsProjectEvent multiEvent = new NlsProjectEvent(this);
      try {
        m_translationResourceEventLock.acquire();
        monitor.beginTask("update Key", IProgressMonitor.UNKNOWN);
        NlsEntry originalEntry = m_entries.remove(entry.getKey());
        if (originalEntry == null) {
          NlsCore.logError("The nls entry with the key '" + entry.getKey() + "' can not be found");
          return;
        }
        else if (originalEntry.getType() == INlsEntry.TYPE_INHERITED) {
          NlsCore.logError("The inherited NLS entry '" + originalEntry.getKey() + "' can not be modified");
          return;
        }
        else {
          multiEvent.addChildEvent(new NlsProjectEvent(this, originalEntry, NlsProjectEvent.TYPE_ENTRY_REMOVEED));
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
        m_translationResourceEventLock.release();
      }
    }
  }

  @Override
  public void updateRow(INlsEntry row, IProgressMonitor monitor) {
    if (m_entries != null) {
      try {
        m_translationResourceEventLock.acquire();
        NlsEntry existingEntry = m_entries.get(row.getKey());
        if (existingEntry != null) {
          if (existingEntry.getType() == INlsEntry.TYPE_INHERITED) {
            NlsCore.logError("The inherited NLS entry '" + existingEntry.getKey() + "' can not be modified", new Exception());
            return;
          }
          else {
            updateExistingRowInternal(existingEntry, row, monitor);
          }
        }
        else {
          createNewRowInternal(row, monitor);
        }
      }
      finally {
        m_translationResourceEventLock.release();
      }
    }
  }

  private void createNewRowInternal(INlsEntry row, IProgressMonitor monitor) {
    NlsEntry newRow = new NlsEntry(row);
    m_entries.put(newRow.getKey(), newRow);
    for (Entry<Language, String> entry : newRow.getAllTranslations().entrySet()) {
      ITranslationResource r = getTranslationResource(entry.getKey());
      if (r != null) {
        r.updateText(newRow.getKey(), entry.getValue(), monitor);
      }
    }
    fireNlsProjectEvent(new NlsProjectEvent(this, newRow, NlsProjectEvent.TYPE_ENTRY_ADDED));
  }

  private void updateExistingRowInternal(NlsEntry existingRow, INlsEntry row, IProgressMonitor monitor) {
    boolean updated = false;
    for (Entry<Language, String> entry : row.getAllTranslations().entrySet()) {
      String existingTranslation = existingRow.getTranslation(entry.getKey());
      if (CompareUtility.notEquals(existingTranslation, entry.getValue())) {
        ITranslationResource r = getTranslationResource(entry.getKey());
        if (r != null) {
          updated = true;
          r.updateText(row.getKey(), entry.getValue(), monitor);
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
      NlsEntry entry = m_entries.get(superRow.getKey());
      if (entry != null) {
        NlsCore.logError("NLS entry with key:'" + superRow.getKey() + "' already exists");
      }
      else {
        entry = new InheritedNlsEntry(superRow, this);
        m_entries.put(entry.getKey(), entry);
        fireNlsProjectEvent(new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_ADDED));
      }
    }
  }

  private void handleParentRowRemoved(INlsEntry superRow) {
    if (m_entries != null) {
      NlsEntry removedEntry = m_entries.remove(superRow.getKey());
      if (removedEntry != null) {
        fireNlsProjectEvent(new NlsProjectEvent(this, removedEntry, NlsProjectEvent.TYPE_ENTRY_REMOVEED));
      }
    }
  }

  private void handleParentRowModified(INlsEntry superRow) {
    if (m_entries != null) {
      NlsEntry entry = m_entries.get(superRow.getKey());
      if (entry == null) {
        NlsCore.logError("NLS entry with key:'" + superRow.getKey() + "' not found");
        return;
      }
      entry.update(superRow);
      fireNlsProjectEvent(new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
    }
  }

  protected abstract ITranslationResource[] loadTranslationResources() throws CoreException;

  protected void updateTranslationResourceLocation() {
    try {
      ITranslationResource[] translationResources = loadTranslationResources();
      for (ITranslationResource r : translationResources) {
        addTranslationResource(r, new NullProgressMonitor());
      }
    }
    catch (CoreException e) {
      NlsCore.logError("could not load tranlstion property resources.", e);
    }
  }

  protected void addTranslationResource(ITranslationResource r, IProgressMonitor monitor) {
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
      catch (Exception e) {
        NlsCore.logWarning(e);
      }
    }
  }

  private void handleTranlationResourceChanged(TranslationResourceEvent event) {
    try {
      if (m_translationResourceEventLock.acquire()) {
        Map<INlsEntry, NlsProjectEvent> addEvents = new HashMap<INlsEntry, NlsProjectEvent>();
        Map<INlsEntry, NlsProjectEvent> modifyEvents = new HashMap<INlsEntry, NlsProjectEvent>();
        Map<INlsEntry, NlsProjectEvent> removeEvents = new HashMap<INlsEntry, NlsProjectEvent>();
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
            entry = new NlsEntry(key, this);
            entry.addTranslation(event.getSource().getLanguage(), translation);
            m_entries.put(entry.getKey(), entry);
            // always an event
            addEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_ADDED));
          }
          else {
            NlsCore.logWarning("translation resource fired an add event of the existing NLS entry:'" + entry.getKey() + "'");
          }
          break;
        case TranslationResourceEvent.TYPE_ENTRY_REMOVE:
          if (entry != null) {
            entry.addTranslation(event.getSource().getLanguage(), null);
            if (entry.isEmpty()) {
              // remove
              m_entries.remove(entry.getKey());
              removeEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_REMOVEED));
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
            entry.addTranslation(event.getSource().getLanguage(), translation);
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
          case NlsProjectEvent.TYPE_ENTRY_REMOVEED: {
            handleParentRowRemoved(event.getEntry());
            break;
          }
          case NlsProjectEvent.TYPE_ENTRY_MODIFYED: {
            handleParentRowModified(event.getEntry());
            break;
          }
          case NlsProjectEvent.TYPE_TRANSLATION_RESOURCE_ADDED: {
            // TODO
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
