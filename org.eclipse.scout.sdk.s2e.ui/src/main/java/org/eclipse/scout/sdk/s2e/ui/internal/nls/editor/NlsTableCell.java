/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import java.util.Optional;

import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link NlsTableCell}</h3>
 *
 * @since 7.0.0
 */
public class NlsTableCell {

  private final int m_column;
  private final IStackedTranslation m_entry;
  private final Language m_language;

  public NlsTableCell(int column, IStackedTranslation entry, Language language) {
    m_column = column;
    m_entry = Ensure.notNull(entry);
    m_language = language;
  }

  public int column() {
    return m_column;
  }

  public IStackedTranslation translation() {
    return m_entry;
  }

  public Optional<Language> language() {
    return Optional.ofNullable(m_language);
  }

  public String text() {
    if (column() == NlsTableController.INDEX_COLUMN_KEYS) {
      return translation().key();
    }
    if (m_language != null) {
      return translation().text(m_language).orElse(null);
    }
    return null;
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

    var other = (NlsTableCell) obj;
    return m_column == other.m_column
        && m_entry.equals(other.m_entry);
  }
}
