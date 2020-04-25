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
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack.toEditableStore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * An {@link IEditableTranslationStore} wrapper reducing the visible translations by key
 * ({@link ITranslation#key()}).<br/>
 * If the wrapped store is editable, this store will be editable too.
 */
public class FilteredTranslationStore implements IEditableTranslationStore {

  private final ITranslationStore m_store;
  private final Set<String> m_keysFilter;

  public FilteredTranslationStore(ITranslationStore store, Collection<String> availableKeysFilter) {
    m_store = Ensure.notNull(store);
    m_keysFilter = new HashSet<>(Ensure.notNull(availableKeysFilter));
  }

  @Override
  public ITranslationEntry addNewTranslation(ITranslation newTranslation) {
    return toEditableStore(m_store).addNewTranslation(newTranslation);
  }

  @Override
  public void flush(IEnvironment env, IProgress progress) {
    toEditableStore(m_store).flush(env, progress);
  }

  @Override
  public ITranslationEntry removeTranslation(String key) {
    return toEditableStore(m_store).removeTranslation(key);
  }

  @Override
  public void addNewLanguage(Language language) {
    toEditableStore(m_store).addNewLanguage(language);
  }

  @Override
  public boolean isDirty() {
    return toEditableStore(m_store).isDirty();
  }

  @Override
  public ITranslationEntry changeKey(String oldKey, String newKey) {
    return toEditableStore(m_store).changeKey(oldKey, newKey);
  }

  @Override
  public ITranslationEntry updateTranslation(ITranslation template) {
    return toEditableStore(m_store).updateTranslation(template);
  }

  @Override
  public boolean isEditable() {
    return m_store.isEditable();
  }

  @Override
  public Stream<String> keys() {
    return m_store.keys()
        .filter(keysFilter()::contains);
  }

  @Override
  public boolean containsKey(String key) {
    return keysFilter().contains(key) && m_store.containsKey(key);
  }

  @Override
  public Stream<? extends ITranslationEntry> entries() {
    return m_store.entries()
        .filter(e -> keysFilter().contains(e.key()));
  }

  @Override
  public Stream<Language> languages() {
    return entries()
        .flatMap(e -> e.texts().keySet().stream())
        .distinct();
  }

  @Override
  public Optional<ITranslationEntry> get(String key) {
    return m_store.get(key)
        .filter(e -> keysFilter().contains(e.key()));
  }

  @Override
  public Optional<Map<String, String>> get(Language language) {
    return m_store.get(language)
        .map(this::getFilteredCopy);
  }

  protected Map<String, String> getFilteredCopy(Map<String, String> original) {
    return original.entrySet()
        .stream()
        .filter(entry -> keysFilter().contains(entry.getKey()))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  protected Set<String> keysFilter() {
    return m_keysFilter;
  }

  @Override
  public Optional<String> get(String key, Language language) {
    if (!keysFilter().contains(key)) {
      return Optional.empty();
    }
    return m_store.get(key, language);
  }

  @Override
  public TextProviderService service() {
    return m_store.service();
  }

  @Override
  public void reload(IProgress progress) {
    m_store.reload(progress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(service(), keysFilter());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    FilteredTranslationStore other = (FilteredTranslationStore) obj;
    return service().equals(other.service())
        && keysFilter().equals(other.keysFilter());
  }

  @Override
  public String toString() {
    return FilteredTranslationStore.class.getSimpleName() + " [" + service().type().name() + ']';
  }
}
