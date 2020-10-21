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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * Generator to create .properties files. It supports header comments.
 */
public class PropertiesGenerator implements ISourceGenerator<ISourceBuilder<?>> {

  /**
   * The encoding used to load the content from an {@link InputStream}. As by the definition of the .properties file
   * format.
   */
  public static final Charset ENCODING = StandardCharsets.ISO_8859_1;
  private static final Pattern LINE_SEPARATOR_REGEX = Pattern.compile(lineSeparator());

  private final Map<String, String> m_properties = new HashMap<>();
  private final List<String> m_headerLines = new ArrayList<>();

  /**
   * @return A new empty {@link PropertiesGenerator}.
   */
  public static PropertiesGenerator create() {
    return create((Map<String, String>) null);
  }

  /**
   * @param properties
   *          The initial property map. May be {@code null}.
   * @return A new {@link PropertiesGenerator} pre filled with the given properties.
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
   * @return A new {@link PropertiesGenerator} pre filled with the given properties and header lines.
   */
  public static PropertiesGenerator create(Map<String, String> properties, Collection<String> headerLines) {
    return new PropertiesGenerator(properties, headerLines);
  }

  /**
   * Creates a new {@link PropertiesGenerator} pre filled with the properties and header lines from the
   * {@link InputStream} given.
   * 
   * @param in
   *          The {@link InputStream} in the .properties file format. Must not be {@code null}.
   * @return A new {@link PropertiesGenerator} with the content from the given stream.
   * @throws IOException
   */
  public static PropertiesGenerator create(InputStream in) throws IOException {
    var result = new PropertiesGenerator(null, null);
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
   */
  public PropertiesGenerator load(InputStream input) throws IOException {
    var content = toCharArray(fromInputStream(input, ENCODING));
    try (var reader = new BufferedReader(new CharArrayReader(content))) {
      readHeaderLines(reader);
    }
    try (Reader reader = new CharArrayReader(content)) {
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
    var prop = new Properties();
    //noinspection UseOfPropertiesAsHashtable
    prop.putAll(m_properties);

    String content;
    try (var out = new ByteArrayOutputStream()) {
      prop.store(out, null);
      content = out.toString(ENCODING.name());
    }
    catch (IOException e) {
      throw new SdkException("Error encoding properties", e);
    }

    return LINE_SEPARATOR_REGEX.splitAsStream(content)
        .filter(line -> !isComment(line));
  }

  private static boolean isComment(CharSequence line) {
    return line.charAt(0) == '#' || line.charAt(0) == '!';
  }

  private void readHeaderLines(BufferedReader reader) throws IOException {
    String line;
    m_headerLines.clear();
    while ((line = reader.readLine()) != null) {
      var naturalLine = line.trim();
      if (naturalLine.isEmpty()) {
        m_headerLines.add(line);
      }
      else if (isComment(naturalLine)) {
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
