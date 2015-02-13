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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class ConfigurationMethod {
  public static final int PROPERTY_METHOD = 1;
  public static final int OPERATION_METHOD = 2;

  private final IType m_type;
  private final ITypeHierarchy m_superTypeHierarchy;
  private final Deque<IMethod> m_methodStack;
  private final String m_methodName;
  private final int m_methodType;
  private String m_configAnnotationType;
  private String m_source;

  public ConfigurationMethod(IType type, ITypeHierarchy superTypeHierarchy, String methodName, int methodType) {
    m_methodStack = new LinkedList<>();
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

  public Deque<IMethod> getMethodStack() {
    return new LinkedList<>(m_methodStack);
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
   * @return Gets the value of the first (most precise) {@link Order} annotation in the method stack or null if no
   *         {@link Order} annotation can be found.
   */
  public Double getOrder() {
    try {
      for (IMethod m : m_methodStack) {
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

  public IMethod getDefaultMethod() {
    if (m_methodStack.size() == 1) {
      return m_methodStack.getFirst();
    }

    Iterator<IMethod> reverseIterator = m_methodStack.descendingIterator();
    while (reverseIterator.hasNext()) {
      IMethod m = reverseIterator.next();
      if (!m.getDeclaringType().equals(getType())) {
        return m;
      }
    }
    return null;
  }

  public ITypeHierarchy getSuperTypeHierarchy() {
    return m_superTypeHierarchy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_methodStack.hashCode();
    result = prime * result + ((m_source == null) ? 0 : m_source.hashCode());
    result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ConfigurationMethod)) {
      return false;
    }
    ConfigurationMethod other = (ConfigurationMethod) obj;
    if (!m_methodStack.equals(other.m_methodStack)) {
      return false;
    }
    if (m_source == null) {
      if (other.m_source != null) {
        return false;
      }
    }
    else if (!m_source.equals(other.m_source)) {
      return false;
    }
    if (m_type == null) {
      if (other.m_type != null) {
        return false;
      }
    }
    else if (!m_type.equals(other.m_type)) {
      return false;
    }
    return true;
  }
}
