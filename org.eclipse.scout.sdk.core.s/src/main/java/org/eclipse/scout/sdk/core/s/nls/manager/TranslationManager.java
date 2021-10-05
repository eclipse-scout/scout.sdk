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

import static java.util.Collections.singleton;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.isForbidden;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateDefaultText;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateKey;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createAddLanguageEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createAddTranslationEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createChangeKeyEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createFlushEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createReloadEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createRemoveTranslationEvent;
import static org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent.createUpdateTranslationEvent;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.IEditableTranslationStore;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.ITranslationImportInfo;
import org.eclipse.scout.sdk.core.s.nls.ITranslationManagerListener;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TextProviderService;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreComparator;
import org.eclipse.scout.sdk.core.s.nls.Translations;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.EventListenerList;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

public class TranslationManager {

  private final List<ITranslationStore> m_stores;
  private final Map<String, StackedTranslation> m_translations;

  private final EventListenerList m_listeners;
  private final List<TranslationManagerEvent> m_eventBuffer;

  private int m_changing;

  protected TranslationManager(Stream<ITranslationStore> stores) {
    m_stores = stores
        .sorted(TranslationStoreComparator.INSTANCE)
        .collect(toList());
    m_translations = buildStackedTranslations(m_stores.stream());
    m_listeners = new EventListenerList();
    m_eventBuffer = new ArrayList<>();

    Translations.storesHavingImplicitOverrides(m_stores.stream()).forEach(TranslationManager::logImplicitOverrides);
  }

  /**
   * Use {@link Translations#createManager(Stream)} instead
   */
  public static Optional<TranslationManager> create(Stream<ITranslationStore> stores) {
    return Optional.of(new TranslationManager(stores))
        .filter(manager -> manager.allStores().findAny().isPresent());
  }

  protected static Map<String, StackedTranslation> buildStackedTranslations(Stream<ITranslationStore> stores) {
    return stores
        .flatMap(ITranslationStore::entries)
        .collect(groupingBy(ITranslation::key, Collector.of(ArrayList::new, List::add, TranslationManager::combine, StackedTranslation::new, Characteristics.UNORDERED)));
  }

  private static List<ITranslationEntry> combine(List<ITranslationEntry> left, Collection<ITranslationEntry> right) {
    left.addAll(right);
    return left;
  }

  protected static void logImplicitOverrides(Collection<ITranslationStore> storesWithImplicitOverrides) {
    var names = storesWithImplicitOverrides.stream()
        .map(ITranslationStore::service)
        .map(TextProviderService::type)
        .map(IType::name)
        .collect(joining(", ", "[", "]"));
    SdkLog.warning("There are TextProviderServices with common keys and the same @Order value: {}. " +
        "To ensure a stable override of translations each service should have a unique @Order value!", names);
  }

  /**
   * Gets a {@link Stream} returning all {@link IStackedTranslation} instances whose key starts with the specified
   * prefix.
   *
   * @param prefix
   *          The prefix. Is evaluated case-insensitive. Must not be {@code null}.
   * @return A {@link Stream} with all entries with keys starting with specified prefix.
   */
  public Stream<? extends IStackedTranslation> allTranslationsWithPrefix(String prefix) {
    return allTranslations()
        .filter(t -> t.key().regionMatches(true, 0, prefix, 0, prefix.length()));
  }

