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
package org.eclipse.scout.sdk.ui.view.properties.presenter.single;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>AbstractValuePresenter</h3> ...
 */
public abstract class AbstractValuePresenter<T> extends AbstractMethodPresenter {

  private Text m_textComponent;
  private final Pattern m_regexAllowedCharacters;
  private T m_currentSourceValue;
  private T m_defaultValue;

  public AbstractValuePresenter(PropertyViewFormToolkit toolkit, Composite parent, String regexAllowedCharacters) {
    super(toolkit, parent);
    if (regexAllowedCharacters == null) {
      m_regexAllowedCharacters = null;
    }
    else {
      m_regexAllowedCharacters = Pattern.compile(regexAllowedCharacters);
    }
  }

  @Override
  protected Control createContent(Composite container) {
    m_textComponent = getToolkit().createText(container, "", SWT.BORDER | getTextAlignment() | (isMultiLine() ? SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL : 0));
    m_textComponent.setEnabled(false);
    P_TextListener listener = new P_TextListener();
    m_textComponent.addListener(SWT.FocusIn, listener);
    m_textComponent.addListener(SWT.FocusOut, listener);
    m_textComponent.addListener(SWT.Verify, listener);
    return m_textComponent;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_textComponent.setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }

  @Override
  public boolean isEnabled() {
    if (!isDisposed()) {
      return m_textComponent.getEnabled() && super.isEnabled();
    }
    return false;
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    super.init(method);
    String defValue = getMethod().computeDefaultValue();
    String value = getMethod().computeValue();
    m_defaultValue = parseSourceInput(defValue);

    if (CompareUtility.equals(defValue, value)) {
      // if the default value and the value are the same: directly use the already parsed default value.
      setCurrentSourceValue(m_defaultValue);
    }
    else {
      // only parse, if the value is different than the default value.
      setCurrentSourceValue(parseSourceInput(value));
    }

    String initialText = formatDisplayValue(getCurrentSourceValue());
    m_textComponent.setText(initialText);
    m_textComponent.setEnabled(true);
  }

  /**
   * to write the value to the ui component
   * 
   * @param value
   * @return
   * @throws CoreException
   */
  protected abstract String formatDisplayValue(T value) throws CoreException;

  /**
   * to parse the source input
   * 
   * @param input
   * @return
   * @throws CoreException
   */
  protected abstract T parseSourceInput(String input) throws CoreException;

  /**
   * to parse the display input
   * 
   * @param input
   * @return
   * @throws CoreException
   */
  protected abstract T parseDisplayInput(String input) throws CoreException;

  protected abstract void storeValue(T value) throws CoreException;

  protected int getTextAlignment() {
    return SWT.LEFT;
  }

  protected final void setCurrentSourceValue(T value) {
    if (CompareUtility.notEquals(value, m_currentSourceValue)) {
      m_currentSourceValue = value;
      execCurrentSourceValueChanged(m_currentSourceValue);
    }
  }

  protected void execCurrentSourceValueChanged(T value) {
  }

  public T getCurrentSourceValue() {
    return m_currentSourceValue;
  }

  public T getDefaultValue() {
    return m_defaultValue;
  }

  public Text getTextComponent() {
    return m_textComponent;
  }

  private class P_TextListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Verify:
          if (m_regexAllowedCharacters != null) {
            String string = event.text;
            if (string != null) {
              event.doit = m_regexAllowedCharacters.matcher(string).matches();
            }
          }
          break;
        case SWT.FocusOut:
          String input = getTextComponent().getText();
          try {
            T value = parseDisplayInput(input);
            String displayValue = formatDisplayValue(value);
            if (displayValue == null) {
              displayValue = "";
            }
            getTextComponent().setText(displayValue);
            if (!CompareUtility.equals(value, getCurrentSourceValue())) {
              storeValue(value);
              setCurrentSourceValue(value);
            }
          }
          catch (CoreException e) {
            ScoutSdkUi.logInfo("input is not well formed " + input);
            getTextComponent().setForeground(getTextComponent().getDisplay().getSystemColor(SWT.COLOR_RED));
          }
          break;
        case SWT.FocusIn:
          getTextComponent().setForeground(null);
          break;

        default:
          break;
      }
    }
  } // end class P_TextListener
}
