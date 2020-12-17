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

import java.io.InputStream;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.generator.properties.PropertiesGenerator;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.Language;

/**
 * <h3>{@link ReadOnlyTranslationFile}</h3>
 *
 * @since 7.0.0
 */
public class ReadOnlyTranslationFile extends AbstractTranslationPropertiesFile {

  private final Object m_source;

  public ReadOnlyTranslationFile(Supplier<InputStream> contentSupplier, Language language) {
    this(contentSupplier, language, null);
  }

  public ReadOnlyTranslationFile(Supplier<InputStream> contentSupplier, Language language, Object source) {
    super(language, contentSupplier);
    m_source = source;
  }

  @Override
  public Object source() {
    return m_source;
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  @Override
  protected void writeEntries(PropertiesGenerator content, IEnvironment env, IProgress progress) {
    throwIfReadOnly();
  }
}