  /**
   * Generates a new unused key using the specified text as base.
   *
   * @param baseText
   *          The base text. From this the new key is derived.
   * @return A new valid key or an empty {@link String} if the input text is empty or {@code null}.
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public String generateNewKey(CharSequence baseText) {
    if (Strings.isBlank(baseText)) {
      return "";
    }

    // remove not allowed characters
    var cleaned = Pattern.compile("[^" + ITranslation.KEY_ALLOWED_CHARACTER_SET + " ]*").matcher(baseText).replaceAll("");
    if (Strings.isBlank(cleaned)) {
      return "";
    }

    // camel case multiple words
    var segments = Pattern.compile(" ")
        .splitAsStream(cleaned)
        .filter(Strings::hasText)
        .toArray(String[]::new);

    // remove not allowed characters from the first segment
    var firstSegment = new StringBuilder(segments[0]);
    //noinspection CharacterComparison
    while (firstSegment.length() > 0 && !((firstSegment.charAt(0) >= 'a' && firstSegment.charAt(0) <= 'z') || (firstSegment.charAt(0) >= 'A' && firstSegment.charAt(0) <= 'Z'))) {
      firstSegment.deleteCharAt(0);
    }
    segments[0] = firstSegment.toString();

    // remove not allowed characters from the last segment
    if (segments.length > 1) {
      var lastSegment = new StringBuilder(segments[segments.length - 1]);
      while (lastSegment.length() > 0 && (lastSegment.charAt(lastSegment.length() - 1) == '.' || lastSegment.charAt(lastSegment.length() - 1) == '-')) {
        lastSegment.deleteCharAt(lastSegment.length() - 1);
      }
      segments[segments.length - 1] = lastSegment.toString();
    }

    String keyCandidate;
    if (segments.length < 2) {
      keyCandidate = segments[0]; // do not capitalize if only one segment
    }
    else {
      keyCandidate = Arrays.stream(segments)
          .map(Strings::capitalize)
          .collect(joining());
    }

    var maxLength = 190;
    // ensure max length
    if (keyCandidate.length() > maxLength) {
      keyCandidate = keyCandidate.substring(0, maxLength);
    }

    // add unique ending number
    var result = keyCandidate;
    var i = 0;
    while (containsKeyIgnoreCase(result)) {
      result = keyCandidate + i;
      i++;
    }
    return result;
  }

  private boolean containsKeyIgnoreCase(String key) {
    return m_translations.keySet().stream()
        .anyMatch(existing -> existing.equalsIgnoreCase(key));
  }

  /**
   * @return If this manager is editable. A {@link TranslationManager} is considered to be editable if at least one
   *         {@link ITranslationStore} is editable.
   */
  public boolean isEditable() {
    return primaryEditableStore().isPresent();
  }

  /**
   * Gets the {@link IStackedTranslation} for the specified key.
   *
   * @param key
   *          The key to search.
   * @return The {@link IStackedTranslation} for the specified key or an empty {@link Optional} if the key cannot be
   *         found.
   */
  public Optional<IStackedTranslation> translation(String key) {
    return Optional.ofNullable(m_translations.get(key));
  }

  /**
   * Checks if this manager contains an {@link IStackedTranslation} having the given key.
   *
   * @param key
   *          The key to search.
   * @return {@code true} if at least one store contains the specified key.
   */
  public boolean containsKey(String key) {
    return m_translations.containsKey(key);
  }

  /**
   * @return All {@link ITranslationStore}s that can be modified.
   */
  public Stream<ITranslationStore> allEditableStores() {
    return allStores()
        .filter(ITranslationStore::isEditable);
  }

  /**
   * @return The first (primary) {@link ITranslationStore} that is editable.
   */
  public Optional<ITranslationStore> primaryEditableStore() {
    return allEditableStores()
        .findFirst();
  }

  /**
   * Merges the {@link ITranslation} specified into this manager.
   * <p>
   * If an editable entry with the same key as the {@link ITranslation} provided already exists in this manager, this
   * entry is merged with the given one.
   * <p>
   * Otherwise, a new translation is created within the {@link ITranslationStore} provided.
   * <p>
   * If the {@link ITranslation} provided contains new {@link Language languages}, languages will be created as needed.
   * <p>
   * The difference to {@link #setTranslation(ITranslation, ITranslationStore)} is that this method does not remove
   * texts for languages not present in the given new translation. While
   * {@link #setTranslation(ITranslation, ITranslationStore)} updates the translation to exactly match the given one
   * this method keeps the texts from both (the ones from the given translation win).
   *
   * @param newTranslation
   *          The new {@link ITranslation} that should be merged into this store. Must not be {@code null}.
   * @param targetInCaseNew
   *          An optional {@link ITranslationStore} in which the entry should be created in case it does not yet exist.
   *          May be {@code null} which means the primary (first) editable store available in this manager is used.
   * @return The created or updated {@link IStackedTranslation}.
   * @throws IllegalArgumentException
   *           if the given translation is invalid, the given store does not belong to this manager or is not editable.
   * @see #setTranslation(ITranslation, ITranslationStore)
   */
  public IStackedTranslation mergeTranslation(ITranslation newTranslation, ITranslationStore targetInCaseNew) {
    var merged = translation(newTranslation.key())
        .map(t -> t.merged(newTranslation))
        .orElse(newTranslation);
    return setTranslation(merged, targetInCaseNew);
  }

