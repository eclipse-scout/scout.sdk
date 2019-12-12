/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.properties;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
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
    int ticksByFile = 100;
    progress.init("Load translation files for service " + service().type().name(), Ensure.notNull(translationFiles).size() * ticksByFile);

    boolean isEditable = !Ensure.notNull(translationFiles).isEmpty();
    m_translations.clear();
    for (ITranslationPropertiesFile f : translationFiles) {
      // load data from file
      f.load(progress.newChild(ticksByFile));

      // create translation mapping by key
      loadFileContent(f);

      // create translation mapping by language
      m_files.put(f.language(), f);

      if (!f.isEditable()) {
        isEditable = false;
      }
    }
    setDirty(false);
    m_isEditable = isEditable;
  }

  protected void loadFileContent(ITranslationPropertiesFile f) {
    f.allEntries()
        .forEach((key, translation) -> m_translations
            .computeIfAbsent(key, k -> new TranslationEntry(k, this))
            .putTranslation(f.language(), translation));
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
    for (Language l : entryToModify.translations().keySet()) {
      m_files.get(l).removeTranslation(key);
    }
    // update instance
    entryToModify.setTranslations(newEntry.translations());

    // add to new properties files
    for (Entry<Language, String> newTranslations : entryToModify.translations().entrySet()) {
      m_files.get(newTranslations.getKey()).setTranslation(key, newTranslations.getValue());
    }
    return entryToModify;
  }

  protected void ensureAllLanguagesExist(ITranslation translation) {
    translation.translations().keySet().stream()
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
    entryToAdd.translations().forEach((key, value) -> updateTextInFile(key, entryToAdd.key(), value));
  }

  @Override
  public ITranslationEntry removeTranslation(String key) {
    throwIfReadOnly();
    setDirty(true);
    m_files.values().forEach(f -> f.removeTranslation(key));
    return m_translations.remove(key);
  }

  protected void updateTextInFile(Language l, String key, String text) {
    ITranslationPropertiesFile file = m_files.get(l);
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
    Path directory = m_files.values().stream()
        .filter(f -> f instanceof EditableTranslationFile)
        .map(f -> (EditableTranslationFile) f)
        .findAny()
        .orElseThrow(() -> newFail("Cannot create new language because the store '{}' is not editable.", this))
        .file()
        .getParent();

    String fileName = getPropertiesFileName(service().filePrefix(), language);
    ITranslationPropertiesFile newFile = new EditableTranslationFile(directory.resolve(fileName));
    newFile.load(new NullProgress());

    setDirty(true);
    m_files.put(language, newFile);
    m_newFiles.add(newFile);
  }

  /**
   * Gets the filename for a {@code .properties} file using the specified prefix and {@link Language}.
   *
   * @param prefix
   *          The file prefix. Must not be {@code null}.
   * @param language
   *          The language. Must not be {@code null}.
   * @return The file name.
   */
  public static String getPropertiesFileName(String prefix, Language language) {
    return prefix + '_' + language.locale() + ".properties";
  }

  @Override
  public void flush(IEnvironment env, IProgress progress) {
    if (!isDirty()) {
      return;
    }
    for (ITranslationPropertiesFile f : m_files.values()) {
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
  public Stream<String> keys() {
    return m_translations.keySet().stream();
  }

  @Override
  public Optional<ITranslationEntry> get(String key) {
    return Optional.ofNullable(m_translations.get(key));
  }

  @Override
  public Optional<String> get(String key, Language language) {
    return get(key)
        .flatMap(t -> t.translation(language));
  }

  @Override
  public Optional<Map<String, String>> get(Language language) {
    return Optional.ofNullable(m_files.get(language))
        .map(ITranslationPropertiesFile::allEntries);
  }

  @Override
  public Stream<? extends ITranslationEntry> entries() {
    return m_translations.values().stream();
  }

  @Override
  public Stream<Language> languages() {
    return m_files.keySet().stream();
  }

  @Override
  public PropertiesTextProviderService service() {
    return m_svc;
  }

  @Override
  public void reload(IProgress progress) {
    // remove created but not yet flushed languages
    m_files.values().removeAll(m_newFiles);
    m_newFiles.clear();

    // reload
    load(m_files.values(), progress);
  }

  @Override
  public int hashCode() {
    return m_svc.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    PropertiesTranslationStore other = (PropertiesTranslationStore) obj;
    return m_svc.equals(other.m_svc);
  }

  @Override
  public String toString() {
    return PropertiesTranslationStore.class.getSimpleName() + " [" + service().type().name() + ']';
  }
}
