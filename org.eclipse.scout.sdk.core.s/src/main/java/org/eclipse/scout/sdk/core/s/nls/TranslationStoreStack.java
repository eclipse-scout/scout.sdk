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
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.function.Predicate.isEqual;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStoreStackEvent.*;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.EventListenerList;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link TranslationStoreStack}</h3>
 * <p>
 * Represents a stack of {@link ITranslationStore}s as available at runtime. In the Scout runtime such a stack is
 * represented by all {@code org.eclipse.scout.rt.shared.services.common.text.ITextProviderService}s available.
 * <p>
 * Use {@link #create(Path, IEnvironment, IProgress)} to create a {@link TranslationStoreStack} for a specific
 * {@link Path}.
 *
 * @since 7.0.0
 */
public class TranslationStoreStack {

  @SuppressWarnings("PublicStaticCollectionField")
  public static final List<ITranslationStoreSupplier> SUPPLIERS = new CopyOnWriteArrayList<>();

  private final Deque<ITranslationStore> m_stores;
  private final EventListenerList m_listeners;
  private final List<TranslationStoreStackEvent> m_eventBuffer;
  private int m_changing;

  protected TranslationStoreStack(Collection<ITranslationStore> stores) {
    List<ITranslationStore> l = new ArrayList<>(stores);
    P_TranslationStoreComparator comparator = new P_TranslationStoreComparator();
    l.sort(comparator);
    if (!comparator.duplicateOrders().isEmpty()) {
      SdkLog.warning("There are TextProviderServices with the same @Order value: {}", comparator.duplicateOrders());
    }
    m_eventBuffer = new ArrayList<>();
    m_listeners = new EventListenerList();
    m_stores = new ArrayDeque<>(l);
  }

  /**
   * Calls all {@link ITranslationStoreSupplier}s to provide {@link ITranslationStore}s for the specified path and
   * collects them in a new {@link TranslationStoreStack}.
   *
   * @param file
   *          The {@link Path} for which all {@link ITranslationStore}s should be collected into a new
   *          {@link TranslationStoreStack}.
   * @param env
   *          The {@link IEnvironment} of the request. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor. Must not be {@code null}.
   * @return A new {@link TranslationStoreStack} for the specified file {@link Path}.
   * @see ITranslationStoreSupplier
   */
  public static Optional<TranslationStoreStack> create(Path file, IEnvironment env, IProgress progress) {
    Ensure.notNull(file);
    return createStack(findAllTranslationStores(file, env, progress));
  }

  private static Optional<TranslationStoreStack> createStack(Collection<ITranslationStore> stores) {
    if (stores.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new TranslationStoreStack(stores));
  }

  private static Collection<ITranslationStore> findAllTranslationStores(Path file, IEnvironment env, IProgress progress) {
    Ensure.notNull(env);
    Ensure.notNull(progress);

    int ticksBySupplier = 1000;
    progress.init("Search for translation stores for " + Ensure.notNull(file), SUPPLIERS.size() * ticksBySupplier);

    List<ITranslationStore> unsortedStores = new ArrayList<>();
    for (ITranslationStoreSupplier supplier : SUPPLIERS) {
      Stream<? extends ITranslationStore> stores = supplier.get(file, env, progress.newChild(ticksBySupplier));
      if (stores != null) {
        stores
            .filter(TranslationStoreStack::isContentAvailable)
            .forEach(unsortedStores::add);
      }
    }
    return unsortedStores;
  }

  private static boolean isContentAvailable(ITranslationStore s) {
    if (s == null) {
      return false;
    }

    if (!s.languages().findAny().isPresent()) {
      SdkLog.warning("{} contains no languages! Please check the configuration.", s);
      return false;
    }

    if (s.languages().noneMatch(l -> l == Language.LANGUAGE_DEFAULT)) {
      SdkLog.warning("{} does not contain a default language!", s);
      return false;
    }

    return true;
  }

  /**
   * Gets a {@link Stream} returning all {@link ITranslationEntry} instances whose key starts with the specified prefix.
   *
   * @param prefix
   *          The prefix. Is evaluated case-insensitive. Must not be {@code null}.
   * @return A {@link Stream} with all entries with keys starting with specified prefix.
   */
  public Stream<? extends ITranslationEntry> allWithPrefix(String prefix) {
    return allEntries()
        .filter(t -> t.key().regionMatches(true, 0, prefix, 0, prefix.length()));
  }

  /**
   * Generates a new unused key using the specified text as base.
   *
   * @param baseText
   *          The base text. From this the new key is derived.
   * @return A new valid key or an empty {@link String} if the input text is empty or {@code null}.
   */
  public String generateNewKey(String baseText) {
    return generateKey(baseText, true);
  }

  /**
   * @return If this stack is editable. A {@link TranslationStoreStack} is considered to be editable if at least one
   *         {@link ITranslationStore} is editable.
   */
  public boolean isEditable() {
    return primaryEditableStore().isPresent();
  }

  /**
   * Gets the {@link ITranslationEntry} for the specified key.
   *
   * @param key
   *          The key to search.
   * @return The {@link ITranslationEntry} of the first store that contains the specified key or an empty
   *         {@link Optional} if the key cannot be found in any {@link ITranslationStore}.
   */
  public Optional<? extends ITranslationEntry> translation(String key) {
    return allStores()
        .flatMap(ITranslationStore::entries)
        .filter(e -> e.key().equals(key))
        .findAny();
  }

  /**
   * Checks if at least one {@link ITranslationStore} of this stack contains the specified key.
   *
   * @param key
   *          The key to search.
   * @return {@code true} if at least one store contains the specified key.
   */
  public synchronized boolean containsKey(String key) {
    return allStores()
        .anyMatch(store -> store.containsKey(key));
  }

  /**
   * @return All {@link ITranslationStore}s that can be modified.
   */
  public Stream<ITranslationStore> allEditableStores() {
    return allStores()
        .filter(ITranslationStore::isEditable);
  }

  protected Stream<IEditableTranslationStore> allEditableStoresInternal() {
    return allEditableStores()
        .map(TranslationStoreStack::toEditableStore);
  }

  /**
   * @return The first (primary) {@link ITranslationStore} that is editable.
   */
  public Optional<ITranslationStore> primaryEditableStore() {
    return allEditableStores()
        .findAny();
  }

  /**
   * Adds the specified {@link ITranslation} to the {@link #primaryEditableStore()}. For more details see
   * {@link #addNewTranslation(ITranslation, ITranslationStore)}
   *
   * @param newTranslation
   *          The new {@link ITranslation} to add.
   */
  public void addNewTranslation(ITranslation newTranslation) {
    addNewTranslation(newTranslation, null);
  }

  /**
   * Adds the specified {@link ITranslation} to the specified {@link ITranslationStore}.
   * <p>
   * The following conditions must be fulfilled that the operation can be completed:
   * <ul>
   * <li>The specified {@link ITranslation} must contain a valid key.</li>
   * <li>The specified {@link ITranslation} must contain a text for the {@link Language#LANGUAGE_DEFAULT}.</li>
   * <li>The specified store must be editable.</li>
   * <li>If the specified store is {@code null}, the {@link #primaryEditableStore()} must be available.</li>
   * <li>The specified store must not already contain an entry with the specified key.</li>
   * <li>The specified store must be part of this stack.</li>
   * </ul>
   *
   * @param newTranslation
   *          The translation to add.
   * @param target
   *          The target {@link ITranslationStore}. May be {@code null}. In that case the
   *          {@link #primaryEditableStore()} is used.
   * @throws IllegalArgumentException
   *           if one of the mentioned condition is not fulfilled.
   */
  public synchronized void addNewTranslation(ITranslation newTranslation, ITranslationStore target) {
    IEditableTranslationStore editableStore = toEditableStore(
        Optional.ofNullable(target)
            .orElseGet(() -> primaryEditableStore()
                .orElseThrow(() -> newFail("Cannot create new entries. All translation stores are read-only."))));

    // validate input
    validateTranslation(newTranslation);
    Ensure.isTrue(!editableStore.containsKey(newTranslation.key()), "Key '{}' already exists in store {}", newTranslation.key(), editableStore);
    Ensure.isTrue(m_stores.contains(editableStore), "Store of wrong stack.");

    // create new text
    runAndFireChanged(() -> createAddTranslationEvent(this, editableStore.addNewTranslation(newTranslation)));
  }

  /**
   * Changes the specified key to a new value.
   *
   * @param oldKey
   *          The old key of the existing entry to modify. Must not be blank.
   * @param newKey
   *          The new key of that entry. Must not be blank.
   * @throws IllegalArgumentException
   *           if the new key already exists in this stack.
   */
  public synchronized void changeKey(String oldKey, String newKey) {
    Ensure.notBlank(oldKey, "Cannot change a blank key.");
    Ensure.notBlank(newKey, "Cannot update to a blank key.");
    Ensure.isTrue(!translation(newKey).isPresent(), "Cannot change key '{}' to '{}' because the new key already exists.", oldKey, newKey);

    setChanging(true);
    try {
      firstEditableStoreWithKey(oldKey)
          .ifPresent(store -> {
            // update key in first store
            fireStackChanged(createChangeKeyEvent(this, store.changeKey(oldKey, newKey), oldKey));

            // if the old key is still present: we changed an overridden translation key. So it is no longer overridden now.
            // create a synthetic event describing that a new one has been added (the one that is no longer overridden).
            allStores()
                .filter(s -> s.containsKey(oldKey))
                .findAny()
                .flatMap(s -> s.get(oldKey))
                .ifPresent(entry -> fireStackChanged(createAddTranslationEvent(this, entry)));

          });
    }
    finally {
      setChanging(false);
    }
  }

  protected Optional<IEditableTranslationStore> firstEditableStoreWithKey(String key) {
    return allEditableStores()
        .filter(s -> s.containsKey(key))
        .findAny()
        .map(TranslationStoreStack::toEditableStore);
  }

  /**
   * Removes all entries with the specified keys.
   *
   * @param keys
   *          The keys of the entries to remove. Must not be {@code null}.
   */
  public synchronized void removeTranslations(Stream<String> keys) {
    Ensure.notNull(keys)
        .filter(Strings::hasText)
        .forEach(key -> firstEditableStoreWithKey(key)
            .ifPresent(s -> runAndFireChanged(() -> createRemoveTranslationEvent(this, s.removeTranslation(key)))));
  }

  protected static void validateTranslation(ITranslation translation) {
    Ensure.notNull(translation, "A translation must be specified.");
    Ensure.notBlank(translation.key(), "Key must be specified.");
    Ensure.isTrue(!Strings.isEmpty(translation.translation(Language.LANGUAGE_DEFAULT).orElse(null)), "Default language translation must be specified.");
  }

  /**
   * Replaces the existing {@link ITranslation} (that has the same key as the specified one) with the specified
   * {@link ITranslation}.
   *
   * @param newEntry
   *          The new entry.
   * @throws IllegalArgumentException
   *           if the specified {@link ITranslation} is {@code null}, has no valid key or does not contain a text for
   *           the {@link Language#LANGUAGE_DEFAULT}.
   */
  public synchronized void updateTranslation(ITranslation newEntry) {
    validateTranslation(newEntry);
    firstEditableStoreWithKey(newEntry.key())
        .ifPresent(store -> runAndFireChanged(() -> createUpdateTranslationEvent(this, store.updateTranslation(newEntry))));
  }

  /**
   * Adds a new {@link Language} to an {@link ITranslationStore}.
   *
   * @param lang
   *          The language to add. Must not be {@code null}.
   * @param target
   *          The target store. Must be an editable STORE that does not yet contain the specified {@link Language}.
   * @throws IllegalArgumentException
   *           if one of the conditions is not fulfilled.
   */
  public synchronized void addNewLanguage(Language lang, ITranslationStore target) {
    IEditableTranslationStore editableStore = toEditableStore(target);

    Ensure.notNull(lang, "Language cannot be null.");
    Ensure.isTrue(editableStore.languages().noneMatch(isEqual(lang)), "Store '{}' already contains language '{}'.", target.service().type().name(), lang);
    Ensure.isTrue(m_stores.contains(editableStore), "Store of wrong stack.");

    runAndFireChanged(() -> {
      editableStore.addNewLanguage(lang);
      return createAddLanguageEvent(this, lang);
    });
  }

  /**
   * Gets all {@link ITranslationEntry} instances in this stack. Please note that if several {@link ITranslationStore}s
   * contain values for the same keys, only the first ones (according to the text provider service order) are returned.
   *
   * @return A {@link Stream} with all not overridden {@link ITranslationEntry} instances.
   */
  public synchronized Stream<ITranslationEntry> allEntries() {
    Map<String, ITranslationEntry> allEntries = new HashMap<>();
    Iterator<ITranslationStore> storesHighestOrderFirst = m_stores.descendingIterator();
    while (storesHighestOrderFirst.hasNext()) {
      ITranslationStore store = storesHighestOrderFirst.next();
      store.entries().forEach(entry -> allEntries.put(entry.key(), entry));
    }
    return allEntries.values().stream();
  }

  /**
   * @return {@code true} if at least one editable store is dirty. {@code false} otherwise.
   */
  public synchronized boolean isDirty() {
    return allEditableStoresInternal()
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
  public synchronized void flush(IEnvironment env, IProgress progress) {
    runAndFireChanged(() -> {
      allEditableStores().forEach(store -> toEditableStore(store).flush(env, progress));
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
  public synchronized void reload(IProgress progress) {
    runAndFireChanged(() -> {
      progress.init("Reload all translation stores.", m_stores.size());
      allStores().forEach(s -> s.reload(progress.newChild(1)));
      return createReloadEvent(this);
    });
  }

  /**
   * @return All {@link ITranslationStore}s of this stack.
   */
  public Stream<ITranslationStore> allStores() {
    return m_stores.stream();
  }

  /**
   * @return The {@link Language}s of all editable {@link ITranslationStore}s.
   */
  public Stream<Language> allEditableLanguages() {
    return allEditableStores()
        .flatMap(ITranslationStore::languages)
        .distinct();
  }

  /**
   * @return All {@link Language}s of this stack.
   */
  public Stream<Language> allLanguages() {
    return allStores()
        .flatMap(ITranslationStore::languages)
        .distinct();
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected String generateKey(String baseText, boolean appendFreeNumSuffix) {
    if (Strings.isEmpty(baseText)) {
      return "";
    }

    StringBuilder ret = new StringBuilder(baseText.length());

    // remove not allowed characters
    baseText = Pattern.compile("[^a-zA-Z0-9_.\\- ]*").matcher(baseText).replaceAll("").trim();

    // camel case multiple words
    String[] split = baseText.split(" ");
    for (String splitValue : split) {
      if (!splitValue.isEmpty()) {
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
      while (containsKey(result)) {
        result = newKey + i;
        i++;
      }
    }
    return result;
  }

  /**
   * Marks the stack that it is changing. As long as the stack is changing no events are fired. Instead the events are
   * buffered and fired only once as soon as the stack is no longer changing. This allows to apply a lot of batch
   * changes and only notify listeners once at the end of the batch.
   *
   * @param changing
   *          {@code true} if it is changing, {@code false} otherwise.
   */
  public synchronized void setChanging(boolean changing) {
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
   * @return {@code true} if the stack is in changing mode (see {@link #setChanging(boolean)}). {@code false} otherwise.
   */
  public synchronized boolean isChanging() {
    return m_changing > 0;
  }

  protected void runAndFireChanged(Supplier<TranslationStoreStackEvent> callable) {
    setChanging(true);
    try {
      fireStackChanged(callable.get());
    }
    finally {
      setChanging(false);
    }
  }

  /**
   * Adds a {@link ITranslationStoreStackListener} to this stack.
   *
   * @param listener
   *          The listener to add. Must not be {@code null}.
   */
  public void addListener(@SuppressWarnings("TypeMayBeWeakened") ITranslationStoreStackListener listener) {
    m_listeners.add(listener);
  }

  /**
   * Removes the specified {@link ITranslationStoreStackListener} from this stack.
   *
   * @param listener
   *          The listener to remove.
   * @return {@code true} if the listener was removed. {@code false} if it could not be found.
   */
  public boolean removeListener(@SuppressWarnings("TypeMayBeWeakened") ITranslationStoreStackListener listener) {
    return m_listeners.remove(listener);
  }

  protected void fireBufferedEvents() {
    if (m_eventBuffer.isEmpty()) {
      return;
    }

    try {
      if (m_listeners.isEmpty()) {
        return;
      }

      Collection<ITranslationStoreStackListener> listeners = m_listeners.get(ITranslationStoreStackListener.class);
      Collection<TranslationStoreStackEvent> events = new ArrayList<>(m_eventBuffer); // copy buffer so that async operating listeners do not work on the cleared version (see finally).
      for (ITranslationStoreStackListener l : listeners) {
        l.stackChanged(events.stream());
      }
    }
    finally {
      m_eventBuffer.clear();
    }
  }

  protected void fireStackChanged(TranslationStoreStackEvent event) {
    if (isChanging()) {
      m_eventBuffer.add(event);
      return;
    }

    Collection<ITranslationStoreStackListener> listeners = m_listeners.get(ITranslationStoreStackListener.class);
    for (ITranslationStoreStackListener l : listeners) {
      l.stackChanged(Stream.of(event));
    }
  }

  @SuppressWarnings({"squid:S2063", "ComparatorNotSerializable"}) // Comparators should be "Serializable". Not necessary here because translation stores are not.
  private static final class P_TranslationStoreComparator implements Comparator<ITranslationStore> {

    private final Set<String> m_duplicateOrders;

    private P_TranslationStoreComparator() {
      m_duplicateOrders = new HashSet<>();
    }

    @Override
    public int compare(ITranslationStore o1, ITranslationStore o2) {
      if (o1 == o2) {
        return 0;
      }

      int compare = Double.compare(o1.service().order(), o2.service().order());
      if (compare != 0) {
        return compare;
      }

      String o1Fqn = o1.service().type().name();
      String o2Fqn = o2.service().type().name();
      rememberDuplicateOrder(o1Fqn, o2Fqn);

      compare = Boolean.compare(o2.isEditable(), o1.isEditable());
      if (compare != 0) {
        // prefer source types
        return compare;
      }
      return o1Fqn.compareTo(o2Fqn);
    }

    private Set<String> duplicateOrders() {
      return m_duplicateOrders;
    }

    private void rememberDuplicateOrder(String o1Fqn, String o2Fqn) {
      String[] duplicateOrdersFqn = {o1Fqn, o2Fqn};
      Arrays.sort(duplicateOrdersFqn);
      m_duplicateOrders.add(Arrays.toString(duplicateOrdersFqn));
    }
  }

  @Override
  @SuppressWarnings("HardcodedLineSeparator")
  public String toString() {
    StringBuilder builder = new StringBuilder(TranslationStoreStack.class.getSimpleName());
    builder.append(" [\n");
    for (ITranslationStore s : m_stores) {
      builder.append(s).append('\n');
    }
    builder.append(']');
    return builder.toString();
  }
}
