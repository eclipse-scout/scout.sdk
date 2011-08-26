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
package org.eclipse.scout.sdk.workspace.type.config;

import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;

/**
 *
 */
public class ConfigurationMethod {
  public static int PROPERTY_METHOD = 1;
  public static int OPERATION_METHOD = 2;

  private final IType m_type;
  private final ITypeHierarchy m_superTypeHierarchy;
  private String m_methodName;
  Stack<IMethod> m_methodStack = new Stack<IMethod>();
  private String m_configAnnotationType;
  private int m_methodType;
  private String m_source;

  public ConfigurationMethod(IType type, ITypeHierarchy superTypeHierarchy, String methodName, int methodType) {
    m_type = type;
    m_superTypeHierarchy = superTypeHierarchy;
    m_methodName = methodName;
    m_methodType = methodType;
  }

  public int getMethodType() {
    return m_methodType;
  }

  public IType getType() {
    return m_type;
  }

  public String getMethodName() {
    return m_methodName;
  }

  public void setConfigAnnotationType(String configAnnotationType) {
    m_configAnnotationType = configAnnotationType;
  }

  public String getConfigAnnotationType() {
    return m_configAnnotationType;
  }

  public boolean isImplemented() {
    return peekMethod().getDeclaringType().equals(getType());
  }

  public void pushMethod(IMethod method) {
    m_methodStack.push(method);
    try {
      m_source = method.getSource();
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not get source of method '" + method.getElementName() + "' in type '" + method.getDeclaringType() + "'.", e);
    }
  }

  public String getSource() {
    return m_source;
  }

  public String computeDefaultValue() {
    if (getMethodType() == PROPERTY_METHOD) {
      try {
        return PropertyMethodSourceUtilities.getMethodReturnValue(getDefaultMethod());
      }
      catch (CoreException e) {
        ScoutSdk.logError("could not parse default value of method '" + getDefaultMethod().getElementName() + "' in type '" + getType().getFullyQualifiedName() + "'.", e);
      }
    }
    return null;
  }

  public String computeValue() throws CoreException {
    if (getMethodType() == PROPERTY_METHOD) {
      return PropertyMethodSourceUtilities.getMethodReturnValue(peekMethod());
    }
    return null;
  }

  public IMethod peekMethod() {
    if (!m_methodStack.isEmpty()) {
      return m_methodStack.peek();
    }
    return null;
  }

  public int getMethodStackSize() {
    return m_methodStack.size();
  }

  public IMethod getDefaultMethod() {
    for (int i = m_methodStack.size() - 1; i > -1; i--) {
      IMethod m = m_methodStack.get(i);
      if (!m.getDeclaringType().equals(getType())) {
        return m;
      }
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ConfigurationMethod)) {
      return false;
    }
    ConfigurationMethod cm = (ConfigurationMethod) obj;
    return cm.hashCode() == hashCode();
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    for (IMethod m : m_methodStack) {
      hashCode = hashCode ^ m.hashCode();
    }
    hashCode = hashCode ^ m_type.hashCode();
    if (!StringUtility.isNullOrEmpty(getSource())) {
      hashCode = hashCode ^ getSource().hashCode();
    }
    return hashCode;
  }

  public ITypeHierarchy getSuperTypeHierarchy() {
    return m_superTypeHierarchy;
  }
}