  /**
   * Imports translations from tabular data.
   * <p>
   * For new {@link ITranslation translations} the store given is used. If no specific target is specified, the first
   * editable store in this manager is used.
   *
   * @param rawTableData
   *          The tabular data to import. The {@link List} must contain a header row specifying the languages and the
   *          key column. Only rows after such a header row are imported.
   * @param targetInCaseNew
   *          The {@link ITranslationStore} in which the entry should be created in case it does not yet exist. May be
   *          {@code null} which means the primary (first) editable store available in this manager is used.
   * @param keyColumnName
   *          The text that identifies the key column in the header row. Must not be {@code null} or an empty
   *          {@link String}.
   * @return An {@link ITranslationImportInfo} describing the result of the import. It allows accessing e.g. the
   *         imported entries, ignored columns or rows.
   */
  public ITranslationImportInfo importTranslations(List<List<String>> rawTableData, String keyColumnName, ITranslationStore targetInCaseNew) {
    var importer = new TranslationImporter(this, rawTableData, keyColumnName, targetInCaseNew);
    importer.tryImport();
    return importer;
  }

  /**
   * Changes the specified key to a new value.<br>
   * If none of the stores of the current translation already contains the new key but other stores of this manager do,
   * the two are merged using the new key.
   *
   * @param oldKey
   *          The old key of the existing entry to modify. Must not be blank.
   * @param newKey
   *          The new key of that entry. Must not be blank.
   * @throws IllegalArgumentException
   *           if the existing translation with the old key contains read-only translation stores or one of the stores
   *           of the current translation already contains the new key.
   */
  public void changeKey(String oldKey, String newKey) {
    if (Objects.equals(oldKey, newKey)) {
      return;
    }
    var entry = m_translations.get(oldKey);
    if (entry == null) {
      return;
    }
    Ensure.isTrue(entry.hasOnlyEditableStores(), "Cannot change key '{}' to '{}' because some entries are read-only.", oldKey, newKey);
    entry.stores()
        .forEach(s -> Ensure.isFalse(isForbidden(validateKey(this, s, newKey)), "Cannot change key '{}' to '{}' because the new key is not valid.", oldKey, newKey));

    runAndFireChanged(() -> changeKeyInternal(entry, oldKey, newKey));
  }

  protected TranslationManagerEvent changeKeyInternal(IStackedTranslation entry, String oldKey, String newKey) {
    var newEntries = entry.stores()
        .map(TranslationManager::toEditableStore)
        .map(store -> store.changeKey(oldKey, newKey))
        .collect(toList());
    m_translations.remove(oldKey);

    var existingTranslationWithNewKey = m_translations.get(newKey);
    if (existingTranslationWithNewKey != null) {
      // in case the newKey is not used in the stores of the entry to change but in other stores
      // then it is overriding now and the rows must be merged
      newEntries.forEach(existingTranslationWithNewKey::entryUpdated);
      fireManagerChanged(createRemoveTranslationEvent(this, entry));
      return createUpdateTranslationEvent(this, existingTranslationWithNewKey);
    }

    var newEntry = new StackedTranslation(newEntries);
    m_translations.put(newKey, newEntry);
    return createChangeKeyEvent(this, newEntry, oldKey);
  }

  /**
   * Removes all entries with the specified keys.
   *
   * @param keys
   *          The keys of the entries to remove.
   * @throws IllegalArgumentException
   *           if a translation contains read-only {@link ITranslationStore stores}.
   */
  public void removeTranslations(Stream<String> keys) {
    if (keys == null) {
      return;
    }

    setChanging(true);
    try {
      keys
          .filter(Strings::hasText)
          .map(this::removeTranslationInternal)
          .forEach(this::fireManagerChanged);
    }
    finally {
      setChanging(false);
    }
  }

  protected TranslationManagerEvent removeTranslationInternal(String key) {
    var toRemove = m_translations.get(key);
    if (toRemove == null) {
      return null;
    }
    Ensure.isTrue(toRemove.hasOnlyEditableStores(), "Cannot delete translation with key '{}' because some entries are read-only.", key);

    toRemove.stores()
        .map(TranslationManager::toEditableStore)
        .forEach(store -> store.removeTranslation(key));
    m_translations.remove(key);
    return createRemoveTranslationEvent(this, toRemove);
  }

