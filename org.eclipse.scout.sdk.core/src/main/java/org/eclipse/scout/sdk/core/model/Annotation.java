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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 *
 */
public class Annotation implements IAnnotation {
  private final org.eclipse.jdt.internal.compiler.ast.Annotation m_annotation;
  private final ClassScope m_scope;
  private final IAnnotatable m_owner;
  private final char[][] m_name; // only used for comparing and hashCode()
  private final int m_ordinal;
  private final int m_hash;
  private final ILookupEnvironment m_env;
  private ListOrderedMap<String, IAnnotationValue> m_values;
  private IType m_type;

  public Annotation(org.eclipse.jdt.internal.compiler.ast.Annotation annotation, ClassScope scope, IAnnotatable owner, int ordinal, ILookupEnvironment lookupEnvironment) {
    m_scope = Validate.notNull(scope);
    m_owner = Validate.notNull(owner);
    m_annotation = Validate.notNull(annotation);
    m_env = lookupEnvironment;
    m_name = m_annotation.type.getTypeName();
    m_ordinal = ordinal;
    m_hash = new HashCodeBuilder().append(m_name).append(m_ordinal).append(m_owner).toHashCode();
  }

  @Override
  public IType getType() {
    if (m_type == null) {
      TypeBinding resolveType = m_annotation.type.resolveType(m_scope);
      m_type = JavaModelUtils.bindingToType(resolveType, m_env);
    }
    return m_type;
  }

  @Override
  public IAnnotationValue getValue(String name) {
    return getValues().get(name);
  }

  @Override
  public ListOrderedMap<String, IAnnotationValue> getValues() {
    if (m_values == null) {
      MemberValuePair[] memberValuePairs = m_annotation.memberValuePairs();
      if (memberValuePairs == null || memberValuePairs.length < 1) {
        m_values = ListOrderedMap.listOrderedMap(new HashMap<String, IAnnotationValue>(0));
      }
      else {
        Map<String, IAnnotationValue> result = new HashMap<>(memberValuePairs.length);
        for (MemberValuePair p : memberValuePairs) {
          AnnotationValue v = new AnnotationValue(p, m_scope, this, m_env);
          result.put(v.getName(), v);
        }
        m_values = ListOrderedMap.listOrderedMap(result);
      }
    }
    return m_values;
  }

  @Override
  public IAnnotatable getOwner() {
    return m_owner;
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
    if (!(obj instanceof Annotation)) {
      return false;
    }
    Annotation other = (Annotation) obj;
    if (m_ordinal != other.m_ordinal) {
      return false;
    }
    if (!Arrays.deepEquals(m_name, other.m_name)) {
      return false;
    }
    if (!m_owner.equals(other.m_owner)) {
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
