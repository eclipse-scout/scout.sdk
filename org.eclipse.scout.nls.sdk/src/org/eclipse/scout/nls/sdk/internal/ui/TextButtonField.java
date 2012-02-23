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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TextButtonField extends Composite {
  private Label m_label;
  private Text m_text;
  private Button m_smartButton;

  public TextButtonField(Composite parent) {
    super(parent, SWT.NONE);
    createComponent(this);
  }

  protected void createComponent(Composite parent) {
    m_label = new Label(parent, SWT.INHERIT_DEFAULT | SWT.RIGHT);
    m_text = new Text(parent, SWT.INHERIT_DEFAULT | SWT.BORDER);

    m_smartButton = new Button(parent, SWT.PUSH | SWT.FLAT);

    parent.setTabList(new Control[]{m_text, m_smartButton});

    // layout
    parent.setLayout(new FormLayout());

    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.bottom = new FormAttachment(100, 0);
    data.right = new FormAttachment(40, 0);
    m_label.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(m_label, 5);
    data.bottom = new FormAttachment(100, 0);
    data.right = new FormAttachment(m_smartButton, -2);
    m_text.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    // data.left = new FormAttachment(m_label,5);
    data.bottom = new FormAttachment(100, 0);
    data.right = new FormAttachment(100, 0);
    m_smartButton.setLayoutData(data);
  }

  public void addButtonSelectionListener(SelectionListener listener) {
    m_smartButton.addSelectionListener(listener);
  }

  public void removeButtonSelectionListener(SelectionListener listener) {
    m_smartButton.removeSelectionListener(listener);
  }

  public void addTextFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  public void removeTextFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  public void addTextModifyListener(ModifyListener listener) {
    m_text.addModifyListener(listener);
  }

  public void removeTextModifyListener(ModifyListener listener) {
    m_text.removeModifyListener(listener);
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_text.setEnabled(enabled);
    m_smartButton.setEnabled(enabled);
    super.setEnabled(enabled);
  }

  public void setText(String text) {
    m_text.setText(text);
  }

  public String getText() {
    return m_text.getText();
  }

  public void setLabelText(String text) {
    m_label.setText(text);
  }

  public String getLabelText() {
    return m_label.getText();
  }

  public void setButtonText(String string) {
    m_smartButton.setText(string);
  }

  public String getButtonText() {
    return m_smartButton.getText();
  }

  public void setTextEnabled(boolean enabled) {
    m_text.setEnabled(enabled);
  }

  public boolean isTextEnabled() {
    return m_text.isEnabled();
  }

  public boolean getTextEnabled() {
    return m_text.getEnabled();
  }
}
