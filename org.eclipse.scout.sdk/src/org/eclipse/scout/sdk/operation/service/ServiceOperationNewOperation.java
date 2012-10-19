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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ServiceOperationNewOperation implements IOperation {

  private ParameterArgument[] m_arguments;
  private ParameterArgument m_returnType;
  private String m_methodName;
  private IType m_serviceInterface;
  private IType[] m_serviceImplementations;
  private IMethod m_createdImplementationMethod;

  @Override
  public String getOperationName() {
    return "new Service Operation...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getServiceInterface() == null) {
      throw new IllegalArgumentException("service interface can not be null.");
    }
    if (getServiceImplementations() == null) {
      throw new IllegalArgumentException("service implementations can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getMethodName())) {
      throw new IllegalArgumentException("method name can not be null.");
    }
    if (getReturnType() == null) {
      throw new IllegalArgumentException("return parameter can not be null.");
    }

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    IImportValidator validator = new CompilationUnitImportValidator(m_serviceInterface.getCompilationUnit());

    StringBuilder methodBody = new StringBuilder("public " + getReturnType().getType() + " " + getMethodName() + "(");
    for (String s : getReturnType().getFullyQuallifiedImports()) {
      validator.addImport(s);
    }
    ParameterArgument[] arguments = getArguments();
    if (arguments != null) {
      for (int i = 0; i < arguments.length; i++) {
        methodBody.append(arguments[i].getType() + " " + arguments[i].getName());
        if (i < (arguments.length - 1)) {
          methodBody.append(", ");
        }
        for (String imp : arguments[i].getFullyQuallifiedImports()) {
          validator.addImport(imp);
        }
      }
    }
    methodBody.append(") throws " + validator.getTypeName(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true)));

    // interface
    workingCopyManager.register(m_serviceInterface.getCompilationUnit(), monitor);
    IMethod method = m_serviceInterface.createMethod(methodBody.toString() + ";", null, false, monitor);
    for (String imp : validator.getImportsToCreate()) {
      m_serviceInterface.getCompilationUnit().createImport(imp, null, monitor);
    }
    // implementation
    methodBody.insert(0, "@" + Override.class.getSimpleName() + "\n");
    String defaultVal = ScoutUtility.getDefaultValueOf(method.getReturnType());
    methodBody.append("{\n" + SdkProperties.TAB);
    String body = createMethodBody(validator);
    if (body == null) {
      if (defaultVal != null) {
        methodBody.append("return " + defaultVal + ";\n");
      }
      else {
        methodBody.append("\n");
      }
    }
    else {
      methodBody.append(body);
    }
    methodBody.append("}");

    // implementations
    for (IType implType : getServiceImplementations()) {
      IImportValidator impValidator = new CompilationUnitImportValidator(implType.getCompilationUnit());
      for (String s : getReturnType().getFullyQuallifiedImports()) {
        impValidator.addImport(s);
      }
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          for (String imp : arguments[i].getFullyQuallifiedImports()) {
            impValidator.addImport(imp);
          }
        }
      }

      workingCopyManager.register(implType.getCompilationUnit(), monitor);
      IMethod createdImplementationMethod = implType.createMethod(methodBody.toString(), null, false, monitor);
      setCreatedImplementationMethod(createdImplementationMethod);
      for (String imp : impValidator.getImportsToCreate()) {
        implType.getCompilationUnit().createImport(imp, null, monitor);
      }
      implType.getCompilationUnit().createImport(RuntimeClasses.ProcessingException, null, monitor);
    }
  }

  protected String createMethodBody(IImportValidator validator) {
    return null;
  }

  public ParameterArgument[] getArguments() {
    return m_arguments;
  }

  public void setArguments(ParameterArgument[] arguments) {
    m_arguments = arguments;
  }

  public ParameterArgument getReturnType() {
    return m_returnType;
  }

  public void setReturnType(ParameterArgument returnType) {
    m_returnType = returnType;
  }

  public String getMethodName() {
    return m_methodName;
  }

  public void setMethodName(String methodName) {
    m_methodName = methodName;
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

  public IMethod getCreatedImplementationMethod() {
    return m_createdImplementationMethod;
  }

  public void setCreatedImplementationMethod(IMethod createdImplementationMethod) {
    m_createdImplementationMethod = createdImplementationMethod;
  }

}
