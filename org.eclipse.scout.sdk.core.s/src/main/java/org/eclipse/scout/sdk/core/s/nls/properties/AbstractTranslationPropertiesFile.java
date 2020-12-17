/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.properties;

import static java.util.Collections.unmodifiableMap;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateKey;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.properties.PropertiesGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AbstractTranslationPropertiesFile}</h3>
 *
 * @since 7.0.0
 */
public abstract class AbstractTranslationPropertiesFile implements ITranslationPropertiesFile {

  public static final String FILE_SUFFIX = ".properties";
  public static final Pattern FILE_PATTERN = Pattern.compile("^[^_]+(?:_(" + Language.LANGUAGE_REGEX + "))?\\" + FILE_SUFFIX + "$");

  private final Language m_language;
  private final Supplier<InputStream> m_inputSupplier;

  private PropertiesGenerator m_fileContent;

  protected AbstractTranslationPropertiesFile(Language language, Supplier<InputStream> contentSupplier) {
    m_language = Ensure.notNull(language);
    m_inputSupplier = Ensure.notNull(contentSupplier);
  }

  private Map<String, String> entries() {
    if (m_fileContent == null) {
      throw newFail("Properties file has not been loaded yet.");
    }
    return m_fileContent.properties();
  }

  @Override
  public final Language language() {
    return m_language;
  }

  @Override
  public Map<String, String> allEntries() {
    return unmodifiableMap(entries());
  }

  @Override
  public final Stream<String> allKeys() {
    return entries().keySet().stream();
  }

  @Override
  public final Optional<String> translation(String key) {
    return Optional.ofNullable(entries().get(key));
  }

  @Override
  public boolean load(IProgress progress) {
    var newContent = readEntries();
    if (Objects.equals(m_fileContent, newContent)) {
      return false;
    }

    m_fileContent = newContent;
    removeInvalidEntries(newContent.properties().entrySet());
    return true;
  }

  private void removeInvalidEntries(Iterable<Entry<String, String>> entries) {
    var iterator = entries.iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      if (validateKey(entry.getKey()) != TranslationValidator.OK) {
        if (isEditable()) {
          // only log if you have the chance to fix it (skip logging for read-only files)
          SdkLog.warning("Skipping entry '{}={}' found in '{}' because the key is invalid.", entry.getKey(), entry.getValue(), source());
        }
        iterator.remove();
      }
    }
  }

  private PropertiesGenerator readEntries() {
    try (var in = Ensure.notNull(m_inputSupplier.get())) {
      return PropertiesGenerator.create(in);
    }
    catch (IOException e) {
      throw new SdkException("Error reading properties file for language '{}'.", language(), e);
    }
  }

  @Override
  public boolean setTranslation(String key, String text) {
    throwIfReadOnly();
    Ensure.notBlank(key);
    if (text == null) {
      return removeTranslation(key);
    }
    var oldTranslation = entries().put(key, text);
    return !text.equals(oldTranslation);
  }

  @Override
  public boolean removeTranslation(String key) {
    throwIfReadOnly();
    return entries().remove(Ensure.notBlank(key)) != null;
  }

  @Override
  public void flush(IEnvironment env, IProgress progress) {
    throwIfReadOnly();
    writeEntries(m_fileContent, env, progress);
  }

  protected void throwIfReadOnly() {
    if (!isEditable()) {
      throw new UnsupportedOperationException("Cannot modify a ready-only resource.");
    }
  }

  protected abstract void writeEntries(PropertiesGenerator content, IEnvironment env, IProgress progress);

  /**
   * @return The source object this file was loaded from. May be {@code null}.
   */
  protected abstract Object source();

  /**
   * Parses the language from a translation .properties file respecting the given prefix.<br>
   * The inverse operation is {@link #getPropertiesFileName(String, Language)}.
   * 
   * @param fileName
   *          The file name to parse. E.g. {@code "Texts_en_GB.properties"}. May not be {@code null}.
   * @param prefix
   *          An optional prefix the given file name must have to be accepted.
   * @return An {@link Optional} holding the language of the given file name or an empty {@link Optional} if it cannot
   *         be parsed (which might be because the prefix does not match).
   */
  public static Optional<Language> parseLanguageFromFileName(String fileName, String prefix) {
    if (Strings.isBlank(fileName)) {
      return Optional.empty();
    }
    if (Strings.hasText(prefix) && !fileName.startsWith(prefix)) {
      return Optional.empty();
    }

    var matcher = FILE_PATTERN.matcher(fileName);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    var languagePart = matcher.group(1);
    if (Strings.isBlank(languagePart)) {
      return Optional.of(Language.LANGUAGE_DEFAULT);
    }
    return Language.parse(languagePart);
  }

  /**
   * Gets the filename for a {@code .properties} file using the specified prefix and {@link Language}.<br>
   * This the inverse operation is {@link #parseLanguageFromFileName(String, String)}.
   *
   * @param prefix
   *          The file prefix. Must not be {@code null}.
   * @param language
   *          The language. Must not be {@code null}.
   * @return The file name.
   */
  public static String getPropertiesFileName(String prefix, Language language) {
    return prefix + '_' + language.locale() + FILE_SUFFIX;
  }
}
