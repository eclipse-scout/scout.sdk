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

import java.util.regex.Matcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>BooleanPresenter</h3> ...
 */
public class BooleanPresenter extends AbstractMethodPresenter {

  private Button m_checkbox;
  private boolean m_defaultValue;

  public BooleanPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
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

    m_defaultValue = Boolean.valueOf(getMethod().computeDefaultValue());
    try {
      parseMethodBody();
    }
    catch (JavaModelException e1) {
      ScoutSdkUi.logError("could not parse method body", e1);
    }
    m_checkbox.setEnabled(true);
  }

  protected void parseMethodBody() throws CoreException {
    Matcher m = Regex.REGEX_PROPERTY_METHOD_REPRESENTER_BOOLEAN.matcher(getMethod().getSource());
    if (m.find()) {
      m_checkbox.setSelection(Boolean.valueOf(m.group(1)));
    }
    else {
      throw new CoreException(new ScoutStatus(getMethod().getSource()));
    }
  }

  private synchronized void handleCheckboxSelectionChanged() {
    IOperation op = null;
    String sourceValue = Boolean.toString(m_checkbox.getSelection());
    if (ScoutSdkUtility.equals(getMethod().computeDefaultValue(), sourceValue)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", false);
    }
    if (op != null) {
      OperationJob job = new OperationJob(op);
      job.setDebug(true);
      job.schedule();
//      try {
//        job.join();
//        if (op instanceof ConfigPropertyMethodUpdateOperation) {
//          showJavaElementInEditor(((ConfigPropertyMethodUpdateOperation) op).getUpdatedMethod(), false);
//
//        }
//      }
//      catch (InterruptedException e) {
//        ScoutSdkUi.logWarning("job interrupted.", e);
//      }
    }
  }

}
