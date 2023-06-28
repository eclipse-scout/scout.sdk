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

import static org.eclipse.scout.sdk.core.util.Strings.isBlank;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.eclipse.scout.sdk.core.generator.properties.PropertiesGenerator;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link EditableTranslationFile}</h3>
 *
 * @since 7.0.0
 */
public class EditableTranslationFile extends AbstractTranslationPropertiesFile {

  private final Path m_file;

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public EditableTranslationFile(Path file, Charset encoding, Language language) {
    super(language, encoding, () -> toStream(file));
    m_file = Ensure.notNull(file);
  }

  protected static InputStream toStream(Path file) {
    if (!Files.isRegularFile(file)) {
      return new ByteArrayInputStream(new byte[]{});
    }

    try {
      return new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ));
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
  protected void writeEntries(PropertiesGenerator content, IEnvironment env, IProgress progress) {
    progress.init(100, "Write translation properties file");

    // remove empty texts
    content.properties().entrySet().removeIf(entry -> isBlank(entry.getValue()) || isBlank(entry.getKey()));
    progress.worked(10);

    if (content.properties().isEmpty()) {
      // this file has no more texts: remove it
      env.deleteIfExists(path());
      progress.worked(90);
      return;
    }

    env.writeResource(content, path(), progress.newChild(70));
    progress.worked(90);
  }

  @Override
  protected Object source() {
    return path();
  }

  public Path path() {
    return m_file;
  }
}
