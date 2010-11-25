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

import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputFormatter;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.TextInputFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
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

public class ButtonTextField<T> extends Composite {
  public static final int VALIDATE_ON_MODIFY = 1 << 99;
  public static final int VALIDATE_ON_FOCUS_LOST = 1 << 98;

  private Label m_label;
  private Text m_text;
  private Button m_smartButton;

  private IInputFormatter<T> m_inputFormatter = new TextInputFormatter<T>();
  private List<IInputChangedListener<T>> m_inputChangedListeners = new LinkedList<IInputChangedListener<T>>();
  private String m_lastVerifiedInput;
  private T m_lastVerifiedItem = null;

  public ButtonTextField(Composite parent) {
    this(parent, VALIDATE_ON_FOCUS_LOST);

  }

  /**
   * @param parent
   * @param style
   *          in addition to swt styles one of VALIDATE_ON_MODIFY or VALIDATE_ON_FOCUS_LOST (default)
   */
  public ButtonTextField(Composite parent, int style) {
    super(parent, SWT.NONE);
    createComponent(this, style);
  }

  protected void createComponent(Composite parent, int style) {
    m_label = new Label(parent, SWT.INHERIT_DEFAULT | SWT.RIGHT);
    m_text = new Text(parent, SWT.INHERIT_DEFAULT | SWT.BORDER);
    if ((style & VALIDATE_ON_MODIFY) != 0) {

      m_text.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          if (!m_text.getText().equals(m_lastVerifiedInput)) {
            handleInputChanged();
          }
        }
      });
    }
    else {
      m_text.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          if (!m_text.getText().equals(m_lastVerifiedInput)) {
            handleInputChanged();
          }
        }
      });
    }
    m_smartButton = new Button(parent, SWT.PUSH);

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

  private void handleInputChanged() {
    String input = m_text.getText();
    T item = m_inputFormatter.parse(this, input);
    if (m_lastVerifiedItem == null && item == null) {
      return;
    }
    if (m_lastVerifiedItem != null) {
      if (m_lastVerifiedItem.equals(item)) {
        return;
      }
    }
    m_lastVerifiedItem = item;
    m_lastVerifiedInput = input;
    m_text.setText(m_inputFormatter.format(this, item));

    for (IInputChangedListener<T> listener : m_inputChangedListeners) {
      listener.inputChanged(item);
    }
    m_text.setSelection(m_text.getText().length());
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_text.setEnabled(enabled);
    m_smartButton.setEnabled(enabled);
    super.setEnabled(enabled);
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

  public void setInputFormatter(IInputFormatter<T> formatter) {
    m_inputFormatter = formatter;
  }

  public IInputFormatter<T> getInputFormatter() {
    return m_inputFormatter;
  }

  public void setInput(T input) {
    m_text.setText(m_inputFormatter.format(this, input));
  }

  public T getInput() {
    return m_inputFormatter.parse(this, m_text.getText());
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

  public void addInputChangedListener(IInputChangedListener<T> listener) {
    m_inputChangedListeners.add(listener);
  }

  public void removeInputChangedListener(IInputChangedListener<T> listener) {
    m_inputChangedListeners.remove(listener);
  }

  public void setInputFire(T item) {
    setInput(item);
    for (IInputChangedListener<T> listener : m_inputChangedListeners) {
      listener.inputChanged(item);
    }
  }

}
