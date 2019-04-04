/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.builder;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link StreamSourceBuilder}</h3>
 *
 * @since 9.0.0
 */
public class StreamSourceBuilder extends AbstractSourceBuilder<StreamSourceBuilder> implements AutoCloseable, Flushable {

  private final Writer m_writer;

  public StreamSourceBuilder(OutputStream out) {
    this(out, StandardCharsets.UTF_8);
  }

  public StreamSourceBuilder(OutputStream out, Charset charset) {
    this(out, charset, new BuilderContext());
  }

  public StreamSourceBuilder(OutputStream out, Charset charset, IBuilderContext context) {
    super(context);
    m_writer = new OutputStreamWriter(Ensure.notNull(out), Ensure.notNull(charset));
  }

  /**
   * @return The (modifiable) {@link Writer} to which the content is delegated.
   */
  public Writer source() {
    return m_writer;
  }

  @Override
  public StreamSourceBuilder append(char c) {
    try {
      source().append(c);
      return currentInstance();
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  @Override
  public StreamSourceBuilder append(CharSequence seq) {
    try {
      source().append(seq);
      return currentInstance();
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Flushes all buffered content to the {@link OutputStream}.
   *
   * @throws SdkException
   *           in case of an {@link IOException}
   */
  @Override
  public void flush() {
    try {
      m_writer.flush();
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Flushes and closes the underlying writers and the {@link OutputStream} provided.
   *
   * @throws SdkException
   *           in case of an {@link IOException}
   */
  @Override
  public void close() {
    try {
      m_writer.close();
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }
}
