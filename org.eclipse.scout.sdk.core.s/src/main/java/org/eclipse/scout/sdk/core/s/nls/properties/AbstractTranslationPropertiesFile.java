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
package org.eclipse.scout.sdk.core.s.nls.properties;

import static java.util.Collections.unmodifiableMap;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link AbstractTranslationPropertiesFile}</h3>
 *
 * @since 7.0.0
 */
public abstract class AbstractTranslationPropertiesFile implements ITranslationPropertiesFile {

  private static final Pattern FILE_PATTERN = Pattern.compile("^[^_]*(?:_(" + Language.LANGUAGE_REGEX + "))?\\.properties$");

  private final Language m_language;
  private final Supplier<InputStream> m_inputSupplier;

  private Map<String, String> m_entries;

  protected AbstractTranslationPropertiesFile(Language language, Supplier<InputStream> contentSupplier) {
    m_language = Ensure.notNull(language);
    m_inputSupplier = Ensure.notNull(contentSupplier);
  }

  private Map<String, String> entries() {
    if (m_entries == null) {
      throw newFail("Properties file has not been loaded yet.");
    }
    return m_entries;
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
    progress.init("Load translation properties file", 100);

    Map<String, String> newEntries = readEntries(progress.newChild(99));
    try {
      if (m_entries != null && m_entries.equals(newEntries)) {
        return false;
      }

      m_entries = newEntries;
      return true;
    }
    finally {
      progress.setWorkRemaining(0);
    }
  }

  private Map<String, String> readEntries(IProgress progress) {
    try (InputStream in = Ensure.notNull(m_inputSupplier.get())) {
      return parse(in, progress.newChild(90));
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  @Override
  public boolean setTranslation(String key, String text) {
    throwIfReadOnly();
    if (text == null) {
      return removeTranslation(key);
    }
    String oldTranslation = entries().put(Ensure.notBlank(key), text);
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
    writeEntries(entries(), env, progress);
  }

  private void throwIfReadOnly() {
    if (!isEditable()) {
      throw new UnsupportedOperationException("Cannot modify a ready-only resource.");
    }
  }

  protected abstract void writeEntries(Map<String, String> entries, IEnvironment env, IProgress progress);

  /**
   * Reads the contents of the specified {@link InputStream} as {@code .properties} file and returns the key-value
   * mappings.
   *
   * @param in
   *          The {@link InputStream} to load the data from. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress} monitor.
   * @return The mapping.
   */
  public static Map<String, String> parse(InputStream in, IProgress progress) {
    progress.init("Reading properties file", 100);

    Set<Entry<Object, Object>> entrySet = loadProperties(in, progress.newChild(90)).entrySet();

    IProgress loopProgress = progress
        .newChild(10)
        .setWorkRemaining(entrySet.size());
    Map<String, String> result = new HashMap<>(entrySet.size());
    for (Entry<Object, Object> entry : entrySet) {
      Object key = entry.getKey();
      Object translation = entry.getValue();
      if (key != null && translation != null) {
        result.put(key.toString(), translation.toString());
      }
      loopProgress.worked(1);
    }
    return result;
  }

  /**
   * Parses the language from the specified {@code .properties} file name.
   * 
   * @param fileName
   *          The file name to parse. Must not be {@code null}.
   * @return The parsed {@link Language}
   * @throws IllegalArgumentException
   *           if it cannot be parsed because it is no valid name.
   */
  public static Language parseFromFileNameOrThrow(CharSequence fileName) {
    Matcher matcher = FILE_PATTERN.matcher(fileName);
    if (matcher.matches()) {
      String languagePart = matcher.group(1);
      if (languagePart == null) {
        return Language.LANGUAGE_DEFAULT;
      }
      return Language.parseThrowingOnError(languagePart);
    }
    throw newFail("Invalid file name '{}'. Language cannot be parsed.", fileName);
  }

  private static Map<Object, Object> loadProperties(InputStream in, IProgress progress) {
    try {
      Properties props = new Properties();
      props.load(in);
      return props;
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
    finally {
      progress.setWorkRemaining(0);
    }
  }
}
