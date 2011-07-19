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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeUniverseSet;
import org.eclipse.osgi.util.NLS;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.model.workspace.InheritedNlsEntry;
import org.eclipse.scout.nls.sdk.internal.model.workspace.NlsType;
import org.eclipse.scout.nls.sdk.internal.model.workspace.nlsfile.AbstractNlsFile;
import org.eclipse.scout.nls.sdk.internal.model.workspace.translationfile.AbstractTranslationFile;
import org.eclipse.scout.nls.sdk.internal.model.workspace.translationfile.WorkspaceTranslationFile;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProjectListener;
import org.eclipse.scout.nls.sdk.model.workspace.project.NlsProjectEvent;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFileListener;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.TranslationFileEvent;

/**
 * The <b>NlsProject</b> is the basis class of an nls support in a plugin. Each Plugin may have more than one
 * NlsProject (*.nls file). In case of dynamic NLS support a NlsProject may have a parent and each translated text is
 * inherited by the child project or may be overwritten. In addition a NlsProject defines additional translations. A
 * Translation is defined in a class extends {@link AbstractDynamicNls} or {@link NLS} as a static variable. All
 * translations are kept in resource bundles. Use the NLS support creation wizard to generate a NLS support in a plugin.
 * Furthermore all translations should be edited in the NLS Editor (edit the .nls file).
 *
 * @see AbstractNlsFile
 * @see AbstractTranslationFile
 */
public class NlsProject implements INlsProject {

  private NlsProject m_parent;
  private EventListenerList m_listeners;
  private ITranslationFileListener m_translationFileListener;
  private OptimisticLock m_translationFileEventLock = new OptimisticLock();

  private NlsFileProvider m_fileProvider = new NlsFileProvider();
  // nls class
  private NlsType m_nlsClass;
  private PropertyChangeListener m_nlsClassPropertyListener;

  private Map<String, NlsEntry> m_entries;

  private String m_hostPluginId;
  private Language m_developerLanguage;

  /**
   * @param type
   * @param languages
   *          used for parent platform project. use null to find all files
   * @param hostPluginId
   */
  public NlsProject(IType type, String hostPluginId) {
    m_hostPluginId = hostPluginId;
    m_listeners = new EventListenerList();
    m_translationFileListener = new P_TranslationFileChangedListener();
    m_nlsClassPropertyListener = new P_NlsClassPropertyChangeListener();
    // NLS class is immutable
    if (type == null || !type.exists()) {
      NlsCore.logError("NLS type does not exist!", new Exception());
    }
    else {
      m_nlsClass = new NlsType(type);
      m_nlsClass.addPropertyChangeListener(m_nlsClassPropertyListener);
      updateTranslationFileLocation();
      setSuperType(m_nlsClass.getSuperType());
    }

    // new NlsMarkerBuilder(this);
  }

  public IType[] getReferenceTypes(){
    ArrayList<IType> referenceeTypes= new ArrayList<IType>();
    if(m_nlsClass != null){
      referenceeTypes.add(m_nlsClass.getType());
      referenceeTypes.addAll(Arrays.asList(m_nlsClass.getAllSuperclasses()));
    }
    return referenceeTypes.toArray(new IType [referenceeTypes.size()]);
  }

  protected void updateTranslationFileLocation() {
    try {
      if (m_nlsClass != null && m_nlsClass.getType() != null && m_nlsClass.getType().exists()) {
        ITranslationFile[] translationFiles = NlsCore.getNlsWorkspace().loadTranslationFiles(m_nlsClass, new NullProgressMonitor());
        for (ITranslationFile file : translationFiles) {
          addTranslationFile(file, new NullProgressMonitor());
        }
      }
    }
    catch (CoreException e) {
      NlsCore.logError("could not load tranlstion property files of project: '" + getName() + "'", e);
    }
  }

  protected void setSuperType(IType superType) {
    // find parent
    if (superType != null) {
      try {
        m_parent = NlsCore.getNlsWorkspace().findNlsProject(superType, new NullProgressMonitor());
      }
      catch (CoreException e) {
        NlsCore.logError("parent of NLS project '" + getName() + "' could not be found. Looked for type '" + superType.getFullyQualifiedName() + "'");
      }
    }
  }