  /**
   * @see #setTranslation(ITranslation, ITranslationStore)
   */
  public IStackedTranslation setTranslation(ITranslation newTranslation) {
    return setTranslation(newTranslation, null);
  }

  /**
   * Updates or creates an {@link IStackedTranslation}.
   * <p>
   * If a translation with the same key as the new translation provided already exists, it is updated to match the given
   * one. Texts for languages no longer present are removed, new languages are created as necessary. Texts for new
   * languages or languages that are modified but currently stored in a read-only store are saved in the
   * {@link ITranslationStore} provided.
   * <p>
   * If no translation with such a key exists in this manager, a new one is created in the {@link ITranslationStore}
   * provided.
   * <p>
   * The following conditions must be fulfilled that the operation can be completed:
   * <ul>
   * <li>The specified {@link ITranslation} must not be {@code null} and contain a valid key.</li>
   * <li>The specified {@link ITranslation} must contain a text for the {@link Language#LANGUAGE_DEFAULT} if not an
   * overridden text already provides one.</li>
   * <li>The specified store must be {@link ITranslationStore#isEditable() editable} or {@code null}.</li>
   * <li>The specified store must be part of this manager.</li>
   * </ul>
   *
   * @param newTranslation
   *          The new translation. Must not be {@code null}.
   * @param storeForNew
   *          An optional {@link ITranslationStore} to save texts for new languages or languages that are currently
   *          read-only. If it is {@code null} the {@link IStackedTranslation#primaryEditableStore() primary editable
   *          store} of the existing translation is used (if present). Otherwise, the {@link #primaryEditableStore()
   *          primary editable store} of this manager is used.
   * @return The created or updated {@link IStackedTranslation}.
   * @throws IllegalArgumentException
   *           if one of the mentioned condition is not fulfilled.
   * @see #mergeTranslation(ITranslation, ITranslationStore)
   */
  public IStackedTranslation setTranslation(ITranslation newTranslation, ITranslationStore storeForNew) {
    Ensure.notNull(newTranslation, "A translation must be specified.");
    Ensure.isTrue(storeForNew == null || m_stores.contains(storeForNew), "Store of wrong manager.");
    Ensure.isFalse(isForbidden(validateKey(newTranslation.key())), "Invalid key");

    var existingEntry = m_translations.get(newTranslation.key());
    Ensure.isFalse(isForbidden(validateDefaultText(newTranslation.text(Language.LANGUAGE_DEFAULT).orElse(null), existingEntry)),
        "Translation validation failed. Ensure a valid key and default text is available.");

    if (existingEntry == null) {
      return addNewTranslationInternal(newTranslation, storeForNew);
    }

    runAndFireChanged(() -> updateTranslationInternal(newTranslation, existingEntry, storeForNew));
    return existingEntry;
  }

  protected IStackedTranslation addNewTranslationInternal(ITranslation newTranslation, ITranslationStore target) {
    var editableStore = toEditableStore(
        Optional.ofNullable(target)
            .orElseGet(() -> primaryEditableStore()
                .orElseThrow(() -> newFail("Cannot create new entries. All translation stores are read-only."))));
    var result = new FinalValue<IStackedTranslation>();
    runAndFireChanged(() -> {
      var createdTranslation = editStoreObservingLanguages(editableStore, s -> s.setTranslation(newTranslation));
      var newStacked = new StackedTranslation(List.of(createdTranslation));
      m_translations.put(newStacked.key(), newStacked);
      result.set(newStacked);
      return createAddTranslationEvent(this, newStacked);
    });
    return result.get();
  }

  protected TranslationManagerEvent updateTranslationInternal(ITranslation newTranslation, StackedTranslation entryToUpdate, ITranslationStore storeForNewLanguages) {
    var storeForNew = Optional.ofNullable(storeForNewLanguages)
        .or(entryToUpdate::primaryEditableStore)
        .or(this::primaryEditableStore)
        .map(TranslationManager::toEditableStore)
        .orElseThrow();
    var changedLanguagesByStore = changedLanguages(newTranslation, entryToUpdate)
        .collect(groupingBy(changedLang -> entryToUpdate.entry(changedLang)
            .map(ITranslationEntry::store)
            .filter(ITranslationStore::isEditable)
            .map(TranslationManager::toEditableStore)
            .orElse(storeForNew)));
    changedLanguagesByStore.entrySet().stream()
        .map(e -> writeTranslationToStore(e.getKey(), e.getValue(), newTranslation))
        .forEach(entryToUpdate::entryUpdated);
    return createUpdateTranslationEvent(this, entryToUpdate);
  }

