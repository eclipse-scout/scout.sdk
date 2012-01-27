/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.nls.sdk.internal.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Control;

public class FieldValidator {
  private List<Control> m_allFields = new LinkedList<Control>();
  private List<Control> m_disabledFields = new LinkedList<Control>();

  public void addField(Control field) {
    m_allFields.add(field);
  }

  public boolean isEnabled(Control c) {
    return m_disabledFields.contains(c);
  }

  public void setDisabled(Control c) {
    m_disabledFields.add(c);
  }

  public void reset() {
    m_disabledFields.clear();
  }

  public void apply() {
    for (Control c : m_allFields) {
      c.setEnabled(!m_disabledFields.contains(c));
    }
  }
}
