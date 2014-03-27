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
package org.eclipse.scout.sdk.operation.form;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.icu.ImportsCreateOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ModifyHandlerCreateMethodsOperation implements IOperation {

  private boolean m_createExecStore = false;
  private boolean m_createExecLoad = false;
  private IType m_formHandler;
  private IType m_formData;
  private IType m_serviceInterface;
  private IType m_updatePermission;

  @Override
  public String getOperationName() {
    return "create modify handler content";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getFormHandler() == null) {
      throw new IllegalArgumentException("Form Handler can not be null.");
    }
    if (getFormData() == null) {
      throw new IllegalArgumentException("FormData can not be null.");
    }
    if (getServiceInterface() == null) {
      throw new IllegalArgumentException("Service interface can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String TAB = SdkProperties.TAB;
    ImportValidator validator = new ImportValidator(getFormHandler().getCompilationUnit());
    workingCopyManager.register(getFormHandler().getCompilationUnit(), monitor);
    String processingExceptionClass = SignatureUtility.getTypeReferenceFromFqn(IRuntimeClasses.ProcessingException, validator);
    String serviceInterfaceName = SignatureUtility.getTypeReferenceFromFqn(getServiceInterface().getFullyQualifiedName(), validator);
    String servicesName = SignatureUtility.getTypeReferenceFromFqn(IRuntimeClasses.SERVICES, validator);
    String formDataName = SignatureUtility.getTypeReferenceFromFqn(getFormData().getFullyQualifiedName(), validator);
    if (isCreateExecLoad()) {
      // execLoad on formhandler
      StringBuilder execLoadBuilder = new StringBuilder();
      execLoadBuilder.append("@Override\n");
      execLoadBuilder.append("public void execLoad() throws " + processingExceptionClass + "{\n");
      execLoadBuilder.append(TAB + serviceInterfaceName + " service = " + servicesName + ".getService(" + serviceInterfaceName + ".class);\n");
      execLoadBuilder.append(TAB + formDataName + " formData = new " + formDataName + "();\n");
      execLoadBuilder.append(TAB + "exportFormData(formData);\n");
      if (TypeUtility.exists(TypeUtility.getMethod(getServiceInterface(), "load"))) {
        execLoadBuilder.append(TAB + "formData = service.load(formData);\n");
      }
      else {
        execLoadBuilder.append(TAB + ScoutUtility.getCommentBlock("service call here") + "\n");
      }
      execLoadBuilder.append(TAB + "importFormData(formData);\n");
      if (getUpdatePermission() != null) {
        execLoadBuilder.append(TAB + "setEnabledPermission(new " + SignatureUtility.getTypeReferenceFromFqn(getUpdatePermission().getFullyQualifiedName(), validator) + "());\n");
      }
      execLoadBuilder.append("}");
      getFormHandler().createMethod(execLoadBuilder.toString(), null, true, monitor);
    }
    if (isCreateExecStore()) {
      // execLoad on formhandler
      StringBuilder execStoreBuilder = new StringBuilder();
      execStoreBuilder.append("@Override\n");
      execStoreBuilder.append("public void execStore() throws " + processingExceptionClass + "{\n");
      execStoreBuilder.append(TAB + serviceInterfaceName + " service = " + servicesName + ".getService(" + serviceInterfaceName + ".class);\n");
      execStoreBuilder.append(TAB + formDataName + " formData = new " + formDataName + "();\n");
      execStoreBuilder.append(TAB + "exportFormData(formData);\n");
      if (TypeUtility.exists(TypeUtility.getMethod(getServiceInterface(), "store"))) {
        execStoreBuilder.append(TAB + "formData = service.store(formData);\n");
      }
      else {
        execStoreBuilder.append(TAB + ScoutUtility.getCommentBlock("service call here") + "\n");
      }
      execStoreBuilder.append("}");
      getFormHandler().createMethod(execStoreBuilder.toString(), null, true, monitor);
    }
    // imports
    new ImportsCreateOperation(getFormHandler().getCompilationUnit(), validator).run(monitor, workingCopyManager);
  }

  public boolean isCreateExecStore() {
    return m_createExecStore;
  }

  public void setCreateExecStore(boolean createExecStore) {
    m_createExecStore = createExecStore;
  }

  public boolean isCreateExecLoad() {
    return m_createExecLoad;
  }

  public void setCreateExecLoad(boolean createExecLoad) {
    m_createExecLoad = createExecLoad;
  }

  public IType getFormHandler() {
    return m_formHandler;
  }

  public void setFormHandler(IType formHandler) {
    m_formHandler = formHandler;
  }

  public IType getFormData() {
    return m_formData;
  }

  public void setFormData(IType formData) {
    m_formData = formData;
  }

  public IType getServiceInterface() {
    return m_serviceInterface;
  }

  public void setServiceInterface(IType serviceInterface) {
    m_serviceInterface = serviceInterface;
  }

  public void setUpdatePermission(IType updatePermission) {
    m_updatePermission = updatePermission;
  }

  public IType getUpdatePermission() {
    return m_updatePermission;
  }

}
