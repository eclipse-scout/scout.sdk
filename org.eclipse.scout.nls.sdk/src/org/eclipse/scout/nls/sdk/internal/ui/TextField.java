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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputFormatter;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IValidationListener;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.TextInputFormatter;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.TextValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TextField<T> extends Composite {

  public static final int VALIDATE_ON_MODIFY = 1 << 99;
  public static final int VALIDATE_ON_FOCUS_LOST = 1 << 98;
  public static final int MULTI_LINE_TEXT_FIELD = 1 << 97;
  public static final int LABEL_NO_ALIGN = 1 << 96;

  private Text m_text;
  private Label m_label;
  private IInputFormatter<T> m_inputFormatter = new TextInputFormatter<T>();
  private IInputValidator m_inputValidator = new TextValidator();
  private List<IInputChangedListener<T>> m_inputChangedListener = new LinkedList<IInputChangedListener<T>>();
  private List<IValidationListener> m_validationListener = new LinkedList<IValidationListener>();
  private Object m_input;
  private boolean m_valid;
  private IStatus m_status;

  public TextField(Composite parent) {
    this(parent, VALIDATE_ON_FOCUS_LOST);
  }

  /**
   * @param parent
   * @param style
   *          in addition to swt styles one of VALIDATE_ON_MODIFY or VALIDATE_ON_FOCUS_LOST (default)
   */
  public TextField(Composite parent, int style) {
    this(parent, style, "");
  }

  public void setLabelVisible(boolean visible) {
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 0);
    labelData.left = new FormAttachment(0, 0);
    if (visible) labelData.right = new FormAttachment(40, 0);
    labelData.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(labelData);
    m_label.setVisible(visible);
  }

  public TextField(Composite parent, int style, String labelName) {
    super(parent, SWT.NONE);
    setLayout(new FormLayout());
    createComposite(this, style);
    setLabelText(labelName);
    validateInput();
  }

  private void createComposite(Composite parent, int style) {

    m_label = new Label(parent, SWT.NONE);
    m_label.setAlignment(SWT.RIGHT);

    int txtStyle = SWT.BORDER;
    if ((style & MULTI_LINE_TEXT_FIELD) != 0) {
      txtStyle = SWT.BORDER | SWT.MULTI | SWT.V_SCROLL;
    }

    m_text = new Text(parent, txtStyle);

    if ((style & VALIDATE_ON_MODIFY) != 0) {
      m_text.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          validateInput();
        }
      });
    }
    else {
      m_text.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          validateInput();
        }
      });
    }

    // layout
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 0);
    labelData.left = new FormAttachment(0, 0);
    if ((style & LABEL_NO_ALIGN) == 0) {
      labelData.right = new FormAttachment(40, 0);
    }
    labelData.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(labelData);

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(m_label, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    m_text.setLayoutData(textData);
  }

  public void addInputChangedListener(IInputChangedListener<T> listener) {
    m_inputChangedListener.add(listener);
  }

  public void removeInputChangedListener(IInputChangedListener<T> listener) {
    m_inputChangedListener.remove(listener);
  }

  public void addValidationListener(IValidationListener listener) {
    m_validationListener.add(listener);
  }

  public void removeValidationListener(IValidationListener listener) {
    m_validationListener.remove(listener);
  }

  private void validateInput() {
    String input = m_text.getText();
    if (input.equals(m_input)) {
      return;
    }
    // validate
    validate();
    if (m_status.isOK()) {
      m_text.setForeground(null);
      // notify
      for (IInputChangedListener<T> listener : m_inputChangedListener) {
        listener.inputChanged(m_inputFormatter.parse(this, input));
      }
    }
    else {
      m_text.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
      // notify
      for (IInputChangedListener<T> listener : m_inputChangedListener) {
        listener.inputChanged(m_inputFormatter.parse(this, null));
      }
    }
    m_input = input;
  }

  public IStatus getStatus() {
    return m_status;
  }

  public IStatus validate() {
    String input = m_text.getText();
    IStatus valid = m_inputValidator.isValid(input);
    m_status = valid;
    if (valid.isOK() != m_valid) {
      // fire
      for (IValidationListener listener : m_validationListener) {
        listener.validationChanged(valid);
      }
      m_valid = valid.isOK();
    }
    return valid;
  }

  @Override
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  @Override
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
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
  public String getToolTipText() {
    return super.getToolTipText();
  }

  public void setValue(T value) {
    m_text.setText(m_inputFormatter.format(this, value));
  }

  public T getValue() {
    return m_inputFormatter.parse(this, m_text.getText());
  }

  public void setEditable(boolean editable) {
    m_text.setEditable(editable);
  }

  public IInputFormatter<T> getInputFormatter() {
    return m_inputFormatter;
  }

  public void setInputFormatter(IInputFormatter<T> inputFormatter) {
    m_inputFormatter = inputFormatter;
  }

  public IInputValidator getInputValidator() {
    return m_inputValidator;
  }

  public void setInputValidator(IInputValidator inputValidator) {
    m_inputValidator = inputValidator;
    m_input = null;
    validateInput();
  }
}
