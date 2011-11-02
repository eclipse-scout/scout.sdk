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
package org.eclipse.scout.sdk.ui.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 *
 */
public class CheckBoxField extends Composite {
  private Button m_check;
  private Label m_label;

  public CheckBoxField(Composite parent) {
    super(parent, SWT.NONE);
    createContent(this);
  }

  protected void createContent(Composite parent) {
    m_label = new Label(parent, SWT.NONE);
    m_check = new Button(parent, SWT.CHECK);

    setLayout(new FormLayout());

    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 0);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(40, 0);
    labelData.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(labelData);

    FormData chkData = new FormData();
    chkData.top = new FormAttachment(0, 0);
    chkData.left = new FormAttachment(m_label, 5);
    chkData.right = new FormAttachment(100, 0);
    chkData.bottom = new FormAttachment(100, 0);
    m_check.setLayoutData(chkData);
  }

  public void setLabel(String label) {
    m_check.setText(label);
  }

  public String getLabel() {
    return m_check.getText();
  }

  public void addSelectionListener(SelectionListener listener) {
    m_check.addSelectionListener(listener);
  }

  public void removeSelectionListener(SelectionListener listener) {
    m_check.removeSelectionListener(listener);
  }

  public boolean getSelection() {
    return m_check.getSelection();
  }

  public void setSelection(boolean selected) {
    m_check.setSelection(selected);
  }
}
