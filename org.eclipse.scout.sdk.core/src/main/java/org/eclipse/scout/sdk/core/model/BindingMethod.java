/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 *
 */
public class BindingMethod implements IMethod {

  private final MethodBinding m_b;
  private final IType m_declaringType;
  private final char[] m_id;
  private final int m_hash;
  private IType m_returnType;
  private ListOrderedSet<IAnnotation> m_annotations;
  private int m_flags;
  private String m_name;
  private ListOrderedSet<IType> m_exceptions;
  private List<IMethodParameter> m_arguments;

  public BindingMethod(MethodBinding b, IType declaringType) {
    m_declaringType = Validate.notNull(declaringType);
    m_b = Validate.notNull(b);
    m_id = m_b.computeUniqueKey();
    m_hash = Arrays.hashCode(m_id);
    m_flags = -1;
  }

  @Override
  public IType getReturnType() {
    if (m_returnType == null && !isConstructor()) {
      m_returnType = JavaModelUtils.bindingToType(m_b.returnType, m_declaringType.getLookupEnvironment());
    }
    return m_returnType;
  }

  @Override
  public ListOrderedSet<IAnnotation> getAnnotations() {
    if (m_annotations == null) {
      m_annotations = JavaModelUtils.annotationBindingsToIAnnotations(m_b.getAnnotations(), this, m_declaringType.getLookupEnvironment());
    }
    return m_annotations;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = JavaModelUtils.getMethodFlags(m_b.modifiers, false, JavaModelUtils.hasDeprecatedAnnotation(m_b.getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public List<IMethodParameter> getParameters() {
    if (m_arguments == null) {
      TypeBinding[] arguments = m_b.parameters;
      if (arguments == null || arguments.length < 1) {
        m_arguments = new ArrayList<>(0);
      }
      else {
        List<IMethodParameter> result = new ArrayList<>(arguments.length);
        for (int i = 0; i < arguments.length; i++) {
          char[] name = null;
          if (m_b.parameterNames.length > i) {
            name = m_b.parameterNames[i];
          }
          else {
            // if no parameter name info is in the class file
            name = ("arg" + i).toCharArray();
          }
          result.add(new BindingMethodParameter(arguments[i], name, this));
        }
        m_arguments = result;
      }
    }
    return m_arguments;
  }

  @Override
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public String getName() {
    if (m_name == null) {
      m_name = new String(m_b.selector);
    }
    return m_name;
  }

  @Override
  public ListOrderedSet<IType> getExceptionTypes() {
    if (m_exceptions == null) {
      ReferenceBinding[] exceptions = m_b.thrownExceptions;
      if (exceptions == null || exceptions.length < 1) {
        m_exceptions = ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
      }
      else {
        List<IType> result = new ArrayList<>(exceptions.length);
        for (ReferenceBinding r : exceptions) {
          IType t = JavaModelUtils.bindingToType(r, m_declaringType.getLookupEnvironment());
          if (t != null) {
            result.add(t);
          }
        }
        m_exceptions = ListOrderedSet.listOrderedSet(result);
      }
    }
    return m_exceptions;
  }

  @Override
  public boolean isConstructor() {
    return m_b.isConstructor();
  }

  @Override
  public int hashCode() {
    return m_hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BindingMethod)) {
      return false;
    }
    BindingMethod other = (BindingMethod) obj;
    if (!Arrays.equals(m_id, other.m_id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }
}
