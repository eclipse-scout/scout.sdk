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
package org.eclipse.scout.sdk.ui.wizard.services;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.service.ParameterArgument;
import org.eclipse.scout.sdk.operation.service.ServiceOperationNewOperation;
import org.eclipse.scout.sdk.ui.fields.TextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.code.IParameterFieldListener;
import org.eclipse.scout.sdk.ui.internal.fields.code.ParameterField;
import org.eclipse.scout.sdk.ui.internal.fields.code.ReturnParameterField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ServiceOperationNewWizardPage extends AbstractWorkspaceWizardPage {

  // ui fields
  private TextField m_operationNameField;
  private ReturnParameterField m_returnParameterField;

  // fields
  private String m_operationName;
  private ParameterArgument m_returnParameter;
  private ParameterArgument m_parameterArg1;
  private ParameterArgument m_parameterArg2;

  // local fields
  private final IType m_serviceImplementation;
  private final IType m_serviceInterface;
  private ParameterField m_parameterArg1Field;
  private ParameterField m_parameterArg2Field;

  public ServiceOperationNewWizardPage(IType serviceInterface, IType serviceImplementation) {
    super(ServiceOperationNewWizardPage.class.getName());
    m_serviceInterface = serviceInterface;
    m_serviceImplementation = serviceImplementation;
    setTitle(Texts.get("NewServiceOperationNoPopup"));
    setDescription(Texts.get("CreateANewServiceOperation"));
  }

  @Override
  protected void createContent(Composite parent) {
    m_operationNameField = new TextField(parent, 20);
    m_operationNameField.setLabelText(Texts.get("OperationName"));
    m_operationNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_operationName = m_operationNameField.getText();
        pingStateChanging();
      }
    });

    IScoutBundle interfaceBundle = ScoutTypeUtility.getScoutBundle(m_serviceInterface.getJavaProject());
    IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[]{interfaceBundle.getJavaProject()});

    m_returnParameterField = new ReturnParameterField(parent, 20, m_returnParameter, searchScope);
    m_returnParameterField.setLabel(Texts.get("ReturnType"));
    m_returnParameterField.addParameterFieldListener(new IParameterFieldListener() {
      @Override
      public void parameterChanged(ParameterArgument argument) {
        m_returnParameter = argument;
        pingStateChanging();
      }
    });

    m_parameterArg1Field = new ParameterField(parent, m_parameterArg1, searchScope);
    m_parameterArg1Field.setLabelParameterName(Texts.get("NameArg") + " 1");
    m_parameterArg1Field.setLabelParameterType(Texts.get("Type"));
    m_parameterArg1Field.addParameterFieldListener(new IParameterFieldListener() {
      @Override
      public void parameterChanged(ParameterArgument argument) {
        m_parameterArg1 = argument;
        pingStateChanging();
      }
    });

    m_parameterArg2Field = new ParameterField(parent, m_parameterArg1, searchScope);
    m_parameterArg2Field.setLabelParameterName(Texts.get("NameArg") + "2");
    m_parameterArg2Field.setLabelParameterType(Texts.get("Type"));
    m_parameterArg2Field.addParameterFieldListener(new IParameterFieldListener() {
      @Override
      public void parameterChanged(ParameterArgument argument) {
        m_parameterArg2 = argument;
        pingStateChanging();
      }
    });

    parent.setLayout(new GridLayout(1, true));
    m_operationNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_returnParameterField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_parameterArg1Field.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_parameterArg2Field.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    ServiceOperationNewOperation op = new ServiceOperationNewOperation();
    ArrayList<ParameterArgument> args = new ArrayList<ParameterArgument>();
    if (m_parameterArg1 != null && !StringUtility.isNullOrEmpty(m_parameterArg1.getName()) && !StringUtility.isNullOrEmpty(m_parameterArg1.getType())) {
      args.add(m_parameterArg1);
    }
    if (m_parameterArg2 != null && !StringUtility.isNullOrEmpty(m_parameterArg2.getName()) && !StringUtility.isNullOrEmpty(m_parameterArg2.getType())) {
      args.add(m_parameterArg2);
    }
    op.setArguments(args.toArray(new ParameterArgument[args.size()]));
    op.setMethodName(m_operationName);
    op.setReturnType(m_returnParameter);
    op.setServiceImplementation(m_serviceImplementation);
    op.setServiceInterface(m_serviceInterface);
    op.run(monitor, manager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getOperationNameStatus());
    multiStatus.add(getReturnParameterStatus());
    if (m_parameterArg1 != null) {
      multiStatus.add(getParameterStatus(m_parameterArg1, m_parameterArg1Field.getLabel()));
    }
    if (m_parameterArg2 != null) {
      multiStatus.add(getParameterStatus(m_parameterArg1, m_parameterArg2Field.getLabel()));
    }
    super.validatePage(multiStatus);
  }

  protected IStatus getOperationNameStatus() {
    if (m_operationName == null || m_operationName.length() == 0) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("OperationNameMissing"));
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected IStatus getReturnParameterStatus() {
    if (m_returnParameter == null || StringUtility.isNullOrEmpty(m_returnParameter.getType())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ReturnTypeMissing"));
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected IStatus getParameterStatus(ParameterArgument arg, String fieldName) {
    if (StringUtility.isNullOrEmpty(arg.getName()) && !StringUtility.isNullOrEmpty(arg.getType())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ParameterXIsNotValid", fieldName));
    }
    else if (!StringUtility.isNullOrEmpty(arg.getName()) && StringUtility.isNullOrEmpty(arg.getType())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ParameterXIsNotValid", fieldName));
    }
    return Status.OK_STATUS;
  }
}
