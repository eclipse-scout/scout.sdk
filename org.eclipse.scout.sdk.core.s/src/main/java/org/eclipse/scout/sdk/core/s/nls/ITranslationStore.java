/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.nls;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.environment.IProgress;

/**
 * <h3>{@link ITranslationStore}</h3>
 * <p>
 * Represents a store holding {@link ITranslation}s.
 *
 * @since 7.0.0
 */
public interface ITranslationStore {

  /**
   * Specifies if this {@link ITranslationStore} is read-only or can be modified. If it is modifiable the store must
   * also implement {@link IEditableTranslationStore}.
   * <p>
   * Please note: A store implementation that supports editing may still be read-only because the underlying data source
   * is read-only.
   *
   * @return {@code true} if it can be modified. {@code false} otherwise.
   */
  boolean isEditable();

  /**
   * @return A {@link Stream} providing all keys that exist in this store.
   */
  Stream<String> keys();

  /**
   * @param key
   *          The key to search.
   * @return {@code true} if this store contains the specified key. {@code false} otherwise.
   */
  boolean containsKey(String key);

  /**
   * @return A {@link Stream} returning all entries of this store.
   */
  Stream<? extends ITranslationEntry> entries();

  /**
   * @return A {@link Stream} returning all languages that exist in this store.
   */
  Stream<Language> languages();

  /**
   * Gets the {@link ITranslationEntry} for the specified key.
   *
   * @param key
   *          The key to search.
   * @return the {@link ITranslationEntry} for the specified key or an empty {@link Optional} if it could not be found.
   */
  Optional<ITranslationEntry> get(String key);

  /**
   * Gets all key-text mappings for a specific {@link Language}.<br>
   * An empty {@link Optional} means the specified {@link Language} could not be found.<br>
   * And empty {@link Map} means the {@link Language} exists but does not contain any translations.
   *
   * @param language
   *          The {@link Language} for which all mappings should be returned.
   * @return All mappings of the specified {@link Language} or an empty {@link Optional}.
   */
  Optional<Map<String, String>> get(Language language);

  /**
   * Gets the text of the translation with specified key and language.
   *
   * @param key
   *          The key of the text to return.
   * @param language
   *          The {@link Language} of the text to return.
   * @return The text or an empty {@link Optional} if there is no entry in that store with specified key and
   *         {@link Language}.
   */
  Optional<String> get(String key, Language language);

  /**
   * @return The {@link TextProviderService} of this {@link ITranslationStore}.
   */
  TextProviderService service();

  /**
   * Reloads the contents of this store from its data source. All modification already applied to the store are
   * discarded.
   *
   * @param progress
   *          The {@link IProgress} monitor.
   */
  void reload(IProgress progress);
}
