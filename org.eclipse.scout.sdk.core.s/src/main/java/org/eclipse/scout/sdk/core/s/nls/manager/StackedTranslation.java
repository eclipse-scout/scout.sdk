/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.manager;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreComparator;
import org.eclipse.scout.sdk.core.util.Ensure;

public class StackedTranslation extends Translation implements IStackedTranslation {

  private final Map<ITranslationStore, ITranslationEntry> m_entries; // unsorted
  private Map<Language, String> m_mergedTexts; // created on first use
  private Map<Language, ITranslationEntry> m_entriesByLanguage; // created on first use

  protected StackedTranslation(List<ITranslationEntry> entries) {
    super(entries.get(0).key()); // element must exist
    m_entries = entries.stream()
        .collect(toMap(ITranslationEntry::store, Function.identity(), Ensure::failOnDuplicates));
  }

  @Override
  protected Map<Language, String> textsMap() {
    if (m_mergedTexts == null) {
      buildCaches();
    }
    return m_mergedTexts;
  }

  protected Map<Language, ITranslationEntry> entriesByLanguage() {
    if (m_entriesByLanguage == null) {
      buildCaches();
    }
    return m_entriesByLanguage;
  }

  protected void buildCaches() {
    Map<Language, String> textsMap = new HashMap<>();
    Map<Language, ITranslationEntry> entryMap = new HashMap<>();

    m_entries.values().stream()
        .sorted(comparing(ITranslationEntry::store, TranslationStoreComparator.INSTANCE).reversed())
        .forEach(entry -> cacheEntry(entry, textsMap, entryMap));

    m_mergedTexts = textsMap;
    m_entriesByLanguage = entryMap;
  }

  protected static void cacheEntry(ITranslationEntry entry, Map<Language, String> textsByLang, Map<Language, ITranslationEntry> entriesByLang) {
    var textsOfEntry = entry.texts();
    textsByLang.putAll(textsOfEntry);
    textsOfEntry.keySet().forEach(lang -> entriesByLang.put(lang, entry));
  }

  @Override
  public boolean isOverriding(Language language, ITranslationStore store) {
    if (!store.containsLanguage(language)) {
      return false;
    }
    var order = store.service().order();
    return m_entries.keySet().stream()
        .anyMatch(s -> s.service().order() > order);
  }

  @Override
  public Stream<Language> languagesOfAllStores() {
    return stores()
        .flatMap(ITranslationStore::languages)
        .distinct();
  }

  @Override
  public boolean hasEditableStores() {
    return stores().anyMatch(ITranslationStore::isEditable);
  }

  @Override
  public boolean hasOnlyEditableStores() {
    return stores().allMatch(ITranslationStore::isEditable);
  }

  @Override
  public Stream<ITranslationStore> stores() {
    return m_entries.keySet().stream();
  }

  @Override
  public Optional<ITranslationStore> primaryEditableStore() {
    return stores()
        .filter(ITranslationStore::isEditable)
        .min(TranslationStoreComparator.INSTANCE);
  }

  @Override
  public Optional<ITranslationEntry> entry(Language language) {
    return Optional.ofNullable(entriesByLanguage().get(language));
  }

  protected void entryAdded(ITranslationEntry newEntry) {
    ensureHasSameKey(newEntry);
    m_entries.put(newEntry.store(), newEntry);
    m_mergedTexts = null;
    m_entriesByLanguage = null;
  }

  protected void entryUpdated(ITranslationEntry updatedEntry) {
    ensureHasSameKey(updatedEntry);
    m_entries.put(updatedEntry.store(), updatedEntry);
    m_mergedTexts = null;
    m_entriesByLanguage = null;
  }

  protected void ensureHasSameKey(ITranslation t) {
    Ensure.isTrue(key().equals(t.key()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    var that = (StackedTranslation) o;
    return m_entries.equals(that.m_entries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), m_entries);
  }
}
