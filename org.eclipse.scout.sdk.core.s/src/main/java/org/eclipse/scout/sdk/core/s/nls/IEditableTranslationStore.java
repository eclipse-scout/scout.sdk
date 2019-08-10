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
   * Adds a new {@link ITranslation} to that {@link ITranslationStore}.
   * <p>
   * If there existed an entry with the same key already, it is replaced.
   *
   * @param newTranslation
   *          The new entry. Must not be {@code null}.
   * @return the just added {@link ITranslationEntry}.
   * @throws IllegalArgumentException
   *           if this store is not editable ({@link #isEditable()}).
   */
  ITranslationEntry addNewTranslation(ITranslation newTranslation);

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
   * @return {@code true} if there were changes to that store since the last {@link #reload(IProgress)} or
   *         {@link #flush(IEnvironment, IProgress)}. {@code false} otherwise.
   */
  boolean isDirty();

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
   * Updates the {@link ITranslationEntry} with the same key as the specified {@link ITranslation}.<br>
   * The languages and texts of the existing {@link ITranslationEntry} are replaced with the ones from the specified
   * template.
   *
   * @param template
   *          The template that defines the new languages and texts of an existing {@link ITranslationEntry} with the
   *          same key as the template. Must not be {@code null}.
   * @return The modified {@link ITranslationEntry}.
   */
  ITranslationEntry updateTranslation(ITranslation template);
}