  protected ITranslationEntry writeTranslationToStore(IEditableTranslationStore store, Iterable<Language> languagesToUpdate, ITranslation newTranslation) {
    var key = newTranslation.key();
    var updateForStore = store.get(key)
        .map(Translation::new)
        .orElseGet(() -> new Translation(key));
    languagesToUpdate.forEach(changedLang -> updateForStore.putText(changedLang, newTranslation.text(changedLang).orElse(null)));
    return editStoreObservingLanguages(store, s -> s.setTranslation(updateForStore));
  }

  protected static Stream<Language> changedLanguages(ITranslation a, ITranslation b) {
    return Stream.concat(languagesDifferentInFirst(a, b), languagesDifferentInFirst(b, a))
        .distinct();
  }

  protected static Stream<Language> languagesDifferentInFirst(ITranslation a, ITranslation b) {
    return a.texts().entrySet().stream()
        .filter(e -> !Objects.equals(textOrNull(b, e.getKey()), textOrNull(e.getValue())))
        .map(Entry::getKey);
  }

  protected static String textOrNull(String s) {
    return Strings.notBlank(s).orElse(null);
  }

  protected static String textOrNull(ITranslation translation, Language language) {
    return translation.text(language).filter(Strings::hasText).orElse(null);
  }

  protected <T> T editStoreObservingLanguages(IEditableTranslationStore storeToObserve, Function<IEditableTranslationStore, T> task) {
    var oldLanguages = storeToObserve.languages().collect(toSet());
    try {
      return task.apply(storeToObserve);
    }
    finally {
      fireAddLanguageForCreatedLanguages(storeToObserve, oldLanguages);
    }
  }

  protected void fireAddLanguageForCreatedLanguages(ITranslationStore storeToObserve, Collection<Language> oldLanguages) {
    storeToObserve.languages()
        .filter(newLang -> !oldLanguages.contains(newLang))
        .map(lang -> createAddLanguageEvent(this, lang))
        .forEach(this::fireManagerChanged);
  }

  /**
   * Adds a new {@link Language} to an {@link ITranslationStore}.
   *
   * @param lang
   *          The language to add. Must not be {@code null}.
   * @param target
   *          The target store. Must be an editable store (not {@code null}).
   * @throws IllegalArgumentException
   *           if the target store is not editable
   */
  public void addNewLanguage(Language lang, ITranslationStore target) {
    if (target.languages().anyMatch(isEqual(lang))) {
      return; // nothing to do
    }

    Ensure.notNull(lang, "Language cannot be null.");
    Ensure.isTrue(m_stores.contains(target), "Store of wrong manager.");
    var editableStore = toEditableStore(target);
    runAndFireChanged(() -> {
      editableStore.addNewLanguage(lang);
      return createAddLanguageEvent(this, lang);
    });
  }

  /**
   * Gets all {@link IStackedTranslation} instances in this manager.
   *
   * @return A {@link Stream} with all {@link IStackedTranslation} instances.
   */
  public Stream<? extends IStackedTranslation> allTranslations() {
    return m_translations
        .values().stream();
  }

  /**
   * @return {@code true} if at least one editable store is dirty. {@code false} otherwise.
   */
  public boolean isDirty() {
    return allEditableStores()
        .map(TranslationManager::toEditableStore)
        .anyMatch(IEditableTranslationStore::isDirty);
  }

  protected static IEditableTranslationStore toEditableStore(ITranslationStore store) {
    Ensure.instanceOf(store, IEditableTranslationStore.class, "Editable stores must implement {}.", IEditableTranslationStore.class);
    Ensure.isTrue(store.isEditable(), "Cannot modify store '{}' because it is read-only.", store.service().type().name());
    return (IEditableTranslationStore) store;
  }

