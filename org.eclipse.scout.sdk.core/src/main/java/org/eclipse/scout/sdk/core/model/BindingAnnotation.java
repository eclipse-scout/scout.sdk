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
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 *
 */
public class BindingAnnotation implements IAnnotation {

  private final AnnotationBinding m_annotation;
  private final IAnnotatable m_owner;
  private final ClassScope m_scope;
  private final int m_ordinal;
  private final char[] m_id;
  private final int m_hash;
  private final ILookupEnvironment m_env;
  private Map<String, IAnnotationValue> m_values;
  private IType m_type;

  public BindingAnnotation(AnnotationBinding annotation, IAnnotatable owner, ClassScope scope, int ordinal, ILookupEnvironment lookupEnvironment) {
    m_scope = scope;
    m_owner = Validate.notNull(owner);
    m_annotation = Validate.notNull(annotation);
    m_ordinal = ordinal;
    m_env = lookupEnvironment;
    m_id = m_annotation.computeUniqueKey(String.valueOf(owner.hashCode()).toCharArray());
    m_hash = new HashCodeBuilder().append(m_id).append(m_ordinal).append(owner).toHashCode();
  }

  @Override
  public IType getType() {
    if (m_type == null) {
      m_type = JavaModelUtils.bindingToType(m_annotation.getAnnotationType(), m_env);
    }
    return m_type;
  }

  @Override
  public Map<String, IAnnotationValue> getValues() {
    if (m_values == null) {
      ElementValuePair[] memberValuePairs = m_annotation.getElementValuePairs();
      if (memberValuePairs == null || memberValuePairs.length < 1) {
        m_values = new HashMap<>(0);
      }
      else {
        Map<String, IAnnotationValue> result = new HashMap<>(memberValuePairs.length);
        for (ElementValuePair p : memberValuePairs) {
          ElementAnnotationValue v = new ElementAnnotationValue(p, m_scope, this, m_env);
          result.put(v.getName(), v);
        }
        m_values = result;
      }
    }
    return m_values;
  }

  @Override
  public IAnnotationValue getValue(String name) {
    return getValues().get(name);
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
    if (!(obj instanceof BindingAnnotation)) {
      return false;
    }
    BindingAnnotation other = (BindingAnnotation) obj;
    if (m_ordinal != other.m_ordinal) {
      return false;
    }
    if (!Arrays.equals(m_id, other.m_id)) {
      return false;
    }
    return Objects.equals(m_owner, other.m_owner);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }
}
