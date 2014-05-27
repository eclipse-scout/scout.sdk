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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class ConfigurationMethod {
  public static final int PROPERTY_METHOD = 1;
  public static final int OPERATION_METHOD = 2;

  private final IType m_type;
  private final ITypeHierarchy m_superTypeHierarchy;
  private final Stack<IMethod> m_methodStack;
  private final String m_methodName;
  private final int m_methodType;
  private String m_configAnnotationType;
  private String m_source;

  public ConfigurationMethod(IType type, ITypeHierarchy superTypeHierarchy, String methodName, int methodType) {
    m_methodStack = new Stack<IMethod>();
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

  /**
   * Pushes the given method onto the top of this stack.<br>
   * <b>Note: Compiler generated methods (Bridge methods) are ignored!</b>
   * 
   * @param method
   *          The method to add
   * @see Flags#AccBridge
   */
  public void pushMethod(IMethod method) {
    try {
      if (Flags.isBridge(method.getFlags())) {
        return; // ignore compiler generated methods
      }
      m_methodStack.push(method);
      m_source = method.getSource();
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not add method '" + method.getElementName() + "' in type '" + method.getDeclaringType() + "'.", e);
    }
  }

  public String getSource() {
    return m_source;
  }

  public String computeDefaultValue() throws CoreException {
    if (getMethodType() == PROPERTY_METHOD) {
      return PropertyMethodSourceUtility.getMethodReturnValue(getDefaultMethod());
    }
    return null;
  }

  /**
   * @return Gets the value of the first (bottom-up) @Order annotation in the method stack or null if no @Order
   *         annotation can
   *         be found.
   */
  public Double getOrder() {
    try {
      for (int i = m_methodStack.size() - 1; i > -1; i--) {
        IMethod m = m_methodStack.get(i);
        if (TypeUtility.exists(m)) {
          Double order = ScoutTypeUtility.getOrderAnnotationValue(m);
          if (order != null) {
            return order;
          }
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("Error retrieving the order for method " + getMethodName(), e);
    }
    return null;
  }

  public String computeValue() throws CoreException {
    if (getMethodType() == PROPERTY_METHOD) {
      return PropertyMethodSourceUtility.getMethodReturnValue(peekMethod());
    }
    return null;
  }

  /**
   * returns the method at the top of this stack without removing it from the stack.
   * 
   * @return
   */
  public IMethod peekMethod() {
    if (!m_methodStack.isEmpty()) {
      while (!m_methodStack.peek().exists()) {
        m_methodStack.pop();
      }
      return m_methodStack.peek();

    }
    return null;
  }

  public int getMethodStackSize() {
    return m_methodStack.size();
  }

  /**
   * @return a copy of this ConfigurationMethod's internal method stack (stack elements are not copied)
   */
  public Stack<IMethod> getMethodStack() {
    Stack<IMethod> s = new Stack<IMethod>();
    s.addAll(m_methodStack);
    return s;
  }

  public IMethod getDefaultMethod() {
    if (m_methodStack.size() == 1) return m_methodStack.get(0);
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
