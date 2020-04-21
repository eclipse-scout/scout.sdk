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
package org.eclipse.scout.sdk.core.s.util.search;

import static org.eclipse.scout.sdk.core.util.CoreUtils.extensionOf;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * Represents the input for an {@link IFileQuery}. This represents a file in which the query is searching.
 */
public class FileQueryInput {

  private final Path m_file;
  private final Path m_module;
  private final String m_extension;
  private final Supplier<char[]> m_fileContentLoader;
  private final FinalValue<char[]> m_fileContent;

  public FileQueryInput(Path file, Path module, Supplier<char[]> fileContentLoader) {
    m_file = Ensure.notNull(file);
    m_module = Ensure.notNull(module);
    m_fileContentLoader = Ensure.notNull(fileContentLoader);
    m_fileContent = new FinalValue<>();
    m_extension = extensionOf(file);
  }

  /**
   * @return The absolute path of the file
   */
  public Path file() {
    return m_file;
  }

  /**
   * @return The absolute path to the java module that contains the {@link #file()}.
   */
  public Path module() {
    return m_module;
  }

  /**
   * @return The file extension of {@link #file()} without leading dot or an empty String if the file has no extension.
   */
  public String fileExtension() {
    return m_extension;
  }

  /**
   * @return The lazy loaded file content.
   */
  public char[] fileContent() {
    return m_fileContent.computeIfAbsentAndGet(m_fileContentLoader);
  }

  @Override
  public String toString() {
    return new StringBuilder(FileQueryInput.class.getSimpleName()).append(" [file=").append(m_file).append(']').toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileQueryInput that = (FileQueryInput) o;
    return m_file.equals(that.m_file);
  }

  @Override
  public int hashCode() {
    return m_file.hashCode();
  }
}
