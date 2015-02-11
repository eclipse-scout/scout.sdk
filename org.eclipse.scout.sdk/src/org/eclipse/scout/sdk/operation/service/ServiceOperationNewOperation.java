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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ServiceOperationNewOperation implements IOperation {

  private ParameterArgument[] m_arguments;
  private ParameterArgument m_returnType;
  private String m_methodName;
  private IType m_serviceInterface;
  private IType m_serviceImplementation;

  private IMethod m_createdImplementationMethod;

  @Override
  public String getOperationName() {
    return "New Service Operation...";
  }

  @Override
  public void validate() {
    if (getServiceInterface() == null) {
      throw new IllegalArgumentException("service interface cannot be null.");
    }
    if (getServiceImplementation() == null) {
      throw new IllegalArgumentException("service implementations cannot be null.");
    }
    if (StringUtility.isNullOrEmpty(getMethodName())) {
      throw new IllegalArgumentException("method name cannot be null.");
    }
    if (getReturnType() == null) {
      throw new IllegalArgumentException("return parameter cannot be null.");
    }

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ServiceMethod createMethod = new ServiceMethod(getMethodName(), TypeUtility.exists(getServiceInterface()) ? getServiceInterface().getFullyQualifiedName() : null);
    for (ParameterArgument arg : getArguments()) {
      createMethod.addParameter(new MethodParameter(arg.getName(), SignatureCache.createTypeSignature(arg.getType())));
    }
    createMethod.setReturnTypeSignature(SignatureCache.createTypeSignature(getReturnType().getType()));
    createMethod.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    createMethod.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        createImports(validator);
        String body = createMethodBody(validator);
        source.append(ScoutUtility.getCommentBlock("business logic here.")).append(lineDelimiter);
        if (body == null) {
          String defaultVal = ScoutUtility.getDefaultValueOf(methodBuilder.getReturnTypeSignature());
          if (defaultVal != null) {
            source.append("return " + defaultVal + ";");
          }
          else {
            source.append(lineDelimiter);
          }
        }
        else {
          source.append(body);
        }
      }
    });

    if (TypeUtility.exists(getServiceInterface())) {
      MethodNewOperation ifcMno = new MethodNewOperation(createMethod.getInterfaceSourceBuilder(), getServiceInterface(), true) {
        @Override
        protected void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          super.createSource(source, lineDelimiter, ownerProject, validator);
          createImports(validator);
        }
      };
      ifcMno.validate();
      ifcMno.run(monitor, workingCopyManager);
    }

    MethodNewOperation svcMno = new MethodNewOperation(createMethod.getImplementationSourceBuilder(), getServiceImplementation(), true);
    svcMno.validate();
    svcMno.run(monitor, workingCopyManager);

    setCreatedImplementationMethod(svcMno.getCreatedMethod());
  }

  protected void createImports(IImportValidator validator) {
    for (ParameterArgument arg : getArguments()) {
      for (String imp : arg.getFullyQualifiedImports()) {
        validator.getTypeName(SignatureCache.createTypeSignature(imp));
      }
    }
    if (getReturnType() != null) {
      for (String imp : getReturnType().getFullyQualifiedImports()) {
        validator.getTypeName(SignatureCache.createTypeSignature(imp));
      }
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

  public IType getServiceImplementation() {
    return m_serviceImplementation;
  }

  public void setServiceImplementation(IType serviceImplementation) {
    m_serviceImplementation = serviceImplementation;
  }

  public IMethod getCreatedImplementationMethod() {
    return m_createdImplementationMethod;
  }

  public void setCreatedImplementationMethod(IMethod createdImplementationMethod) {
    m_createdImplementationMethod = createdImplementationMethod;
  }

}
