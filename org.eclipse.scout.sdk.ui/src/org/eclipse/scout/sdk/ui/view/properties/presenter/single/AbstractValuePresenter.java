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

import java.io.BufferedReader;
import java.io.Reader;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.LegacyOperationAction;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>AbstractValuePresenter</h3> ...
 */
@SuppressWarnings("deprecation")
public abstract class AbstractValuePresenter<T> extends AbstractMethodPresenter {

  private Text m_textComponent;
  private final String m_regexAllowedCharacters;
  private T m_currentSourceValue;
  private T m_defaultValue;

  public AbstractValuePresenter(FormToolkit toolkit, Composite parent, String regexAllowedCharacters) {
    super(toolkit, parent);
    m_regexAllowedCharacters = regexAllowedCharacters;
  }

  @Override
  protected Control createContent(Composite container) {
    m_textComponent = getToolkit().createText(container, "", SWT.BORDER | getTextAlignment() | (isMultiLine() ? SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL : 0));
    m_textComponent.setEnabled(false);
    P_TextListener listener = new P_TextListener();
    m_textComponent.addListener(SWT.FocusIn, listener);
    m_textComponent.addListener(SWT.FocusOut, listener);
    m_textComponent.addListener(SWT.Verify, listener);
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    Menu menu = manager.createContextMenu(m_textComponent);
    manager.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager managerInside) {
        managerInside.removeAll();
        createContextMenu((MenuManager) managerInside);
      }
    });
    m_textComponent.setMenu(menu);
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
    m_defaultValue = parseSourceInput(getMethod().computeDefaultValue());
    setCurrentSourceValue(parseSourceInput(getMethod().computeValue()));
    String initialText = formatDisplayValue(getCurrentSourceValue());
    m_textComponent.setText(initialText);
    m_textComponent.setEnabled(true);

  }

  /**
   * to write the value to the source
   * 
   * @param value
   * @return
   * @throws CoreException
   */
  protected abstract String formatSourceValue(T value) throws CoreException;

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

  protected void createContextMenu(MenuManager manager) {
    if (getMethod() != null && getMethod().isImplemented()) {
      manager.add(new LegacyOperationAction(Texts.get("SetDefaultValue"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.StatusInfo), new ScoutMethodDeleteOperation(getMethod().peekMethod())));
    }
  }

  private class P_TextListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Verify:
          if (m_regexAllowedCharacters != null) {
            String string = event.text;
            if (string != null) {
              event.doit = Pattern.matches(m_regexAllowedCharacters, string);
              if (!event.doit) {
                ScoutSdkUi.logInfo("not allowed input: " + string);
              }
            }
          }
          break;
        case SWT.FocusOut:
          String input = getTextComponent().getText();
          try {
            T value = parseDisplayInput(input);
            String displayValue = formatDisplayValue(value);
            getTextComponent().setText(displayValue);
            if (!CompareUtility.equals(value, getCurrentSourceValue())) {
              storeValue(value);
              setCurrentSourceValue(value);
            }
          }
          catch (CoreException e) {
            ScoutSdkUi.logInfo("input is not well fomed " + input);
            getTextComponent().setForeground(getTextComponent().getDisplay().getSystemColor(SWT.COLOR_RED));
          }
          break;
        case SWT.FocusIn:
          getJavaDoc();
          getTextComponent().setForeground(null);
          break;

        default:
          break;
      }
    }
  } // end class P_TextListener

  @Override
  protected String getJavaDoc() {
    String javaDoc = null;
    try {
      Reader contentReader = JavadocContentAccess.getHTMLContentReader(getMethod().peekMethod(), true, false);
      if (contentReader != null) {
        BufferedReader bf = new BufferedReader(contentReader);
        System.out.println(bf.readLine());
      }
    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return javaDoc;
  }

}
