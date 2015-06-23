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
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 *
 */
public class BaseType implements IType {

  private final BaseTypeBinding m_btb;
  private final String m_simpleName;
  private final int m_arrayDimension;
  private final int m_hash;
  private final ILookupEnvironment m_env;

  private ListOrderedSet<IAnnotation> m_annotations;
  private List<ITypeParameter> m_typeParameters;
  private ListOrderedSet<IType> m_superInterfaces;
  private ListOrderedSet<IType> m_memberTypes;
  private ListOrderedSet<IMethod> m_methods;
  private List<IType> m_typeArguments;
  private ListOrderedSet<IField> m_fields;

  public BaseType(BaseTypeBinding btb, int arrayDim, ILookupEnvironment lookupEnvironment) {
    m_btb = btb;
    m_env = lookupEnvironment;
    m_arrayDimension = arrayDim;
    m_simpleName = new String(m_btb.simpleName);
    m_hash = new HashCodeBuilder().append(m_arrayDimension).append(m_simpleName).toHashCode();
  }

  @Override
  public ListOrderedSet<IAnnotation> getAnnotations() {
    if (m_annotations == null) {
      m_annotations = ListOrderedSet.listOrderedSet(new HashSet<IAnnotation>(0));
    }
    return m_annotations;
  }

  @Override
  public ILookupEnvironment getLookupEnvironment() {
    return m_env;
  }

  @Override
  public boolean isArray() {
    return m_arrayDimension > 0;
  }

  @Override
  public int getArrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public ICompilationUnit getCompilationUnit() {
    return null;
  }

  @Override
  public String getSimpleName() {
    return m_simpleName;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public String getName() {
    return getSimpleName();
  }

  @Override
  public IType getDeclaringType() {
    return null;
  }

  @Override
  public List<ITypeParameter> getTypeParameters() {
    if (m_typeParameters == null) {
      m_typeParameters = new ArrayList<>(0);
    }
    return m_typeParameters;
  }

  @Override
  public int getFlags() {
    return Flags.AccDefault;
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Override
  public boolean isAnonymous() {
    return false;
  }

  @Override
  public IType getSuperClass() {
    return null;
  }

  @Override
  public ListOrderedSet<IType> getSuperInterfaces() {
    if (m_superInterfaces == null) {
      m_superInterfaces = ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
    }
    return m_superInterfaces;
  }

  @Override
  public ListOrderedSet<IType> getTypes() {
    if (m_memberTypes == null) {
      m_memberTypes = ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
    }
    return m_memberTypes;
  }

  @Override
  public ListOrderedSet<IMethod> getMethods() {
    if (m_methods == null) {
      m_methods = ListOrderedSet.listOrderedSet(new HashSet<IMethod>(0));
    }
    return m_methods;
  }

  @Override
  public List<IType> getTypeArguments() {
    if (m_typeArguments == null) {
      m_typeArguments = new ArrayList<>(0);
    }
    return m_typeArguments;
  }

  @Override
  public ListOrderedSet<IField> getFields() {
    if (m_fields == null) {
      m_fields = ListOrderedSet.listOrderedSet(new HashSet<IField>(0));
    }
    return m_fields;
  }

  @Override
  public IPackage getPackage() {
    return IPackage.EMPTY_PACKAGE;
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
    if (!(obj instanceof BaseType)) {
      return false;
    }
    BaseType other = (BaseType) obj;
    if (m_arrayDimension != other.m_arrayDimension) {
      return false;
    }
    if (m_simpleName == null) {
      if (other.m_simpleName != null) {
        return false;
      }
    }
    else if (!m_simpleName.equals(other.m_simpleName)) {
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

  @Override
  public boolean isWildcardType() {
    return false;
  }
}
