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
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.multi.AbstractMultiMethodPresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodBean;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethodSet;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.scout.sdk.workspace.type.config.parser.BooleanPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>MultiBooleanPresenter</h3> ...
 */
public class MultiBooleanPresenter extends AbstractMultiMethodPresenter<Boolean> {

  private Button m_checkbox;

  public MultiBooleanPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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
    Collection<MethodBean<Boolean>> methodBeans = getMethodBeans();
    if (!allEqual(methodBeans)) {
      m_checkbox.setText("###");
      m_checkbox.setForeground(m_checkbox.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    }
    else {
      m_checkbox.setText("");
      m_checkbox.setForeground(null);
      m_checkbox.setSelection(methodBeans.iterator().next().getCurrentSourceValue());
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

  protected synchronized void storeMethods(Collection<MethodBean<Boolean>> beans) {
    ArrayList<IOperation> list = new ArrayList<IOperation>(beans.size());
    for (MethodBean<Boolean> bean : beans) {
      ConfigurationMethod method = bean.getMethod();
      ConfigPropertyUpdateOperation<Boolean> updateOp = new ConfigPropertyUpdateOperation<Boolean>(method, new BooleanPropertySourceParser());
      updateOp.setValue(m_checkbox.getSelection());
      list.add(updateOp);
    }
    new OperationJob(list).schedule();
  }
}
