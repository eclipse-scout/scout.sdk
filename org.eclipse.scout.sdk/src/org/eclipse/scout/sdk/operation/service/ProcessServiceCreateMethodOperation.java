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
package org.eclipse.scout.sdk.operation.service;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class ProcessServiceCreateMethodOperation implements IOperation {
  private boolean m_createPrepareCreateMethod = false;
  private boolean m_createCreateMethod = false;
  private boolean m_createLoadMethod = false;
  private boolean m_createStoreMethod = false;
  private IType m_formData;
  private IType m_serviceInterface;
  private IType[] m_serviceImplementations = new IType[0];
  private IType m_createPermission;
  private IType m_readPermission;
  private IType m_updatePermission;

  // internal members
  private final HashMap<String, IImportValidator> m_importValidator = new HashMap<String, IImportValidator>();
  private static String TAB = SdkProperties.TAB;

  @Override
  public String getOperationName() {
    return "create process service methods of '" + getServiceInterface().getElementName() + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getServiceInterface() == null) {
      throw new IllegalArgumentException("Service interface can not be null.");
    }
    if (getFormData() == null) {
      throw new IllegalArgumentException("Form data can not be null.");
    }
    if ((isCreateCreateMethod() || isCreatePrepareCreateMethod()) && getCreatePermission() == null) {
      throw new IllegalArgumentException("Create Permission can not be null.");
    }
    if (isCreateLoadMethod() && getReadPermission() == null) {
      throw new IllegalArgumentException("Read Permission can not be null.");
    }
    if (isCreateStoreMethod() && getUpdatePermission() == null) {
      throw new IllegalArgumentException("Update Permission can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // prepare
    if (isCreatePrepareCreateMethod()) {
      // interface
      createPrepareCreateMethod(getServiceInterface(), ScoutTypeUtility.findNlsProject(getServiceInterface()), monitor, workingCopyManager);
      // implementation
      for (IType impl : getServiceImplementations()) {
        createPrepareCreateMethod(impl, ScoutTypeUtility.findNlsProject(impl), monitor, workingCopyManager);
      }
    }
    // create
    if (isCreateCreateMethod()) {
      // interface
      createCreateMethod(getServiceInterface(), ScoutTypeUtility.findNlsProject(getServiceInterface()), monitor, workingCopyManager);
      // implementation
      for (IType impl : getServiceImplementations()) {
        createCreateMethod(impl, ScoutTypeUtility.findNlsProject(impl), monitor, workingCopyManager);
      }
    }
    // load
    if (isCreateLoadMethod()) {
      // interface
      createLoadMethod(getServiceInterface(), ScoutTypeUtility.findNlsProject(getServiceInterface()), monitor, workingCopyManager);
      // implementation
      for (IType impl : getServiceImplementations()) {
        createLoadMethod(impl, ScoutTypeUtility.findNlsProject(impl), monitor, workingCopyManager);
      }
    }
    // store
    if (isCreateStoreMethod()) {
      // interface
      createStoreMethod(getServiceInterface(), ScoutTypeUtility.findNlsProject(getServiceInterface()), monitor, workingCopyManager);
      // implementation
      for (IType impl : getServiceImplementations()) {
        createStoreMethod(impl, ScoutTypeUtility.findNlsProject(impl), monitor, workingCopyManager);
      }
    }
    for (IType impl : m_serviceImplementations) {
      IImportValidator val = getOrCreateValidator(impl.getCompilationUnit());
      for (String imp : val.getImportsToCreate()) {
        impl.getCompilationUnit().createImport(imp, null, monitor);
      }
    }
    for (String imp : getOrCreateValidator(getServiceInterface().getCompilationUnit()).getImportsToCreate()) {
      getServiceInterface().getCompilationUnit().createImport(imp, null, monitor);
    }

  }

  private void createCreateMethod(IType parentType, INlsProject nlsProject, IProgressMonitor monitor, IWorkingCopyManager manager) throws JavaModelException {
    IMethod createMethod = TypeUtility.getMethod(parentType, "create");
    if (TypeUtility.exists(createMethod)) {
      return;
    }
    manager.register(parentType.getCompilationUnit(), monitor);
    IImportValidator impValidator = getOrCreateValidator(parentType.getCompilationUnit());
    String formDataName = SignatureUtility.getTypeReferenceFromFqn(getFormData().getFullyQualifiedName(), impValidator);
    String processingExceptionName = SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ProcessingException, impValidator);
    StringBuilder builder = new StringBuilder();
    if (!parentType.isInterface()) {
      builder.append("@" + Override.class.getSimpleName() + "\n");
      builder.append("public ");
    }
    builder.append(formDataName + " create(" + formDataName + " formData) throws " + processingExceptionName);
    if (parentType.isInterface()) {
      builder.append(";");
    }
    else {
      builder.append("{\n");
      if (getCreatePermission() != null) {
        builder.append(TAB + "if(!" + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, impValidator) + ".check(new " + SignatureUtility.getTypeReferenceFromFqn(getCreatePermission().getFullyQualifiedName(), impValidator) + "())){\n");
        builder.append(TAB + TAB + "throw new " + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, impValidator) + "(" + SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), impValidator) + ".get(\"" + SdkProperties.TEXT_AUTHORIZATION_FAILED + "\"));\n");
        builder.append(TAB + "}\n");
      }
      builder.append(TAB + ScoutUtility.getCommentBlock("business logic here.") + "\n");
      builder.append(TAB + "return formData;\n");
      builder.append("}");
    }
    parentType.createMethod(builder.toString(), null, true, monitor);
  }

  private void createLoadMethod(IType parentType, INlsProject nlsProject, IProgressMonitor monitor, IWorkingCopyManager manager) throws JavaModelException {
    IMethod loadMethod = TypeUtility.getMethod(parentType, "load");
    if (TypeUtility.exists(loadMethod)) {
      return;
    }
    manager.register(parentType.getCompilationUnit(), monitor);
    IImportValidator impValidator = getOrCreateValidator(parentType.getCompilationUnit());
    String formDataName = SignatureUtility.getTypeReferenceFromFqn(getFormData().getFullyQualifiedName(), impValidator);
    String processingExceptionName = SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ProcessingException, impValidator);
    StringBuilder builder = new StringBuilder();
    if (!parentType.isInterface()) {
      builder.append("@" + Override.class.getSimpleName() + "\n");
      builder.append("public ");
    }
    builder.append(formDataName + " load(" + formDataName + " formData) throws " + processingExceptionName);
    if (parentType.isInterface()) {
      builder.append(";");
    }
    else {
      builder.append("{\n");
      if (getReadPermission() != null) {
        builder.append(TAB + "if(!" + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, impValidator) + ".check(new " + SignatureUtility.getTypeReferenceFromFqn(getReadPermission().getFullyQualifiedName(), impValidator) + "())){\n");
        builder.append(TAB + TAB + "throw new " + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, impValidator) + "(" + SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), impValidator) + ".get(\"" + SdkProperties.TEXT_AUTHORIZATION_FAILED + "\"));\n");
        builder.append(TAB + "}\n");
      }
      builder.append(TAB + ScoutUtility.getCommentBlock("business logic here") + "\n");
      builder.append(TAB + "return formData;\n");
      builder.append("}");
    }
    parentType.createMethod(builder.toString(), null, true, monitor);
  }

  private void createPrepareCreateMethod(IType parentType, INlsProject nlsProject, IProgressMonitor monitor, IWorkingCopyManager manager) throws JavaModelException {
    IMethod prepareCreateMethod = TypeUtility.getMethod(parentType, "prepareCreate");
    if (TypeUtility.exists(prepareCreateMethod)) {
      return;
    }
    manager.register(parentType.getCompilationUnit(), monitor);
    IImportValidator impValidator = getOrCreateValidator(parentType.getCompilationUnit());
    String formDataName = SignatureUtility.getTypeReferenceFromFqn(getFormData().getFullyQualifiedName(), impValidator);
    String processingExceptionName = SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ProcessingException, impValidator);
    StringBuilder builder = new StringBuilder();
    if (!parentType.isInterface()) {
      builder.append("@" + Override.class.getSimpleName() + "\n");
      builder.append("public ");
    }
    builder.append(formDataName + " prepareCreate(" + formDataName + " formData) throws " + processingExceptionName);
    if (parentType.isInterface()) {
      builder.append(";");
    }
    else {
      builder.append("{\n");
      if (getCreatePermission() != null) {
        builder.append(TAB + "if(!" + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, impValidator) + ".check(new " + SignatureUtility.getTypeReferenceFromFqn(getCreatePermission().getFullyQualifiedName(), impValidator) + "())){\n");
        builder.append(TAB + TAB + "throw new " + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, impValidator) + "(" + SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), impValidator) + ".get(\"" + SdkProperties.TEXT_AUTHORIZATION_FAILED + "\"));\n");
        builder.append(TAB + "}\n");
      }
      builder.append(TAB + ScoutUtility.getCommentBlock("business logic here") + "\n");
      builder.append(TAB + "return formData;\n");
      builder.append("}");
    }
    parentType.createMethod(builder.toString(), null, true, monitor);
  }

  private void createStoreMethod(IType parentType, INlsProject nlsProject, IProgressMonitor monitor, IWorkingCopyManager manager) throws JavaModelException {
    IMethod storeMethod = TypeUtility.getMethod(parentType, "store");
    if (TypeUtility.exists(storeMethod)) {
      return;
    }
    manager.register(parentType.getCompilationUnit(), monitor);
    IImportValidator impValidator = getOrCreateValidator(parentType.getCompilationUnit());
    String formDataName = SignatureUtility.getTypeReferenceFromFqn(getFormData().getFullyQualifiedName(), impValidator);
    String processingExceptionName = SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ProcessingException, impValidator);
    StringBuilder builder = new StringBuilder();
    if (!parentType.isInterface()) {
      builder.append("@" + Override.class.getSimpleName() + "\n");
      builder.append("public ");
    }
    builder.append(formDataName + " store(" + formDataName + " formData) throws " + processingExceptionName);
    if (parentType.isInterface()) {
      builder.append(";");
    }
    else {
      builder.append("{\n");
      if (getUpdatePermission() != null) {
        builder.append(TAB + "if(!" + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.ACCESS, impValidator) + ".check(new " + SignatureUtility.getTypeReferenceFromFqn(getUpdatePermission().getFullyQualifiedName(), impValidator) + "())){\n");
        builder.append(TAB + TAB + "throw new " + SignatureUtility.getTypeReferenceFromFqn(RuntimeClasses.VetoException, impValidator) + "(" + SignatureUtility.getTypeReferenceFromFqn(nlsProject.getNlsAccessorType().getFullyQualifiedName(), impValidator) + ".get(\"" + SdkProperties.TEXT_AUTHORIZATION_FAILED + "\"));\n");
        builder.append(TAB + "}\n");
      }
      builder.append(TAB + ScoutUtility.getCommentBlock("business logic here") + "\n");
      builder.append(TAB + "return formData;\n");
      builder.append("}");
    }
    parentType.createMethod(builder.toString(), null, true, monitor);
  }

  private IImportValidator getOrCreateValidator(ICompilationUnit icu) {
    IImportValidator importValidator = m_importValidator.get(icu.getElementName());
    if (importValidator == null) {
      importValidator = new CompilationUnitImportValidator(icu);
      m_importValidator.put(icu.getElementName(), importValidator);
    }
    return importValidator;
  }

  public boolean isCreatePrepareCreateMethod() {
    return m_createPrepareCreateMethod;
  }

  public void setCreatePrepareCreateMethod(boolean createPrepareCreateMethod) {
    m_createPrepareCreateMethod = createPrepareCreateMethod;
  }

  public boolean isCreateCreateMethod() {
    return m_createCreateMethod;
  }

  public void setCreateCreateMethod(boolean createCreateMethod) {
    m_createCreateMethod = createCreateMethod;
  }

  public boolean isCreateLoadMethod() {
    return m_createLoadMethod;
  }

  public void setCreateLoadMethod(boolean createLoadMethod) {
    m_createLoadMethod = createLoadMethod;
  }

  public boolean isCreateStoreMethod() {
    return m_createStoreMethod;
  }

  public void setCreateStoreMethod(boolean createStoreMethod) {
    m_createStoreMethod = createStoreMethod;
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

  public IType[] getServiceImplementations() {
    return m_serviceImplementations;
  }

  public void setServiceImplementations(IType[] serviceImplementations) {
    m_serviceImplementations = serviceImplementations;
  }

  public IType getCreatePermission() {
    return m_createPermission;
  }

  public void setCreatePermission(IType createPermission) {
    m_createPermission = createPermission;
  }

  public IType getReadPermission() {
    return m_readPermission;
  }

  public void setReadPermission(IType readPermission) {
    m_readPermission = readPermission;
  }

  public IType getUpdatePermission() {
    return m_updatePermission;
  }

  public void setUpdatePermission(IType updatePermission) {
    m_updatePermission = updatePermission;
  }

}
