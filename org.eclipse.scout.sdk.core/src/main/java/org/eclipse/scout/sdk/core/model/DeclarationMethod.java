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
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

/**
 *
 */
public class DeclarationMethod implements IMethod {
  private final AbstractMethodDeclaration m_md;
  private final ClassScope m_scope;
  private final IType m_declaringType;
  private final char[] m_id;
  private final int m_hash;
  private IType m_returnType;
  private ListOrderedSet<IAnnotation> m_annotations;
  private int m_flags;
  private List<IMethodParameter> m_arguments;
  private String m_name;
  private ListOrderedSet<IType> m_exceptions;

  public DeclarationMethod(AbstractMethodDeclaration md, ClassScope scope, IType declaringType) {
    m_declaringType = Validate.notNull(declaringType);
    m_md = Validate.notNull(md);
    m_scope = Validate.notNull(scope);
    m_flags = -1; // mark as uninitialized
    m_id = computeUniqueKey();
    m_hash = new HashCodeBuilder().append(m_declaringType).append(m_id).toHashCode();
  }

  private char[] computeUniqueKey() {
    StringBuilder sig = new StringBuilder();
    if (m_md.binding != null) {
      // binding can be null for static constructors
      sig.append(m_md.binding.selector);
    }
    sig.append('(');
    if (m_md.arguments != null && m_md.arguments.length > 0) {
      appendArgument(sig, m_md.arguments[0]);
      for (int i = 1; i < m_md.arguments.length; i++) {
        sig.append(',');
        appendArgument(sig, m_md.arguments[i]);
      }
    }
    sig.append(')');

    int sigLength = sig.length();
    char[] uniqueKey = new char[sigLength];
    sig.getChars(0, sigLength, uniqueKey, 0);
    return uniqueKey;
  }

  private void appendArgument(StringBuilder sig, Argument m) {
    char[][] nameParts = m.type.getTypeName();
    if (nameParts != null && nameParts.length > 0) {
      for (char[] part : nameParts) {
        sig.append(part);
      }
    }
  }

  @Override
  public boolean isConstructor() {
    return m_md.isConstructor();
  }

  @Override
  public IType getReturnType() {
    if (m_returnType == null && m_md instanceof MethodDeclaration) {
      TypeReference ref = ((MethodDeclaration) m_md).returnType;
      if (ref.resolvedType == null) {
        ref.resolveType(m_scope);
      }
      IType result = JavaModelUtils.bindingToType(ref.resolvedType, m_declaringType.getLookupEnvironment());
      m_returnType = result;
    }
    return m_returnType;
  }

  @Override
  public ListOrderedSet<IAnnotation> getAnnotations() {
    if (m_annotations == null) {
      Annotation[] annots = m_md.annotations;
      m_annotations = JavaModelUtils.annotationsToIAnnotations(annots, m_scope, this, m_declaringType.getLookupEnvironment());
    }
    return m_annotations;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = JavaModelUtils.getMethodFlags(m_md.modifiers, isVarArgs(), JavaModelUtils.hasDeprecatedAnnotation(m_md.annotations));
    }
    return m_flags;
  }

  private boolean isVarArgs() {
    Argument[] arguments = m_md.arguments;
    if (arguments != null && arguments.length > 0) {
      return arguments[arguments.length - 1].isVarArgs();
    }
    return false;
  }

  @Override
  public List<IMethodParameter> getParameters() {
    if (m_arguments == null) {
      Argument[] arguments = m_md.arguments;
      if (arguments == null || arguments.length < 1) {
        m_arguments = new ArrayList<>(0);
      }
      else {
        List<IMethodParameter> result = new ArrayList<>(arguments.length);
        for (Argument a : arguments) {
          result.add(new MethodParameter(a, this, m_scope));
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
      if (m_md.selector != null) {
        m_name = new String(m_md.selector);
      }
      else if (m_md.binding.selector != null) {
        m_name = new String(m_md.binding.selector);
      }
      else {
        // m_md.binding is null for static constructors
        m_name = "<clinit>";
      }
    }
    return m_name;
  }

  @Override
  public ListOrderedSet<IType> getExceptionTypes() {
    if (m_exceptions == null) {
      TypeReference[] exceptions = m_md.thrownExceptions;
      if (exceptions == null || exceptions.length < 1) {
        m_exceptions = ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
      }
      else {
        List<IType> result = new ArrayList<>(exceptions.length);
        for (TypeReference r : exceptions) {
          if (r.resolvedType == null) {
            r.resolveType(m_scope);
          }
          IType t = JavaModelUtils.bindingToType(r.resolvedType, m_declaringType.getLookupEnvironment());
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
    if (!(obj instanceof DeclarationMethod)) {
      return false;
    }
    DeclarationMethod other = (DeclarationMethod) obj;
    if (m_declaringType == null) {
      if (other.m_declaringType != null) {
        return false;
      }
    }
    else if (!m_declaringType.equals(other.m_declaringType)) {
      return false;
    }
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
