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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class NewHandlerCreateMethodsOperation implements IOperation {

  private boolean m_createExecStore = false;
  private boolean m_createExecLoad = false;
  private IType m_formHandler;
  private IType m_formData;
  private IType m_serviceInterface;

  @Override
  public String getOperationName() {
    return "create new handler content";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getFormHandler() == null) {
      throw new IllegalArgumentException("Form Hanlder can not be null.");
    }
    if (getFormData() == null) {
      throw new IllegalArgumentException("FormData can not be null.");
    }
    if (getServiceInterface() == null) {
      throw new IllegalArgumentException("Service interface can not be null.");
    }
    if (isCreateExecLoad()) {
      if (!TypeUtility.exists(TypeUtility.getMethod(getServiceInterface(), "prepareCreate"))) {
        throw new IllegalArgumentException("prepareCreate method mission in '" + getServiceInterface().getFullyQualifiedName() + "'.");
      }
    }
    if (isCreateExecStore()) {
      if (!TypeUtility.exists(TypeUtility.getMethod(getServiceInterface(), "create"))) {
        throw new IllegalArgumentException("create method mission in '" + getServiceInterface().getFullyQualifiedName() + "'.");
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (getServiceInterface() == null) {
      return;
    }
    String TAB = SdkProperties.TAB;
    CompilationUnitImportValidator validator = new CompilationUnitImportValidator(getFormHandler().getCompilationUnit());
    workingCopyManager.register(getFormHandler().getCompilationUnit(), monitor);
    String processingExceptionClass = SignatureUtility.getTypeReference(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true), validator);
    String serviceInterfaceName = SignatureUtility.getTypeReference(Signature.createTypeSignature(getServiceInterface().getFullyQualifiedName(), true), validator);
    String servicesName = SignatureUtility.getTypeReference(Signature.createTypeSignature(RuntimeClasses.SERVICES, true), validator);
    String formDataName = SignatureUtility.getTypeReference(Signature.createTypeSignature(getFormData().getFullyQualifiedName(), true), validator);
    if (isCreateExecLoad()) {
      // execLoad on formhandler
      StringBuilder execLoadBuilder = new StringBuilder();
      execLoadBuilder.append("@Override\n");
      execLoadBuilder.append("public void execLoad() throws " + processingExceptionClass + "{\n");
      execLoadBuilder.append(TAB + serviceInterfaceName + " service = " + servicesName + ".getService(" + serviceInterfaceName + ".class);\n");
      execLoadBuilder.append(TAB + formDataName + " formData = new " + formDataName + "();\n");
      execLoadBuilder.append(TAB + "exportFormData(formData);\n");
      execLoadBuilder.append(TAB + "formData = service.prepareCreate(formData);\n");
      execLoadBuilder.append(TAB + "importFormData(formData);\n");
      execLoadBuilder.append("}");
      getFormHandler().createMethod(execLoadBuilder.toString(), null, true, monitor);
    }
    if (isCreateExecStore()) {
      // execLoad on formhandler
      StringBuilder execLoadBuilder = new StringBuilder();
      execLoadBuilder.append("@Override\n");
      execLoadBuilder.append("public void execStore() throws " + processingExceptionClass + "{\n");
      execLoadBuilder.append(TAB + serviceInterfaceName + " service = " + servicesName + ".getService(" + serviceInterfaceName + ".class);\n");
      execLoadBuilder.append(TAB + formDataName + " formData = new " + formDataName + "();\n");
      execLoadBuilder.append(TAB + "exportFormData(formData);\n");
      execLoadBuilder.append(TAB + "formData = service.create(formData);\n");
      execLoadBuilder.append("}");
      getFormHandler().createMethod(execLoadBuilder.toString(), null, true, monitor);
    }
    // imports
    for (String s : validator.getImportsToCreate()) {
      getFormHandler().getCompilationUnit().createImport(s, null, monitor);

    }
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

}