  public void fullRefresh(boolean reloadFiles) {
    resetCache();
    if (reloadFiles) {
      for (ITranslationFile file : m_fileProvider.getFiles()) {
        file.removeTranslationFileListener(m_translationFileListener);
        m_fileProvider.remove(file);
      }
      updateTranslationFileLocation();
      fireNlsProjectEvent(new NlsProjectEvent(this, NlsProjectEvent.TYPE_FULL_REFRESH));
    }
    else {
      fireNlsProjectEvent(new NlsProjectEvent(this, NlsProjectEvent.TYPE_REFRESH));
    }
  }

  public ITranslationFile[] getTranslationFiles() {
    return m_fileProvider.getFiles();
  }

  public String getHostPluginId() {
    return m_hostPluginId;
  }

  /**
   * @return the filename of the underlaying *.nls file.
   */
  public String getName() {
    return m_hostPluginId;
  }

  public void setClass(NlsType clazz, IProgressMonitor monitor) {
    m_nlsClass = clazz;
  }

  public String getFullyQuallifiedNlsClassName() {
    return m_nlsClass.getType().getFullyQualifiedName();
  }

  public NlsType getNlsType() {
    return m_nlsClass;
  }

  public void addTranslationFile(ITranslationFile file, IProgressMonitor monitor) {
    file.getLanguage().setLocal(true);
    file.addTranslationFileListener(m_translationFileListener);
    m_fileProvider.addFile(file);
    // reset cache
    resetCache();
    fireNlsProjectEvent(new NlsProjectEvent(this, file, NlsProjectEvent.TYPE_TRANSLATION_FILE_ADDED));
  }

  public void removeTranslationFile(ITranslationFile file, IProgressMonitor monitor) {
    m_fileProvider.remove(file);
    file.removeTranslationFileListener(m_translationFileListener);
    resetCache();
    fireNlsProjectEvent(new NlsProjectEvent(this, file, NlsProjectEvent.TYPE_TRANSLATION_FILE_REMOVED));
  }

