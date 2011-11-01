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
package org.eclipse.scout.sdk.operation.method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutSignature;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 *
 */
public class MethodOverrideOperation extends MethodCreateOperation {
  // operation member
  private IMethod m_methodToOverride;
  private String m_genericWildcardReplacement;

  public MethodOverrideOperation(IType declaringType, String methodName) throws JavaModelException {
    this(declaringType, methodName, false);
  }

  public MethodOverrideOperation(IType declaringType, String methodName, boolean formatSource) throws JavaModelException {
    super(declaringType, methodName, null, formatSource);
  }

  @Override
  public String getOperationName() {
    return "Override '" + getMethodName() + "'.";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find super method
    ITypeHierarchy superTypeHierarchy = getDeclaringType().newSupertypeHierarchy(monitor);
    IType superType = superTypeHierarchy.getSuperclass(getDeclaringType());
    m_methodToOverride = findMethodToOverride(superType, getMethodName(), superTypeHierarchy);
    if (m_methodToOverride == null) {
      ScoutSdk.logError("method '" + getMethodName() + "' to override on a super type of '" + getDeclaringType().getFullyQualifiedName() + "' could not be found [stop operation].");
      return;
    }
    // generic substitutions
    if (getReturnTypeSignature() == null) {
      String returnTypeSig = ScoutSignature.getReturnTypeSignatureResolved(m_methodToOverride, getDeclaringType());
      if (getGenericWildcardReplacement() != null) {
        returnTypeSig = returnTypeSig.replaceAll("\\" + Signature.C_STAR, getGenericWildcardReplacement());
      }
      setReturnTypeSignature(returnTypeSig);
    }
    setMethodFlags(m_methodToOverride.getFlags());
    setExceptionSignatures(m_methodToOverride.getExceptionTypes());
    String[] paramNames = m_methodToOverride.getParameterNames();
    setParameterNames(paramNames);
    setParameterSignatures(ScoutSignature.getMethodParameterSignatureResolved(m_methodToOverride, getDeclaringType()));
    addAnnotation(AnnotationCreateOperation.OVERRIDE_OPERATION);

    super.run(monitor, workingCopyManager);
    if (isFormatSource()) {
      JavaElementFormatOperation op = new JavaElementFormatOperation(getCreatedMethod(), true);
      op.validate();
      op.run(monitor, workingCopyManager);
    }
  }

  private IMethod findMethodToOverride(IType type, String methodName, ITypeHierarchy superTypeHierarchy) {
    IMethod method = TypeUtility.getMethod(type, methodName);
    if (TypeUtility.exists(method)) {
      return method;
    }
    // super types
    IType superType = superTypeHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType) && !superType.getElementName().equals(Object.class.getName())) {
      method = findMethodToOverride(superType, methodName, superTypeHierarchy);
    }
    if (TypeUtility.exists(method)) {
      return method;
    }
    // interfaces
    for (IType intType : superTypeHierarchy.getSuperInterfaces(type)) {
      if (TypeUtility.exists(intType) && !intType.getElementName().equals(Object.class.getName())) {
        method = findMethodToOverride(intType, methodName, superTypeHierarchy);
      }
      if (TypeUtility.exists(method)) {
        return method;
      }
    }
    return null;
  }

  protected IMethod getMethodToOverride() {
    return m_methodToOverride;
  }

  public String getGenericWildcardReplacement() {
    return m_genericWildcardReplacement;
  }

  public void setGenericWildcardReplacement(String genericWildcardReplacement) {
    m_genericWildcardReplacement = genericWildcardReplacement;
  }
}
