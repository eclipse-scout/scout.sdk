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

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link TranslationStoreStackEvent}</h3>
 * <p>
 * Describes an event of a {@link TranslationStoreStack}.
 * <p>
 * Use {@link TranslationStoreStack#addListener(ITranslationStoreStackListener)} to register an observer.
 *
 * @since 7.0.0
 */
public final class TranslationStoreStackEvent {

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
   * Event type describing that all changes of the stack have been flushed to the {@link ITranslationStore}s.
   */
  public static final int TYPE_FLUSH = 1 << 6;
  /**
   * Event type describing that the content of the stack has been reloaded from the {@link ITranslationStore}s. All
   * changes applied are discarded.
   */
  public static final int TYPE_RELOAD = 1 << 7;

  private final TranslationStoreStack m_stack;
  private final int m_type;
  private final ITranslationEntry m_entry;
  private final String m_key;
  private final Language m_language;

  private TranslationStoreStackEvent(TranslationStoreStack source, int type, ITranslationEntry entry, String key) {
    m_type = type;
    m_stack = source;
    m_entry = entry;
    m_key = key;
    m_language = null;
  }

  private TranslationStoreStackEvent(TranslationStoreStack source, int type, Language lang) {
    m_type = type;
    m_stack = source;
    m_language = lang;
    m_entry = null;
    m_key = null;
  }

  public static TranslationStoreStackEvent createChangeKeyEvent(TranslationStoreStack source, ITranslationEntry newEntry, String oldKey) {
    return new TranslationStoreStackEvent(source, TYPE_KEY_CHANGED, newEntry, oldKey);
  }

  public static TranslationStoreStackEvent createRemoveTranslationEvent(TranslationStoreStack source, ITranslationEntry removedEntry) {
    return new TranslationStoreStackEvent(source, TYPE_REMOVE_TRANSLATION, removedEntry, removedEntry.key());
  }

  public static TranslationStoreStackEvent createUpdateTranslationEvent(TranslationStoreStack source, ITranslationEntry updatedEntry) {
    return new TranslationStoreStackEvent(source, TYPE_UPDATE_TRANSLATION, updatedEntry, updatedEntry.key());
  }

  public static TranslationStoreStackEvent createAddTranslationEvent(TranslationStoreStack source, ITranslationEntry newEntry) {
    return new TranslationStoreStackEvent(source, TYPE_NEW_TRANSLATION, newEntry, newEntry.key());
  }

  public static TranslationStoreStackEvent createAddLanguageEvent(TranslationStoreStack source, Language newLanguage) {
    return new TranslationStoreStackEvent(source, TYPE_NEW_LANGUAGE, newLanguage);
  }

  public static TranslationStoreStackEvent createFlushEvent(TranslationStoreStack source) {
    return new TranslationStoreStackEvent(source, TYPE_FLUSH, null);
  }

  public static TranslationStoreStackEvent createReloadEvent(TranslationStoreStack source) {
    return new TranslationStoreStackEvent(source, TYPE_RELOAD, null);
  }

  /**
   * @return The {@link TranslationStoreStack} that is the origin of the event.
   */
  public TranslationStoreStack source() {
    return m_stack;
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
   *         <li>For {@link #TYPE_KEY_CHANGED}: The {@link ITranslationEntry} that changed. This entry already returns
   *         the new key. The old key can be obtained using {@link #key()}.</li>
   *         <li>For {@link #TYPE_REMOVE_TRANSLATION}: The {@link ITranslationEntry} that has been removed.</li>
   *         <li>For {@link #TYPE_UPDATE_TRANSLATION}: The {@link ITranslationEntry} that has been updated.</li>
   *         <li>For {@link #TYPE_NEW_TRANSLATION}: The new {@link ITranslationEntry} that was added.</li>
   *         <li>For all other events: An empty {@link Optional}</li>
   *         </ul>
   */
  public Optional<ITranslationEntry> entry() {
    return Optional.ofNullable(m_entry);
  }

  /**
   * @return
   *         <ul>
   *         <li>For {@link #TYPE_KEY_CHANGED}: The old key of the entry. The modified {@link ITranslationEntry} holding
   *         the new key can be obtained using {@link #entry()}.</li>
   *         <li>For {@link #TYPE_REMOVE_TRANSLATION}: The key of the {@link ITranslationEntry} that has been
   *         removed.</li>
   *         <li>For {@link #TYPE_UPDATE_TRANSLATION}: The key of the {@link ITranslationEntry} that has been
   *         updated.</li>
   *         <li>For {@link #TYPE_NEW_TRANSLATION}: The key of the new {@link ITranslationEntry} that was added.</li>
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
    var joiner = new StringJoiner(", ", TranslationStoreStackEvent.class.getSimpleName() + " [", "]")
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
    result = prime * result + m_stack.hashCode();
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
    var other = (TranslationStoreStackEvent) obj;
    return m_type == other.m_type
        && Objects.equals(m_key, other.m_key)
        && Objects.equals(m_entry, other.m_entry)
        && Objects.equals(m_language, other.m_language)
        && Objects.equals(m_stack, other.m_stack);
  }
}