  private void resetCache() {
    m_entries = null;
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
      for (ITranslationFile file : getAllTranslationFiles()) {
        for (String key : file.getAllKeys()) {
          NlsEntry nlsEntry = m_entries.get(key);
          if (nlsEntry == null) {
            nlsEntry = new NlsEntry(key, this);
            m_entries.put(key, nlsEntry);
          }
          if (nlsEntry.getType() == INlsEntry.TYPE_LOCAL) {
            nlsEntry.addTranslation(file.getLanguage(), file.getTranslation(key));
          }
          else {
            NlsCore.logError("NLS key: '" + nlsEntry.getKey() + "' is inherited in the project: '" + getJavaProject().getElementName() + "' and can not be overwritten");
          }
        }
      }
    }
  }

  public ITranslationFile[] getAllTranslationFiles() {
    return m_fileProvider.getSortedFiles();
  }

  // commodity
  public IJavaProject getJavaProject() {
    return m_nlsClass.getJavaProject();
  }

  public IProject getProject() {
    return m_nlsClass.getJavaProject().getProject();
  }

  public ITranslationFile getTranslationFile(Language language) {
    return m_fileProvider.getFile(language);
  }

  public String[] getAllKeys() {
    cache();
    return m_entries.keySet().toArray(new String[m_entries.size()]);
  }

  public INlsEntry getEntry(String key) {
    cache();
    return m_entries.get(key);
  }

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

  public INlsEntry[] getAllEntries() {
    cache();
    return m_entries.values().toArray(new INlsEntry[m_entries.size()]);
  }

  /**
   * Returns the parent NlsProject or null if no parent has been defined.
   *
   * @return
   */
  public NlsProject getParent() {
    return m_parent;
  }

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

  public void setParent(NlsProject parent, IProgressMonitor monitor) {
    m_parent = parent;
    m_parent.addProjectListener(new P_ParentListener());
  }

  public void updateKey(INlsEntry entry, String newKey, IProgressMonitor monitor) {
    if (m_entries != null) {
      NlsProjectEvent multiEvent = new NlsProjectEvent(this);
      try {
        m_translationFileEventLock.acquire();
        monitor.beginTask("update Key", IProgressMonitor.UNKNOWN);
        NlsEntry originalEntry = m_entries.remove(entry.getKey());
        if (originalEntry == null) {
          NlsCore.logError("The nls entry with the key '" + entry.getKey() + "' can not be found in project '" + getProject().getName() + "'", new Exception());
          return;
        }
        else if (originalEntry.getType() == INlsEntry.TYPE_INHERITED) {
          NlsCore.logError("The inherited NLS entry '" + originalEntry.getKey() + "' can not be modified", new Exception());
          return;
        }
        else {
          multiEvent.addChildEvent(new NlsProjectEvent(this, originalEntry, NlsProjectEvent.TYPE_ENTRY_REMOVEED));
          for (Entry<Language, String> e : entry.getAllTranslations().entrySet()) {
            ITranslationFile file = getTranslationFile(e.getKey());
            if (file != null) {
              file.updateKey(entry.getKey(), newKey, monitor);
            }
          }
          NlsEntry newEntry = new NlsEntry(originalEntry);
          newEntry.setKey(newKey);
          m_entries.put(newEntry.getKey(), newEntry);
          multiEvent.addChildEvent(new NlsProjectEvent(this, newEntry, NlsProjectEvent.TYPE_ENTRY_ADDED));
        }
      }
      finally {
        m_translationFileEventLock.release();
      }
    }
  }

  public void updateRow(INlsEntry row) {
    updateRow(row, new NullProgressMonitor());
  }

  public void updateRow(INlsEntry row, IProgressMonitor monitor) {
    if (m_entries != null) {
      try {
        m_translationFileEventLock.acquire();
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
        m_translationFileEventLock.release();
      }
    }
  }

  private void createNewRowInternal(INlsEntry row, IProgressMonitor monitor) {
    NlsEntry newRow = new NlsEntry(row);
    m_entries.put(newRow.getKey(), newRow);
    for (Entry<Language, String> entry : newRow.getAllTranslations().entrySet()) {
      ITranslationFile file = getTranslationFile(entry.getKey());
      if (file != null) {
        file.updateText(newRow.getKey(), entry.getValue(), monitor);
      }
    }
    fireNlsProjectEvent(new NlsProjectEvent(this, newRow, NlsProjectEvent.TYPE_ENTRY_ADDED));
  }

  private void updateExistingRowInternal(NlsEntry existingRow, INlsEntry row, IProgressMonitor monitor) {
    boolean updated = false;
    for (Entry<Language, String> entry : row.getAllTranslations().entrySet()) {
      String existingTranslation = existingRow.getTranslation(entry.getKey());
      if (CompareUtility.notEquals(existingTranslation, entry.getValue())) {
        ITranslationFile file = getTranslationFile(entry.getKey());
        if (file != null) {
          updated = true;
          file.updateText(row.getKey(), entry.getValue(), monitor);
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
        NlsCore.logError("NLS entry with key:'" + superRow.getKey() + "' already exists in project:'" + getProject().getName() + "'", new Exception());
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

  public void removeRow(String key) {
    NlsEntry row = m_entries.remove(key);
    Assert.isNotNull(row);
    fireNlsProjectEvent(new NlsProjectEvent(this, row, NlsProjectEvent.TYPE_ENTRY_REMOVEED));
  }

  private void handleParentRowModified(INlsEntry superRow) {
    if (m_entries != null) {
      NlsEntry entry = m_entries.get(superRow.getKey());
      if (entry == null) {
        NlsCore.logError("NLS entry with key:'" + superRow.getKey() + "' not found in the project:'" + getProject().getName() + "'", new Exception());
        return;
      }
      entry.update(superRow);
      fireNlsProjectEvent(new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
    }
  }

  public void createTranslationFile(Language language, INlsFolder folder, IProgressMonitor monitor) throws CoreException {
    String fileName = NlsCore.getLocalizedPropertiesFileName(getNlsType().getTranslationsPrefix(), language);
    IFile file = folder.getFolder().getFile(new Path(fileName));
    if (!file.exists()) {
      file.create(new ByteArrayInputStream("".getBytes()), true, monitor);
    }
    addTranslationFile(new WorkspaceTranslationFile(file), monitor);
  }

  /**
   * @param key
   *          the key to update or null to update all
   * @param value
   *          the number of references will be set to the key defined row.
   */
  public void handleReferenceCountUpdated(String key, int value) {
    Assert.isNotNull(key);
    NlsEntry row = m_entries.get(key);
    if (row != null) {
      row.setReferenceCount(value);
    }
  }

  /**
   * Provides all languages supported by this project and all its ancestors.
   *
   * @return a sorted list of all supported languages
   */
  public Language[] getAllLanguages() {
    return m_fileProvider.getAllLanguages();
  }

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

  public boolean containsLanguage(Language languge) {
    return m_fileProvider.containsFile(languge);
  }

  public boolean containsLanguageLocal(Language language) {
    return m_fileProvider.containsFile(language);
  }

  public boolean containsKey(String key) {
    return m_entries.containsKey(key);
  }

  /**
   * The listener will be notified when: - a translation file has been added to the project
   *
   * @param listener
   */
  public void addProjectListener(INlsProjectListener listener) {
    m_listeners.add(INlsProjectListener.class, listener);
  }

  public void removeProjectListener(INlsProjectListener listener) {
    m_listeners.remove(INlsProjectListener.class, listener);
  }

  protected void fireNlsProjectEvent(NlsProjectEvent event) {
    for (INlsProjectListener listener : m_listeners.getListeners(INlsProjectListener.class)) {
      listener.notifyProjectChanged(event);
    }
  }

  private void handleTranlationFileChanged(TranslationFileEvent event) {
    try {
      if (m_translationFileEventLock.acquire()) {
        Map<INlsEntry, NlsProjectEvent> addEvents = new HashMap<INlsEntry, NlsProjectEvent>();
        Map<INlsEntry, NlsProjectEvent> modifyEvents = new HashMap<INlsEntry, NlsProjectEvent>();
        Map<INlsEntry, NlsProjectEvent> removeEvents = new HashMap<INlsEntry, NlsProjectEvent>();
        handleTranslationFileChangedRec(addEvents, modifyEvents, removeEvents, event);
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
      m_translationFileEventLock.release();
    }
  }

  /**
   * @param addEvents
   * @param modifyEvents
   * @param removeEvents
   * @param event
   */
  private void handleTranslationFileChangedRec(Map<INlsEntry, NlsProjectEvent> addEvents, Map<INlsEntry, NlsProjectEvent> modifyEvents, Map<INlsEntry, NlsProjectEvent> removeEvents,
      TranslationFileEvent event) {
    if (event.isMulti()) {
      for (TranslationFileEvent e : event.getSubEvents()) {
        handleTranslationFileChangedRec(addEvents, modifyEvents, removeEvents, e);
      }
    }
    else {
      String key = event.getKey();
      String translation = event.getTranslation();
      NlsEntry entry = m_entries.get(key);
      switch (event.getType()) {
        case TranslationFileEvent.TYPE_ENTRY_ADD:
          if (entry == null) {
            entry = new NlsEntry(key, this);
            entry.addTranslation(event.getSource().getLanguage(), translation);
            m_entries.put(entry.getKey(), entry);
            // always an event
            addEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_ADDED));
          }
          else {
            NlsCore.logWarning("translation file fired an add event of the existing NLS entry:'" + entry.getKey() + "'");
          }
          break;
        case TranslationFileEvent.TYPE_ENTRY_REMOVE:
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
        case TranslationFileEvent.TYPE_ENTRY_MODIFY:
          if (entry != null) {
            entry.addTranslation(event.getSource().getLanguage(), translation);
            modifyEvents.put(entry, new NlsProjectEvent(this, entry, NlsProjectEvent.TYPE_ENTRY_MODIFYED));
          }
          break;
        case TranslationFileEvent.TYPE_FILE_REMOVED:
          fullRefresh(true);
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
          case NlsProjectEvent.TYPE_TRANSLATION_FILE_ADDED: {
            // TODO
            break;
          }
        }
      }
    }
  } // end class P_ParentListener

  private class P_TranslationFileChangedListener implements ITranslationFileListener {
    public void translationFileChanged(TranslationFileEvent event) {
      handleTranlationFileChanged(event);

    }
  } // end class P_TranslstionFIleChangedListnener

  private class P_NlsClassPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      if (NlsType.PROP_TRANSLATION_FILE_PREFIX.equals(evt.getPropertyName()) ||
          NlsType.PROP_TRANSLATION_FOLDER_NAME.equals(evt.getPropertyName())) {
        // reset cache
        resetCache();
        // find files
        updateTranslationFileLocation();
        // fire full reload
        fireNlsProjectEvent(new NlsProjectEvent(NlsProject.this, NlsProjectEvent.TYPE_FULL_REFRESH));
      }
      else if (NlsType.PROP_SUPER_TYPE.equals(evt.getPropertyName())) {

        resetCache();
        setSuperType(m_nlsClass.getSuperType());
        fireNlsProjectEvent(new NlsProjectEvent(NlsProject.this, NlsProjectEvent.TYPE_FULL_REFRESH));
      }
    }

  } // end class P_NlsClassPropertyChangeListener

}
