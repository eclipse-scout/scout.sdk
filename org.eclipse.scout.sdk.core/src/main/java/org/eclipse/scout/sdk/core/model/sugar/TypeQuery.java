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
package org.eclipse.scout.sdk.core.model.sugar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link TypeQuery}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class TypeQuery {
  private final List<IType> m_types;
  private boolean m_includeRecursiveInnerTypes = false;
  private String m_name;
  private String m_simpleName;
  private IFilter<IType> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public TypeQuery(List<IType> types) {
    m_types = types;
  }

  /**
   * Set to true to include inner types inner types (recursively)
   *
   * @param b
   *          default false
   * @return this
   */
  public TypeQuery withRecursiveInnerTypes(boolean b) {
    m_includeRecursiveInnerTypes = b;
    return this;
  }

  /**
   * @param name
   *          fully qualified name
   * @return this
   */
  public TypeQuery withName(String fullyQualifiedName) {
    m_name = fullyQualifiedName;
    return this;
  }

  /**
   * @param simpleName
   * @return this
   */
  public TypeQuery withSimpleName(String simpleName) {
    m_simpleName = simpleName;
    return this;
  }

  /**
   * @param filter
   * @return this
   */
  public TypeQuery withFilter(IFilter<IType> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * @param maxResultCount
   * @return this
   */
  public TypeQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IType t) {
    if (m_name != null && !m_name.equals(t.getName())) {
      return false;
    }
    if (m_simpleName != null && !m_simpleName.equals(t.getSimpleName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(t)) {
      return false;
    }
    return true;
  }

  protected void visitRec(List<IType> types, List<IType> result, int maxCount) {
    if (types == null || types.isEmpty()) {
      return;
    }
    for (IType t : types) {
      if (accept(t)) {
        result.add(t);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
    if (m_includeRecursiveInnerTypes) {
      for (IType t : types) {
        visitRec(t.getTypes(), result, maxCount);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  public boolean exists() {
    return first() != null;
  }

  public IType first() {
    ArrayList<IType> result = new ArrayList<>(1);
    visitRec(m_types, result, 1);
    return result.isEmpty() ? null : result.get(0);
  }

  public List<IType> list() {
    ArrayList<IType> result = new ArrayList<>();
    visitRec(m_types, result, m_maxResultCount);
    return result;
  }

}
