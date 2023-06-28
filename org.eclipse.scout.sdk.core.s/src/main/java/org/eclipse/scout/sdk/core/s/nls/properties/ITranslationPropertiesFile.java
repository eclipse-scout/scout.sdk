/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls.properties;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.nls.Language;

/**
 * <h3>{@link ITranslationPropertiesFile}</h3>
 * <p>
 * Represents a translation {@code .properties} file as used by
 * {@link IScoutApi#AbstractDynamicNlsTextProviderService()}.
 *
 * @since 7.0.0
 */
public interface ITranslationPropertiesFile {

  /**
   * Specifies if the {@code .properties} file can be modified.
   *
   * @return {@code true} if it can be modified. {@code false} otherwise.
   */
  boolean isEditable();

  /**
   * @return The {@link Language} of this file.
   */
  Language language();

  /**
   * Loads all entries from the {@code .properties} file. Existing values are replaced with the ones from the
   * {@code .properties} file.
   *
   * @param progress
   *          The {@link IProgress} monitor.
   * @return {@code true} if the operation changed entries. {@code false} if it is the same as before.
   */
  boolean load(IProgress progress);

  /**
   * @return The encoding of the .properties file.
   */
  Optional<Charset> encoding();

  /**
   * @return A {@link Stream} with all keys that exist in this {@code .properties} file.
   */
  Stream<String> allKeys();

  /**
   * Gets the text with the specified key within this {@code .properties} file.
   *
   * @param key
   *          The key to search.
   * @return The text for the specified key or an empty {@link Optional}.
   */
  Optional<String> translation(String key);

  /**
   * Sets the specified key to the specified text.
   *
   * @param key
   *          The key to modify. Must not be blank.
   * @param text
   *          The new text. If it is {@code null} the corresponding key is removed. Otherwise, the new text is
   *          registered for the specified key.
   * @return {@code true} if the file was updated. {@code false} otherwise.
   */
  boolean setTranslation(String key, String text);

  /**
   * Removes the specified key from this {@code .properties} file.
   *
   * @param key
   *          The key to remove. Must not be blank.
   * @return {@code true} if the key was removed. {@code false} if it could not be found.
   */
  boolean removeTranslation(String key);

  /**
   * Flushes the current content of this file to the underlying {@code .properties} file.
   *
   * @param env
   *          The {@link IEnvironment} to use.
   * @param progress
   *          The {@link IProgress} monitor.
   */
  void flush(IEnvironment env, IProgress progress);

  /**
   * @return An unmodifiable view to all key-text mappings of this {@code .properties} file.
   */
  Map<String, String> allEntries();
}
