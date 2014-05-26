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
package org.eclipse.scout.sdk.ui.view.properties.presenter.multi;

import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodBean;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>AbstractMultiValuePresenter</h3> ...
 * 
 * @param <T>
 */
public abstract class AbstractMultiValuePresenter<T> extends AbstractMultiMethodPresenter<T> {

  private Text m_textComponent;
  private final Pattern m_regexAllowedCharacters;

  public AbstractMultiValuePresenter(PropertyViewFormToolkit toolkit, Composite parent, String regexAllowedInput) {
    super(toolkit, parent);
    m_regexAllowedCharacters = Pattern.compile(regexAllowedInput);
  }

  @Override
  protected Control createContent(Composite container) {
    m_textComponent = getToolkit().createText(container, "", getTextAlignment());
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

  protected abstract void storeMethods(Collection<MethodBean<T>> beans, T value);

  protected int getTextAlignment() {
    return SWT.LEFT;
  }

  public Text getTextComponent() {
    return m_textComponent;
  }

  private class P_TextListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Verify:
          String string = event.text;
          if (SdkProperties.INPUT_MULTI_UNDEFINED.equals(string)) {
            // allow undefined focus lost
            return;
          }
          if (string != null) {
            event.doit = m_regexAllowedCharacters.matcher(string).matches();
          }
          break;
        case SWT.FocusOut:

          String input = getTextComponent().getText();
          if (SdkProperties.INPUT_MULTI_UNDEFINED.equals(input)) {
            // allow undefined focus lost
            return;
          }
          try {
            T value = parseDisplayInput(input);
            getTextComponent().setText(formatDisplayValue(value));
            storeMethods(getMethodBeans(), value);
          }
          catch (CoreException e) {
            ScoutSdkUi.logInfo("input is not well fomed " + input);
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
