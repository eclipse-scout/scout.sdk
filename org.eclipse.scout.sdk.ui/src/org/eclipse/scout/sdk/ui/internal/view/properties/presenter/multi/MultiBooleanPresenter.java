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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.multi;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.multi.AbstractMultiMethodPresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodBean;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethodSet;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>MultiBooleanPresenter</h3> ...
 */
public class MultiBooleanPresenter extends AbstractMultiMethodPresenter<Boolean> {

  private Button m_checkbox;

  public MultiBooleanPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    m_checkbox = getToolkit().createButton(container, "", SWT.CHECK);
    m_checkbox.setEnabled(false);
    SelectionListener listener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        storeMethods(getMethodBeans());
      }
    };
    m_checkbox.addSelectionListener(listener);
    return m_checkbox;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_checkbox.setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }

  @Override
  public boolean isEnabled() {
    if (!isDisposed()) {
      return m_checkbox.getEnabled() && super.isEnabled();
    }
    return false;
  }

  @Override
  protected void init(ConfigurationMethodSet methodSet) throws CoreException {
    super.init(methodSet);
    MethodBean<Boolean>[] methodBeans = getMethodBeans();
    boolean[] ar = new boolean[methodBeans.length];
    for (int i = 0; i < methodBeans.length; i++) {
      ar[i] = methodBeans[i].getCurrentSourceValue();
    }
    if (!allEqual(ar)) {
      m_checkbox.setText("###");
      m_checkbox.setForeground(m_checkbox.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    }
    else {
      m_checkbox.setText("");
      m_checkbox.setForeground(null);
      m_checkbox.setSelection(ar[0]);
    }
    m_checkbox.setEnabled(true);
  }

  @Override
  protected Boolean parseDisplayInput(String value) throws CoreException {
    return m_checkbox.getSelection();
  }

  @Override
  protected Boolean parseSourceInput(String value, ConfigurationMethod method) throws CoreException {
    Boolean bool = PropertyMethodSourceUtility.parseReturnParameterBoolean(value, method.peekMethod(), method.getSuperTypeHierarchy());
    return bool;
  }

  @Override
  protected String formatDisplayValue(Boolean value) throws CoreException {
    // not used for boolean
    return "";
  }

  @Override
  protected String formatSourceValue(Boolean value) throws CoreException {
    return Boolean.toString(value);
  }

  protected synchronized void storeMethods(MethodBean<Boolean>[] beans) {

    ArrayList<IOperation> list = new ArrayList<IOperation>();
    for (MethodBean<Boolean> bean : beans) {
      try {
        String sourceValue = formatSourceValue(m_checkbox.getSelection());
        ConfigurationMethod method = bean.getMethod();
        if (UiUtility.equals(method.computeDefaultValue(), sourceValue)) {
          if (method.isImplemented()) {
            list.add(new ScoutMethodDeleteOperation(method.peekMethod()));
          }
        }
        else {
          list.add(new ConfigPropertyMethodUpdateOperation(method.getType(), method.getMethodName(), "return " + sourceValue + ";", true));
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("could not format source value", e);
      }
    }
    new OperationJob(list).schedule();
  }

  private boolean allEqual(boolean[] ar) {
    if (ar.length > 0) {
      boolean equal = ar[0];
      for (boolean b : ar) {
        if (b != equal) {
          return false;
        }
      }
    }
    return true;

  }

}
