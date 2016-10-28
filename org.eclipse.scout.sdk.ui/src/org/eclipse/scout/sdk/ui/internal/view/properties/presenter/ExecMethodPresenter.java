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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class ExecMethodPresenter extends AbstractMethodPresenter {

  private ImageHyperlink m_addButton;

  public ExecMethodPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    return null;
  }

  @Override
  protected void fillButtonArea(Composite buttonArea) {
    super.fillButtonArea(buttonArea);
    m_addButton = getToolkit().createImageHyperlink(buttonArea, SWT.PUSH);
    m_addButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolAdd));
    m_addButton.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        overrideMethod();
      }
    });
    GridData buttonData = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
    m_addButton.setLayoutData(buttonData);
  }

  @Override
  protected void handleLabelLinkSelected() {
    if (getMethod().isImplemented()) {
      // if the method exists in the current file -> show
      showJavaElementInEditor(getMethod().peekMethod());
    }
    else {
      // if the method does not exist -> add and show
      overrideMethod();
    }
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    super.init(method);
    ((GridData) m_addButton.getLayoutData()).exclude = getMethod().isImplemented();
    m_addButton.setVisible(!getMethod().isImplemented());
    m_addButton.setToolTipText(Texts.get("ImplementXInY", getMethod().getMethodName(), getMethod().getType().getElementName()));

  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (!isDisposed()) {
      m_addButton.setEnabled(enabled);
    }
  }

  protected void overrideMethod() {
    if (!getMethod().isImplemented()) {
      try {
        MethodOverrideOperation overrideOp = new MethodOverrideOperation(getMethod().getDefaultMethod(), getMethod().getType());
        overrideOp.setFormatSource(true);
        IJavaElement sibling = null;
        IStructuredType structuredType = ScoutTypeUtility.createStructuredType(getMethod().getType());
        sibling = structuredType.getSiblingMethodConfigExec(getMethod().getMethodName());
        overrideOp.setSibling(sibling);
        OperationJob job = new OperationJob(overrideOp);
        job.schedule();
        try {
          job.join();
        }
        catch (InterruptedException e) {
        }
        if (overrideOp.getCreatedMethod() != null) {
          showJavaElementInEditor(overrideOp.getCreatedMethod());
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logWarning("could not override the method '" + getMethod().getMethodName() + "' on '" + getMethod().getType().getFullyQualifiedName() + "'", e);
      }
    }
  }
}