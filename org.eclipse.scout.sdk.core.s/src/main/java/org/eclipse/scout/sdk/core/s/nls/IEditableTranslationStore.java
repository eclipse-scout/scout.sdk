/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link IEditableTranslationStore}</h3>
 * <p>
 * Represents an {@link ITranslationStore} that supports modifications.
 *
 * @since 7.0.0
 */
public interface IEditableTranslationStore extends ITranslationStore {

  /**
   * Adds a new {@link Language} to this store.
   * <p>
   * If this {@link Language} already existed, it will be replaced.
   *
   * @param language
   *          The language to add. Must not be {@code null}.
   * @throws IllegalArgumentException
   *           if this store is not editable ({@link #isEditable()}).
   */
  void addNewLanguage(Language language);

  /**
   * Updates or creates the {@link ITranslationEntry} with the same key as the specified {@link ITranslation}.<br>
   * The languages and texts of the existing {@link ITranslationEntry} are replaced with the ones from the specified
   * template. Languages no longer present are removed from the entry. Missing languages are created as needed.
   *
   * @param template
   *          The template that defines the new languages and texts. Must not be {@code null}.
   * @return The modified or created {@link ITranslationEntry}.
   */
  ITranslationEntry setTranslation(ITranslation template);

  /**
   * Changes the key of the {@link ITranslationEntry} with the specified key.
   *
   * @param oldKey
   *          The existing key of the {@link ITranslationEntry} to modify. Must not be blank.
   * @param newKey
   *          The new key. Must not be blank.
   * @return The updated {@link ITranslationEntry}.
   */
  ITranslationEntry changeKey(String oldKey, String newKey);

  /**
   * Removes the translation with the specified key from this store.
   *
   * @param key
   *          The key of the translation to remove. Must not be blank (see {@link Strings#isBlank(CharSequence)}).
   * @return The removed {@link ITranslationEntry} or {@code null} if it could not be found.
   * @throws IllegalArgumentException
   *           if this store is not editable ({@link #isEditable()}).
   */
  ITranslationEntry removeTranslation(String key);

  /**
   * @return {@code true} if there were changes to that store since the last {@link #reload(IProgress)} or
   *         {@link #flush(IEnvironment, IProgress)}. {@code false} otherwise.
   */
  boolean isDirty();

  /**
   * Flushes all changes made to the data source.
   *
   * @param env
   *          The {@link IEnvironment} to store the new content.
   * @param progress
   *          The {@link IProgress} monitor.
   * @throws IllegalArgumentException
   *           if this store is not editable ({@link #isEditable()}).
   */
  void flush(IEnvironment env, IProgress progress);
}
