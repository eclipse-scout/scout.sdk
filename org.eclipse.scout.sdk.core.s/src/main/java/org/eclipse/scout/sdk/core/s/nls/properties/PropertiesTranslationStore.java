/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.properties;

import static java.util.Collections.unmodifiableMap;
import static org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.getPropertiesFileName;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.IEditableTranslationStore;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationEntry;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link PropertiesTranslationStore}</h3>
 *
 * @since 7.0.0
 */
public class PropertiesTranslationStore implements IEditableTranslationStore {

  private final PropertiesTextProviderService m_svc;
  private final Map<String, TranslationEntry> m_translations;
  private final Map<Language, ITranslationPropertiesFile> m_files;
  private final Set<ITranslationPropertiesFile> m_newFiles;
  private boolean m_isEditable;
  private boolean m_isDirty;

  public PropertiesTranslationStore(PropertiesTextProviderService svc) {
    m_svc = Ensure.notNull(svc);
    m_translations = new HashMap<>();
    m_files = new HashMap<>();
    m_newFiles = new HashSet<>();
  }

  /**
   * Loads the store using the specified files.
   *
   * @param translationFiles
   *          The {@link ITranslationPropertiesFile}s that belong to this store. The files must not be loaded already
   *          but are loaded as part of this method call.
   * @param progress
   *          The {@link IProgress} monitor.
   */
  public void load(Collection<ITranslationPropertiesFile> translationFiles, IProgress progress) {
    Ensure.notNull(translationFiles);

    int ticksByFile = 100;
    progress.init(translationFiles.size() * ticksByFile, "Load translation files for service '{}'.", service().type().name());

    boolean isEditable = !translationFiles.isEmpty();
    m_translations.clear();
    translationFiles().clear();
    for (ITranslationPropertiesFile f : translationFiles) {
      // load data from file
      f.load(progress.newChild(ticksByFile));

      // create translation mapping by key
      loadFileContent(f);

      // create translation mapping by language
      translationFiles().put(f.language(), f);

      if (!f.isEditable()) {
        isEditable = false;
      }
    }
    setDirty(false);
    m_isEditable = isEditable;
  }

  protected void loadFileContent(ITranslationPropertiesFile f) {
    f.allEntries()
        .forEach((key, value) -> m_translations
            .computeIfAbsent(key, k -> new TranslationEntry(k, this))
            .putText(f.language(), value));
  }

  @Override
  public ITranslationEntry changeKey(String oldKey, String newKey) {
    setDirty(true);
    TranslationEntry removed = (TranslationEntry) removeTranslation(oldKey);
    if (removed == null) {
      SdkLog.warning("Cannot update key '{}' to '{}' because it could not be found.", oldKey, newKey);
      return null;
    }
    removed.setKey(newKey);
    addTranslationEntry(removed);
    return removed;
  }

  @Override
  public ITranslationEntry updateTranslation(ITranslation newEntry) {
    String key = newEntry.key();
    TranslationEntry entryToModify = (TranslationEntry) get(key).get();
    setDirty(true);

    ensureAllLanguagesExist(newEntry);

    // remove translation from all properties files
    for (Language l : entryToModify.texts().keySet()) {
      translationFiles().get(l).removeTranslation(key);
    }
    // update instance
    entryToModify.setTexts(newEntry.texts());

    // add to new properties files
    for (Entry<Language, String> newTranslations : entryToModify.texts().entrySet()) {
      translationFiles().get(newTranslations.getKey()).setTranslation(key, newTranslations.getValue());
    }
    return entryToModify;
  }

  protected void ensureAllLanguagesExist(ITranslation translation) {
    translation.texts().keySet().stream()
        .filter(lang -> languages().noneMatch(existing -> existing.equals(lang)))
        .forEach(this::addNewLanguage);
  }

  @Override
  public ITranslationEntry addNewTranslation(ITranslation newTranslation) {
    throwIfReadOnly();
    TranslationEntry newEntry = new TranslationEntry(newTranslation, this);
    ensureAllLanguagesExist(newEntry);
    setDirty(true);
    addTranslationEntry(newEntry);
    return newEntry;
  }

