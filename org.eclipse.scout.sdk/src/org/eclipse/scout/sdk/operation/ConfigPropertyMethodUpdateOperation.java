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
package org.eclipse.scout.sdk.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.MethodUpdateContentOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 *
 */
public class ConfigPropertyMethodUpdateOperation implements IOperation {

  private final IType m_declaringType;
  private final String m_methodName;
  private String m_simpleContent;
  private boolean m_formatSource;
  // out
  private IMethod m_updatedMethod;

  public ConfigPropertyMethodUpdateOperation(IType declaringType, String methodName) {
    this(declaringType, methodName, null);
  }

  public ConfigPropertyMethodUpdateOperation(IType declaringType, String methodName, String simpleContent) {
    this(declaringType, methodName, simpleContent, false);
  }

  public ConfigPropertyMethodUpdateOperation(IType declaringType, String methodName, String simpleContent, boolean formatSource) {
    m_declaringType = declaringType;
    m_methodName = methodName;
    m_simpleContent = simpleContent;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "Update '" + getMethodName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getMethodName())) {
      throw new IllegalArgumentException("method name can not be null.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IMethod method = TypeUtility.getMethod(getDeclaringType(), getMethodName());
    if (TypeUtility.exists(method)) {
      MethodUpdateContentOperation op = new MethodUpdateContentOperation(method) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          return ConfigPropertyMethodUpdateOperation.this.createMethodBody(getMethod(), validator);
        }
      };
      op.setFormatSource(m_formatSource);
      op.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation op = new MethodOverrideOperation(getDeclaringType(), getMethodName()) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          return ConfigPropertyMethodUpdateOperation.this.createMethodBody(getMethodToOverride(), validator);
        }
      };
      op.setSibling(computeSibling());

      op.setFormatSource(m_formatSource);
      op.run(monitor, workingCopyManager);
      method = op.getCreatedMethod();
    }
    m_updatedMethod = method;
  }

  protected IJavaElement computeSibling() {
    IStructuredType structuredType = SdkTypeUtility.createStructuredType(getDeclaringType());
    return structuredType.getSiblingMethodConfigGetConfigured(getMethodName());
  }

  /**
   * can be overridden to provide a specific method body. The method body is defined as part between the method body{}.
   * Use {@link ScoutSdkUtility#getSimpleTypeRefName(String, IImportValidator)} to determ class references (fully
   * quallified vs. simple name).
   * 
   * @param validator
   *          validator can be used to determ class references (fully quallified vs. simple name).
   * @return
   * @throws JavaModelException
   */
  protected String createMethodBody(IMethod methodToOverride, @SuppressWarnings("unused") IImportValidator validator) throws JavaModelException {
    StringBuilder builder = new StringBuilder();
    if (StringUtility.isNullOrEmpty(getSimpleContent())) {
      builder.append(ScoutUtility.getCommentAutoGeneratedMethodStub() + "\n");
      String methodSignature = methodToOverride.getSignature();
      if (!Signature.getReturnType(methodSignature).equals(Signature.SIG_VOID)) {
        builder.append("return " + ScoutUtility.getDefaultValueOf(Signature.getReturnType(methodSignature)) + ";\n");
      }
    }
    else {
      builder.append(getSimpleContent());
    }
    return builder.toString();
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public String getMethodName() {
    return m_methodName;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setSimpleContent(String simpleContent) {
    m_simpleContent = simpleContent;
  }

  public String getSimpleContent() {
    return m_simpleContent;
  }

  public IMethod getUpdatedMethod() {
    return m_updatedMethod;
  }

}
