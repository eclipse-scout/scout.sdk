/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.generator.properties;

import static java.lang.System.lineSeparator;
import static org.eclipse.scout.sdk.core.util.Strings.fromInputStream;
import static org.eclipse.scout.sdk.core.util.Strings.toCharArray;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Generator to create .properties files. It supports header comments.
 */
public class PropertiesGenerator implements ISourceGenerator<ISourceBuilder<?>> {

  private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
  private static final Pattern LINE_SEPARATOR_REGEX = Pattern.compile(lineSeparator());

  private final Map<String, String> m_properties = new HashMap<>();
  private final List<String> m_headerLines = new ArrayList<>();
  private Charset m_encoding = null; // uses default

  /**
   * @return A new empty {@link PropertiesGenerator}.
   */
  public static PropertiesGenerator create() {
    return create(null);
  }

  /**
   * @param properties
   *          The initial property map. May be {@code null}.
   * @return A new {@link PropertiesGenerator} pre-filled with the given properties.
   */
  public static PropertiesGenerator create(Map<String, String> properties) {
    return create(properties, null);
  }

  /**
   * @param properties
   *          The initial property map. May be {@code null}.
   * @param headerLines
   *          The header lines to add before the first properties. The lines are added as provided. To add comments the
   *          lines must include a comment char (#).
   * @return A new {@link PropertiesGenerator} pre-filled with the given properties and header lines.
   */
  public static PropertiesGenerator create(Map<String, String> properties, Collection<String> headerLines) {
    return new PropertiesGenerator(properties, headerLines);
  }

  /**
   * Creates a new {@link PropertiesGenerator} pre-filled with the properties and header lines from the
   * {@link InputStream} given.
   * 
   * @param in
   *          The {@link InputStream} in the .properties file format. Must not be {@code null}.
   * @param encoding
   *          The encoding to use when reading the .properties file. May be {@code null} to use the default encoding
   *          (UTF-8).
   * @return A new {@link PropertiesGenerator} with the content from the given stream.
   * @throws IOException
   *           while reading from the given {@link InputStream}.
   */
  public static PropertiesGenerator create(InputStream in, Charset encoding) throws IOException {
    var result = new PropertiesGenerator(null, null).withEncoding(encoding);
    result.load(in);
    return result;
  }

  protected PropertiesGenerator(Map<String, String> properties, Collection<String> headerLines) {
    if (properties != null) {
      m_properties.putAll(properties);
    }
    if (headerLines != null) {
      m_headerLines.addAll(headerLines);
    }
  }

  /**
   * Loads this generator with the content of the given {@link InputStream} having the .properties file format.<br>
   * All existing content is replaced.
   * 
   * @param input
   *          The {@link InputStream} in the .properties file format. Must not be {@code null}.
   * @return This generator.
   * @throws IOException
   *           while reading from the given {@link InputStream}.
   */
  public PropertiesGenerator load(InputStream input) throws IOException {
    var encoding = encoding().orElse(DEFAULT_ENCODING);
    var content = toCharArray(fromInputStream(input, encoding));
    try (var reader = new BufferedReader(new CharArrayReader(content))) {
      readHeaderLines(reader);
    }
    try (var reader = new CharArrayReader(content)) {
      readProperties(reader);
    }
    return this;
  }

  /**
   * @return All header lines. The resulting {@link List} may be modified.
   */
  public List<String> headerLines() {
    return m_headerLines;
  }

  /**
   * @return The {@link Charset} to use when writing the .properties files. By default, UTF-8 is used.
   */
  public Optional<Charset> encoding() {
    return Optional.ofNullable(m_encoding);
  }

  /**
   * Sets the new {@link Charset} to use when writing .properties files. By default, UTF-8 is used. If
   * {@link StandardCharsets#ISO_8859_1} is passed, characters are encoded as necessary.
   * 
   * @param newEncoding
   *          The new {@link Charset} or {@code null} to use the default charset (UTF-8).
   * @return this instance
   */
  public PropertiesGenerator withEncoding(Charset newEncoding) {
    m_encoding = newEncoding;
    return this;
  }

  /**
   * @return All the property mappings. The resulting {@link Map} may be modified.
   */
  public Map<String, String> properties() {
    return m_properties;
  }

  @Override
  public void generate(ISourceBuilder<?> builder) {
    m_headerLines.forEach(builder::appendLine);
    getAsPropertiesEncodedLines()
        .sorted()
        .forEach(builder::appendLine);
  }

  protected Stream<String> getAsPropertiesEncodedLines() {
    var encoding = encoding().orElse(DEFAULT_ENCODING);
    var escapeUnicode = StandardCharsets.ISO_8859_1.equals(encoding); // use encoded content (non Latin-1 characters are written as their appropriate unicode hexadecimal value) for ISO-8859-1 encoding
    var content = escapeUnicode ? getWithUnicodeEscape() : getWithoutUnicodeEscape();
    return LINE_SEPARATOR_REGEX.splitAsStream(content)
        .filter(line -> !isComment(line));
  }

  protected CharSequence getWithoutUnicodeEscape() {
    var prop = new Properties();
    prop.putAll(m_properties);

    try (var out = new StringWriter()) {
      prop.store(out, null); // store to Writer performs NO unicode escape
      return out.getBuffer();
    }
    catch (IOException e) {
      throw new SdkException("Error encoding properties", e);
    }
  }

  protected CharSequence getWithUnicodeEscape() {
    var prop = new Properties();
    prop.putAll(m_properties);

    try (var out = new ByteArrayOutputStream()) {
      prop.store(out, null); // store to OutputStream performs unicode escape
      return out.toString(StandardCharsets.ISO_8859_1);
    }
    catch (IOException e) {
      throw new SdkException("Error encoding properties", e);
    }
  }

  private static boolean isComment(CharSequence line) {
    return line.charAt(0) == '#' || line.charAt(0) == '!';
  }

  private void readHeaderLines(BufferedReader reader) throws IOException {
    String line;
    m_headerLines.clear();
    while ((line = reader.readLine()) != null) {
      var lineTrimmed = Strings.trim(line);
      if (Strings.isEmpty(lineTrimmed)) {
        m_headerLines.add(line);
      }
      else if (isComment(lineTrimmed)) {
        m_headerLines.add(line);
      }
      else {
        return;
      }
    }
  }

  private void readProperties(Reader reader) throws IOException {
    var props = new Properties();
    props.load(reader);
    m_properties.clear();
    for (var entry : props.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      if (key instanceof String && value instanceof String) {
        m_properties.put((String) key, (String) value);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var that = (PropertiesGenerator) o;
    return m_properties.equals(that.m_properties) &&
        m_headerLines.equals(that.m_headerLines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_properties, m_headerLines);
  }
}
