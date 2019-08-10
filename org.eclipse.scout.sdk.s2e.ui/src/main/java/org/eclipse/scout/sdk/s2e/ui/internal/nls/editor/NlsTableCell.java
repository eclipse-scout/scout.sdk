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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import java.util.Optional;

import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link NlsTableCell}</h3>
 *
 * @since 7.0.0
 */
public class NlsTableCell {

  private final int m_column;
  private final ITranslationEntry m_entry;
  private final Language m_language;

  public NlsTableCell(int column, ITranslationEntry entry, Language language) {
    m_column = column;
    m_entry = Ensure.notNull(entry);
    m_language = language;
  }

  public int column() {
    return m_column;
  }

  public ITranslationEntry entry() {
    return m_entry;
  }

  public Optional<Language> language() {
    return Optional.ofNullable(m_language);
  }

  public String text() {
    if (column() == NlsTableController.INDEX_COLUMN_KEYS) {
      return entry().key();
    }
    if (m_language != null) {
      return entry().translation(m_language).orElse(null);
    }
    return null;
  }

  public ITranslationStore store() {
    return entry().store();
  }

  @Override
  public int hashCode() {
    return 31 * m_column + m_entry.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    NlsTableCell other = (NlsTableCell) obj;
    return m_column == other.m_column
        && m_entry.equals(other.m_entry);
  }
}
