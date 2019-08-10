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
package org.eclipse.scout.sdk.core.s.nls.properties;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * <h3>{@link EditableTranslationFile}</h3>
 *
 * @since 7.0.0
 */
public class EditableTranslationFile extends AbstractTranslationPropertiesFile {

  private final Path m_file;

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public EditableTranslationFile(Path file) {
    super(parseFromFileNameOrThrow(file.getFileName().toString()), () -> toStream(file));
    m_file = Ensure.notNull(file);
  }

  protected static InputStream toStream(Path file) {
    if (!Files.exists(file)) {
      return new ByteArrayInputStream(new byte[]{});
    }

    try {
      return Files.newInputStream(file, StandardOpenOption.READ);
    }
    catch (IOException e) {
      throw new SdkException("Cannot read file '{}'.", file, e);
    }
  }

  @Override
  public boolean isEditable() {
    return true;
  }

  @Override
  protected void writeEntries(Map<String, String> entries, IEnvironment env, IProgress progress) {
    progress.init("Write translation properties file", 100);

    String[] propertiesEncodeLines = propertiesEncode(entries);
    progress.worked(20);

    Arrays.sort(propertiesEncodeLines);
    progress.worked(10);

    env.writeResource(b -> appendLines(b, propertiesEncodeLines), file(), progress.newChild(70));
  }

  private static void appendLines(ISourceBuilder<?> builder, String[] linesSorted) {
    for (int i = firstNonCommentLine(linesSorted); i < linesSorted.length; i++) {
      builder.append(linesSorted[i]).nl();
    }
  }

  private static int firstNonCommentLine(String[] lines) {
    int i = 0;
    while (lines.length > i && lines[i].startsWith("#")) {
      i++;
    }
    return i;
  }

  private static String[] propertiesEncode(Map<String, String> entries) {
    Properties prop = new Properties();
    //noinspection UseOfPropertiesAsHashtable
    prop.putAll(entries);

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      prop.store(out, null);
      String content = out.toString(StandardCharsets.ISO_8859_1.name());
      String systemNl = System.lineSeparator();
      return Pattern.compile(systemNl).split(content);
    }
    catch (IOException e) {
      throw new SdkException("Error encoding translations", e);
    }
  }

  public Path file() {
    return m_file;
  }
}