  protected void addTranslationEntry(TranslationEntry entryToAdd) {
    m_translations.put(entryToAdd.key(), entryToAdd);
    entryToAdd.texts().forEach((key, value) -> updateTextInFile(key, entryToAdd.key(), value));
  }

  @Override
  public ITranslationEntry removeTranslation(String key) {
    throwIfReadOnly();
    setDirty(true);
    translationFiles().values().forEach(f -> f.removeTranslation(key));
    return m_translations.remove(key);
  }

  protected void updateTextInFile(Language l, String key, String text) {
    ITranslationPropertiesFile file = translationFiles().get(l);
    if (file == null) {
      SdkLog.warning("Cannot add text '{}' for key '{}' and language '{}' because this language does not exist in store {}.", text, key, l, this);
      return;
    }
    file.setTranslation(key, text);
  }

  @Override
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public void addNewLanguage(Language language) {
    throwIfReadOnly();
    Ensure.notNull(language);
    Path directory = translationFiles().values().stream()
        .filter(f -> f instanceof EditableTranslationFile)
        .map(f -> (EditableTranslationFile) f)
        .findAny()
        .orElseThrow(() -> newFail("Cannot create new language because the store '{}' is not editable.", this))
        .path()
        .getParent();

    String fileName = getPropertiesFileName(service().filePrefix(), language);
    ITranslationPropertiesFile newFile = new EditableTranslationFile(directory.resolve(fileName), language);
    newFile.load(new NullProgress());

    setDirty(true);
    translationFiles().put(language, newFile);
    m_newFiles.add(newFile);
  }

  @Override
  public void flush(IEnvironment env, IProgress progress) {
    if (!isDirty()) {
      return;
    }
    for (ITranslationPropertiesFile f : translationFiles().values()) {
      f.flush(env, progress);
    }
    m_newFiles.clear();
    setDirty(false);
  }

  protected void throwIfReadOnly() {
    if (!isEditable()) {
      throw newFail("Translation store {} is read-only.", this);
    }
  }

  protected void setDirty(boolean dirty) {
    m_isDirty = dirty;
  }

  @Override
  public boolean isDirty() {
    return m_isDirty;
  }

  @Override
  public boolean isEditable() {
    return m_isEditable;
  }

  @Override
  public boolean containsKey(String key) {
    return m_translations.containsKey(key);
  }

  @Override
  public boolean containsLanguage(Language language) {
    return m_files.containsKey(language);
  }

  @Override
  public Stream<String> keys() {
    return m_translations.keySet().stream();
  }

  @Override
  public long size() {
    return m_translations.size();
  }

  @Override
  public Optional<ITranslationEntry> get(String key) {
    return Optional.ofNullable(m_translations.get(key));
  }

  @Override
  public Optional<String> get(String key, Language language) {
    return get(key)
        .flatMap(t -> t.text(language));
  }

  @Override
  public Optional<Map<String, String>> get(Language language) {
    return Optional.ofNullable(translationFiles().get(language))
        .map(ITranslationPropertiesFile::allEntries);
  }

  @Override
  public Stream<? extends ITranslationEntry> entries() {
    return m_translations.values().stream();
  }

  @Override
  public Stream<Language> languages() {
    return translationFiles().keySet().stream();
  }

  @Override
  public PropertiesTextProviderService service() {
    return m_svc;
  }

  @Override
  public void reload(IProgress progress) {
    // remove created but not yet flushed languages
    translationFiles().values().removeAll(m_newFiles);
    m_newFiles.clear();

    // reload
    load(new ArrayList<>(translationFiles().values()), progress); // create a copy here because the load modifies the translation files. otherwise it modifies its own method argument.
  }

  protected Map<Language, ITranslationPropertiesFile> translationFiles() {
    return m_files;
  }

  public Map<Language, ITranslationPropertiesFile> files() {
    return unmodifiableMap(m_files);
  }

  @Override
  public int hashCode() {
    return service().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    ITranslationStore other = (ITranslationStore) obj;
    return service().equals(other.service());
  }

  @Override
  public String toString() {
    return PropertiesTranslationStore.class.getSimpleName() + " [" + service().type().name() + ']';
  }
}