  /**
   * Flushes all editable {@link ITranslationStore}s to their data source.
   *
   * @param env
   *          The {@link IEnvironment} to store the new content.
   * @param progress
   *          The {@link IProgress} monitor.
   */
  public void flush(IEnvironment env, IProgress progress) {
    runAndFireChanged(() -> {
      allEditableStores()
          .map(TranslationManager::toEditableStore)
          .forEach(store -> store.flush(env, progress));
      return createFlushEvent(this);
    });
  }

  /**
   * Reloads all {@link ITranslationStore}s from their data source. All modifications already applied to the stores are
   * discarded.
   *
   * @param progress
   *          The {@link IProgress} monitor.
   */
  public void reload(IProgress progress) {
    runAndFireChanged(() -> {
      progress.init(m_stores.size(), "Reload all translation stores.");
      allStores().forEach(s -> s.reload(progress.newChild(1)));
      m_translations.clear();
      m_translations.putAll(buildStackedTranslations(m_stores.stream()));
      return createReloadEvent(this);
    });
  }

  /**
   * @return All {@link ITranslationStore}s of this manager ordered by the text provider service order (see
   *         {@link TextProviderService#order()}).
   */
  public Stream<ITranslationStore> allStores() {
    return m_stores.stream();
  }

  /**
   * @return All {@link Language}s of this manager.
   */
  public Stream<Language> allLanguages() {
    return allStores()
        .flatMap(ITranslationStore::languages)
        .distinct();
  }

  /**
   * Marks the manager as changing. As long as the manager is changing no events are fired. Instead, the events are
   * buffered and fired only once as soon as the manager is no longer changing. This allows to apply a lot of batch
   * changes and only notify listeners once at the end of the batch.
   *
   * @param changing
   *          {@code true} if it is changing, {@code false} otherwise.
   */
  public void setChanging(boolean changing) {
    if (changing) {
      m_changing++;
    }
    else {
      m_changing--;
      if (!isChanging()) {
        fireBufferedEvents();
      }
    }
  }

  /**
   * @return {@code true} if the manager is in changing mode (see {@link #setChanging(boolean)}). {@code false}
   *         otherwise.
   */
  public boolean isChanging() {
    return m_changing > 0;
  }

  protected void runAndFireChanged(Supplier<TranslationManagerEvent> callable) {
    setChanging(true);
    try {
      fireManagerChanged(callable.get());
    }
    finally {
      setChanging(false);
    }
  }

  /**
   * Adds a {@link ITranslationManagerListener} to this manager.
   *
   * @param listener
   *          The listener to add. Must not be {@code null}.
   */
  public void addListener(@SuppressWarnings("TypeMayBeWeakened") ITranslationManagerListener listener) {
    m_listeners.add(listener);
  }

  /**
   * Removes the specified {@link ITranslationManagerListener} from this manager.
   *
   * @param listener
   *          The listener to remove.
   * @return {@code true} if the listener was removed. {@code false} if it could not be found.
   */
  public boolean removeListener(@SuppressWarnings("TypeMayBeWeakened") ITranslationManagerListener listener) {
    return m_listeners.remove(listener);
  }

  protected synchronized void fireBufferedEvents() {
    if (m_eventBuffer.isEmpty()) {
      return;
    }

    try {
      if (m_listeners.isEmpty()) {
        return;
      }

      // copy buffer because if a listener evaluates its events (the stream) asynchronously,
      // the underlying collection might already have been cleared in the finally-block. Then the stream is empty.
      fireManagerChanged(new ArrayList<>(m_eventBuffer));
    }
    finally {
      m_eventBuffer.clear();
    }
  }

  protected void fireManagerChanged(Collection<TranslationManagerEvent> eventsToFire) {
    var listeners = m_listeners.get(ITranslationManagerListener.class);
    for (var l : listeners) {
      l.managerChanged(eventsToFire.stream());
    }
  }

  protected synchronized void fireManagerChanged(TranslationManagerEvent event) {
    if (event == null) {
      return;
    }
    if (isChanging()) {
      m_eventBuffer.add(event);
      return;
    }
    fireManagerChanged(singleton(event));
  }

  @Override
  @SuppressWarnings("HardcodedLineSeparator")
  public String toString() {
    return m_stores.stream()
        .map(s -> String.valueOf(s) + '\n')
        .collect(joining("", TranslationManager.class.getSimpleName() + " [\n", "]"));
  }
}
