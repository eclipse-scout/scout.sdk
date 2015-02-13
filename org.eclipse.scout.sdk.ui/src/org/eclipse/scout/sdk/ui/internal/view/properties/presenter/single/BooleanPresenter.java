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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.scout.sdk.workspace.type.config.parser.BooleanPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>BooleanPresenter</h3>
 */
public class BooleanPresenter extends AbstractMethodPresenter {

  private Button m_checkbox;
  private final IPropertySourceParser<Boolean> m_parser;

  public BooleanPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new BooleanPropertySourceParser();
  }

  @Override
  protected Control createContent(Composite container) {
    Composite rootArea = getToolkit().createComposite(container);
    m_checkbox = getToolkit().createButton(rootArea, "", SWT.CHECK);
    m_checkbox.setEnabled(false);
    SelectionListener listener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleCheckboxSelectionChanged();
      }
    };
    m_checkbox.addSelectionListener(listener);
    RowLayout layout = new RowLayout();
    layout.marginBottom = 0;
    layout.marginHeight = 0;
    layout.marginLeft = 0;
    layout.marginRight = 0;
    layout.marginTop = 0;
    layout.spacing = 0;
    rootArea.setLayout(layout);

    return rootArea;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_checkbox.setEnabled(enabled);
      super.setEnabled(enabled);
    }
  }

  @Override
  public boolean isEnabled() {
    if (!isDisposed()) {
      return m_checkbox.getEnabled() && super.isEnabled();
    }
    return false;
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    super.init(method);
    try {
      parseMethodBody();
    }
    catch (JavaModelException e1) {
      ScoutSdkUi.logError("could not parse method body", e1);
    }
    m_checkbox.setEnabled(true);
  }

  protected void parseMethodBody() throws CoreException {
    String value = PropertyMethodSourceUtility.getMethodReturnValue(getMethod().peekMethod());
    boolean checked = m_parser.parseSourceValue(value, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    m_checkbox.setSelection(checked);
  }

  private synchronized void handleCheckboxSelectionChanged() {
    try {
      ConfigPropertyUpdateOperation<Boolean> updateOp = new ConfigPropertyUpdateOperation<>(getMethod(), m_parser);
      updateOp.setValue(Boolean.valueOf(m_checkbox.getSelection()));
      OperationJob job = new OperationJob(updateOp);
      job.setDebug(true);
      job.schedule();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not parse default value of method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
  }
}
