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
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

public class DeclarationType implements IType {

  private final TypeDeclaration m_td;
  private final ICompilationUnit m_icu;
  private final IType m_declaringType;
  private final int m_hash;
  private final String m_fqn;
  private final List<IType> m_typeArguments;
  private final ILookupEnvironment m_env;

  private IPackage m_package;
  private String m_simpleName;
  private IType m_superType;
  private ListOrderedSet<IType> m_memberTypes;
  private ListOrderedSet<IType> m_superInterfaces;
  private List<ITypeParameter> m_typeParameters;
  private ListOrderedSet<IAnnotation> m_annotations;
  private ListOrderedSet<IMethod> m_methods;
  private ListOrderedSet<IField> m_fields;
  private int m_flags;

  public DeclarationType(TypeDeclaration td, ICompilationUnit owner, IType declaringType, ILookupEnvironment lookupEnvironment) {
    m_td = Validate.notNull(td);
    m_icu = Validate.notNull(owner);
    m_declaringType = declaringType;
    m_env = lookupEnvironment;
    m_typeArguments = new ArrayList<>(0); // no arguments for declarations
    m_fqn = calcFullyQualifiedName(td);
    m_hash = m_fqn.hashCode();
    m_flags = -1; // mark as uninitialized
  }

  @Override
  public String getSimpleName() {
    if (m_simpleName == null) {
      m_simpleName = new String(m_td.name);
    }
    return m_simpleName;
  }

  @Override
  public int getArrayDimension() {
    return 0;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public boolean isAnonymous() {
    return m_td.name == null || m_td.name.length < 1;
  }

  @Override
  public String getName() {
    return m_fqn;
  }

  @Override
  public IPackage getPackage() {
    if (m_package == null) {
      char[] qualifiedPackageName = m_td.binding.qualifiedPackageName();
      if (qualifiedPackageName != null && qualifiedPackageName.length > 0) {
        m_package = new Package(new String(qualifiedPackageName));
      }
    }
    return m_package;
  }

  @Override
  public ListOrderedSet<IAnnotation> getAnnotations() {
    if (m_annotations == null) {
      Annotation[] annots = m_td.annotations;
      m_annotations = JavaModelUtils.annotationsToIAnnotations(annots, m_td.scope, this, m_env);
    }
    return m_annotations;
  }

  @Override
  public ListOrderedSet<IMethod> getMethods() {
    if (m_methods == null) {
      AbstractMethodDeclaration[] methods = m_td.methods;
      if (methods == null || methods.length < 1) {
        m_methods = ListOrderedSet.listOrderedSet(new HashSet<IMethod>(0));
      }
      else {
        List<IMethod> result = new ArrayList<>(methods.length);
        for (AbstractMethodDeclaration a : methods) {
          if (a.bodyStart > 0) { // skip compiler generated methods
            result.add(new DeclarationMethod(a, m_td.scope, this));
          }
        }
        m_methods = ListOrderedSet.listOrderedSet(result);
      }
    }
    return m_methods;
  }

  @Override
  public IType getSuperClass() {
    if (m_superType == null) {
      if (m_td.superclass == null) {
        m_superType = null;
      }
      else {
        TypeBinding tb = m_td.superclass.resolvedType;
        if (tb == null) {
          m_superType = null;
        }
        else {
          m_superType = JavaModelUtils.bindingToType(tb, m_env);
        }
      }
    }
    return m_superType;
  }

  @Override
  public ListOrderedSet<IType> getTypes() {
    if (m_memberTypes == null) {
      TypeDeclaration[] memberTypes = m_td.memberTypes;
      if (memberTypes == null || memberTypes.length < 1) {
        m_memberTypes = ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
      }
      else {
        List<IType> result = new ArrayList<>(memberTypes.length);
        for (TypeDeclaration d : memberTypes) {
          result.add(new DeclarationType(d, m_icu, this, m_env));
        }
        m_memberTypes = ListOrderedSet.listOrderedSet(result);
      }
    }
    return m_memberTypes;
  }

  @Override
  public ListOrderedSet<IType> getSuperInterfaces() {
    if (m_superInterfaces == null) {
      TypeReference[] refs = m_td.superInterfaces;
      if (refs == null || refs.length < 1) {
        m_superInterfaces = ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
      }
      else {
        List<IType> result = new ArrayList<>(refs.length);
        for (TypeReference r : refs) {
          TypeBinding b = r.resolvedType;
          if (b != null) {
            IType t = JavaModelUtils.bindingToType(b, m_env);
            if (t != null) {
              result.add(t);
            }
          }
        }
        m_superInterfaces = ListOrderedSet.listOrderedSet(result);
      }
    }
    return m_superInterfaces;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = JavaModelUtils.getTypeFlags(m_td.modifiers, m_td.allocation, JavaModelUtils.hasDeprecatedAnnotation(m_td.annotations));
    }
    return m_flags;
  }

  @Override
  public boolean hasTypeParameters() {
    TypeParameter[] typeParams = m_td.typeParameters;
    return typeParams != null && typeParams.length > 0;
  }

  @Override
  public List<IType> getTypeArguments() {
    return m_typeArguments;
  }

  @Override
  public List<ITypeParameter> getTypeParameters() {
    if (m_typeParameters == null) {
      TypeParameter[] typeParams = m_td.typeParameters;
      if (typeParams == null || typeParams.length < 1) {
        m_typeParameters = new ArrayList<>(0);
      }
      else {
        List<ITypeParameter> result = new ArrayList<>(typeParams.length);
        for (TypeParameter param : typeParams) {
          result.add(new org.eclipse.scout.sdk.core.model.TypeParameter(param, this));
        }
        m_typeParameters = result;
      }
    }
    return m_typeParameters;
  }

  @Override
  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }

  @Override
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public ListOrderedSet<IField> getFields() {
    if (m_fields == null) {
      FieldDeclaration[] fields = m_td.fields;
      if (fields == null || fields.length < 1) {
        m_fields = ListOrderedSet.listOrderedSet(new HashSet<IField>(0));
      }
      else {
        List<IField> result = new ArrayList<>(fields.length);
        for (FieldDeclaration fd : fields) {
          result.add(new DeclarationField(fd, this, m_td.scope));
        }
        m_fields = ListOrderedSet.listOrderedSet(result);
      }
    }
    return m_fields;
  }

  private static String calcFullyQualifiedName(TypeDeclaration td) {
    StringBuilder sb = new StringBuilder();

    char[] qualifiedPackageName = td.binding.qualifiedPackageName();
    if (qualifiedPackageName != null && qualifiedPackageName.length > 0) {
      sb.append(qualifiedPackageName);
      sb.append('.');
    }

//  // collect declaring types
    Deque<char[]> namesBottomUp = new LinkedList<>();
    TypeDeclaration declaringType = td;
    while (declaringType != null) {
      namesBottomUp.add(declaringType.name);
      declaringType = declaringType.enclosingType;
    }

    Iterator<char[]> namesTopDown = namesBottomUp.descendingIterator();
    sb.append(namesTopDown.next()); // there must be at least one type name
    while (namesTopDown.hasNext()) {
      sb.append('$').append(namesTopDown.next());
    }
    return sb.toString();
  }

  @Override
  public ILookupEnvironment getLookupEnvironment() {
    return m_env;
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
    if (!(obj instanceof DeclarationType)) {
      return false;
    }
    DeclarationType other = (DeclarationType) obj;
    if (m_fqn == null) {
      if (other.m_fqn != null) {
        return false;
      }
    }
    else if (!m_fqn.equals(other.m_fqn)) {
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
