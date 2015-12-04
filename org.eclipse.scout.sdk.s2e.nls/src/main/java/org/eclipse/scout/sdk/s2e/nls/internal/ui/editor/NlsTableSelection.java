/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.editor;

import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;

public class NlsTableSelection {
  private NlsEntry m_selectedRow;
  private int m_column;

  public NlsTableSelection(NlsEntry row, int column) {
    m_selectedRow = row;
    m_column = column;
  }

  public int getColumn() {
    return m_column;
  }

  public NlsEntry getSelectedRow() {
    return m_selectedRow;
  }

}
