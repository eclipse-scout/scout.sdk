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
package org.eclipse.scout.sdk.s2e.ui.fields.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

/**
 * <h3>TextField</h3>
 */
public class TextField extends Composite {

  private StyledTextEx m_text;
  private Label m_label;
  private Color m_backupTextBackground;
  private final int m_labelPercentage;

  public static final int DEFAULT_LABEL_PERCENTAGE = 20;

  public TextField(Composite parent) {
    this(parent, "");
  }

  public TextField(Composite parent, int labelPercentage) {
    this(parent, "", labelPercentage);
  }

  public TextField(Composite parent, String labelName) {
    this(parent, labelName, DEFAULT_LABEL_PERCENTAGE);
  }

  public TextField(Composite parent, String labelName, int labelPercentage) {
    super(parent, SWT.NONE);
    m_labelPercentage = labelPercentage;
    createContent(this);
    setLabelText(labelName);
  }

  protected void createContent(Composite parent) {
    m_label = new Label(parent, SWT.NONE);
    m_label.setAlignment(SWT.RIGHT);
    m_text = new StyledTextEx(parent, SWT.BORDER | SWT.SINGLE);

    // layout
    parent.setLayout(new FormLayout());
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(getLabelPercentage(), 0);
    labelData.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(labelData);

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(m_label, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    m_text.setLayoutData(textData);
  }

  @Override
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  @Override
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  public void addVerifyListener(VerifyListener verifyListener) {
    m_text.addVerifyListener(verifyListener);
  }

  public void removeVerifyListener(VerifyListener verifyListener) {
    m_text.removeVerifyListener(verifyListener);
  }

  public void addModifyListener(ModifyListener listener) {
    m_text.addModifyListener(listener);
  }

  public void removeModifyListener(ModifyListener listener) {
    m_text.removeModifyListener(listener);
  }

  public void setLabelText(String text) {
    m_label.setText(text);
  }

  public String getLabelText() {
    return m_label.getText();
  }

  @Override
  public void setToolTipText(String string) {
    m_label.setToolTipText(string);
    m_text.setToolTipText(string);
    super.setToolTipText(string);
  }

  @Override
  public Menu getMenu() {
    return m_text.getMenu();
  }

  @Override
  public void setMenu(Menu menu) {
    m_text.setMenu(menu);
  }

  public void setText(String text) {
    if (text == null) {
      text = "";
    }
    m_text.setText(text);
  }

  public String getText() {
    return m_text.getText();
  }

  public void setEditable(boolean editable) {
    m_text.setEditable(editable);
  }

  public boolean getEditable() {
    return m_text.getEditable();
  }

  public boolean isEditable() {
    return m_text.getEditable();
  }

  @Override
  public boolean setFocus() {
    return m_text.setFocus();
  }

  public StyledTextEx getTextComponent() {
    return m_text;
  }

  protected Label getLabelComponent() {
    return m_label;
  }

  public Point getSelection() {
    return m_text.getSelection();
  }

  public void setSelection(int start) {
    m_text.setSelection(start);
  }

  public void setSelection(Point point) {
    m_text.setSelection(point);
  }

  @Override
  public boolean getEnabled() {
    if (m_text == null) {
      return super.getEnabled();
    }
    return m_text.getEnabled();
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (m_backupTextBackground == null) {
      m_backupTextBackground = m_text.getBackground();
    }
    m_text.setEnabled(enabled);
    if (enabled) {
      m_text.setBackground(m_backupTextBackground);
    }
    else {
      m_text.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
    }
  }

  public int getLabelPercentage() {
    return m_labelPercentage;
  }

}
