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

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import org.eclipse.scout.sdk.core.s.nls.ITranslationManagerListener;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link TranslationManagerEvent}</h3>
 * <p>
 * Describes an event of a {@link TranslationManager}.
 * <p>
 * Use {@link TranslationManager#addListener(ITranslationManagerListener)} to register an observer.
 *
 * @since 7.0.0
 */
public final class TranslationManagerEvent {

  /**
   * Event type describing a new translation with a new key.
   */
  public static final int TYPE_NEW_TRANSLATION = 1 << 1;
  /**
   * Event type describing that a translation has been removed.
   */
  public static final int TYPE_REMOVE_TRANSLATION = 1 << 2;
  /**
   * Event type describing that a translation changed its values. This can also be a new text for a {@link Language}.
   */
  public static final int TYPE_UPDATE_TRANSLATION = 1 << 3;
  /**
   * Event type describing that the key of an entry has changed.
   */
  public static final int TYPE_KEY_CHANGED = 1 << 4;
  /**
   * Event type describing that a new language has been added.
   */
  public static final int TYPE_NEW_LANGUAGE = 1 << 5;
  /**
   * Event type describing that all changes of the manager have been flushed to the {@link ITranslationStore}s.
   */
  public static final int TYPE_FLUSH = 1 << 6;
  /**
   * Event type describing that the content of the manager has been reloaded from the {@link ITranslationStore}s. All
   * changes applied are discarded.
   */
  public static final int TYPE_RELOAD = 1 << 7;

  private final TranslationManager m_manager;
  private final int m_type;
  private final IStackedTranslation m_entry;
  private final String m_key;
  private final Language m_language;

  private TranslationManagerEvent(TranslationManager source, int type, IStackedTranslation entry, String key) {
    m_type = type;
    m_manager = source;
    m_entry = entry;
    m_key = key;
    m_language = null;
  }

  private TranslationManagerEvent(TranslationManager source, int type, Language lang) {
    m_type = type;
    m_manager = source;
    m_language = lang;
    m_entry = null;
    m_key = null;
  }

  public static TranslationManagerEvent createChangeKeyEvent(TranslationManager source, IStackedTranslation newEntry, String oldKey) {
    return new TranslationManagerEvent(source, TYPE_KEY_CHANGED, newEntry, oldKey);
  }

  public static TranslationManagerEvent createRemoveTranslationEvent(TranslationManager source, IStackedTranslation removedEntry) {
    return new TranslationManagerEvent(source, TYPE_REMOVE_TRANSLATION, removedEntry, removedEntry.key());
  }

  public static TranslationManagerEvent createUpdateTranslationEvent(TranslationManager source, IStackedTranslation updatedEntry) {
    return new TranslationManagerEvent(source, TYPE_UPDATE_TRANSLATION, updatedEntry, updatedEntry.key());
  }

  public static TranslationManagerEvent createAddTranslationEvent(TranslationManager source, IStackedTranslation newEntry) {
    return new TranslationManagerEvent(source, TYPE_NEW_TRANSLATION, newEntry, newEntry.key());
  }

  public static TranslationManagerEvent createAddLanguageEvent(TranslationManager source, Language newLanguage) {
    return new TranslationManagerEvent(source, TYPE_NEW_LANGUAGE, newLanguage);
  }

  public static TranslationManagerEvent createFlushEvent(TranslationManager source) {
    return new TranslationManagerEvent(source, TYPE_FLUSH, null);
  }

  public static TranslationManagerEvent createReloadEvent(TranslationManager source) {
    return new TranslationManagerEvent(source, TYPE_RELOAD, null);
  }

  /**
   * @return The {@link TranslationManager} that is the origin of the event.
   */
  public TranslationManager source() {
    return m_manager;
  }

  /**
   * @return The type of the event. Is one of the following:
   *         <ul>
   *         <li>{@link #TYPE_NEW_TRANSLATION}</li>
   *         <li>{@link #TYPE_REMOVE_TRANSLATION}</li>
   *         <li>{@link #TYPE_UPDATE_TRANSLATION}</li>
   *         <li>{@link #TYPE_KEY_CHANGED}</li>
   *         <li>{@link #TYPE_NEW_LANGUAGE}</li>
   *         <li>{@link #TYPE_FLUSH}</li>
   *         <li>{@link #TYPE_RELOAD}</li>
   *         </ul>
   */
  public int type() {
    return m_type;
  }

  /**
   * @return
   *         <ul>
   *         <li>For {@link #TYPE_KEY_CHANGED}: The {@link IStackedTranslation} that changed. This entry already returns
   *         the new key. The old key can be obtained using {@link #key()}.</li>
   *         <li>For {@link #TYPE_REMOVE_TRANSLATION}: The {@link IStackedTranslation} that has been removed.</li>
   *         <li>For {@link #TYPE_UPDATE_TRANSLATION}: The {@link IStackedTranslation} that has been updated.</li>
   *         <li>For {@link #TYPE_NEW_TRANSLATION}: The new {@link IStackedTranslation} that was added.</li>
   *         <li>For all other events: An empty {@link Optional}</li>
   *         </ul>
   */
  public Optional<IStackedTranslation> translation() {
    return Optional.ofNullable(m_entry);
  }

  /**
   * @return
   *         <ul>
   *         <li>For {@link #TYPE_KEY_CHANGED}: The old key of the entry. The modified {@link IStackedTranslation}
   *         holding the new key can be obtained using {@link #translation()}.</li>
   *         <li>For {@link #TYPE_REMOVE_TRANSLATION}: The key of the {@link IStackedTranslation} that has been
   *         removed.</li>
   *         <li>For {@link #TYPE_UPDATE_TRANSLATION}: The key of the {@link IStackedTranslation} that has been
   *         updated.</li>
   *         <li>For {@link #TYPE_NEW_TRANSLATION}: The key of the new {@link IStackedTranslation} that was added.</li>
   *         <li>For all other events: An empty {@link Optional}</li>
   *         </ul>
   */
  public Optional<String> key() {
    return Optional.ofNullable(m_key);
  }

  /**
   * @return The {@link Language} that was added if the {@link #type()} is {@link #TYPE_NEW_LANGUAGE}. An empty
   *         {@link Optional} otherwise.
   */
  public Optional<Language> language() {
    return Optional.ofNullable(m_language);
  }

  static String typeName(int type) {
    switch (type) {
      case TYPE_NEW_TRANSLATION:
        return "NewTranslation";
      case TYPE_KEY_CHANGED:
        return "KeyChange";
      case TYPE_REMOVE_TRANSLATION:
        return "RemoveTranslation";
      case TYPE_NEW_LANGUAGE:
        return "NewLanguage";
      case TYPE_RELOAD:
        return "Reload";
      case TYPE_UPDATE_TRANSLATION:
        return "UpdateTranslation";
      case TYPE_FLUSH:
        return "Flush";
      default:
        throw newFail("Unknown event type '{}'.", type);
    }
  }

  @Override
  public String toString() {
    var joiner = new StringJoiner(", ", TranslationManagerEvent.class.getSimpleName() + " [", "]")
        .add("type=" + typeName(m_type));
    if (Strings.hasText(m_key)) {
      joiner.add("key=" + m_key);
    }
    if (m_type == TYPE_KEY_CHANGED && m_entry != null) {
      joiner.add("newKey=" + m_entry.key());
    }
    if (m_language != null) {
      joiner.add("language=" + m_language);
    }
    return joiner.toString();
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = 1;
    result = prime * result + ((m_entry == null) ? 0 : m_entry.hashCode());
    result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
    result = prime * result + ((m_language == null) ? 0 : m_language.hashCode());
    result = prime * result + m_manager.hashCode();
    result = prime * result + m_type;
    return result;
  }

  @Override
  @SuppressWarnings("squid:S1067")
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    var other = (TranslationManagerEvent) obj;
    return m_type == other.m_type
        && Objects.equals(m_key, other.m_key)
        && Objects.equals(m_entry, other.m_entry)
        && Objects.equals(m_language, other.m_language)
        && Objects.equals(m_manager, other.m_manager);
  }
}
