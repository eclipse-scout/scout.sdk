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
import org.eclipse.scout.sdk.core.model.api.internal.WrappedList;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link TypeQuery}</h3> Inner types query that by default returns all direct inner types of the container.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class TypeQuery {
  private final List<IType> m_types;
  private boolean m_includeRecursiveInnerTypes = false;
  private String m_name;
  private String m_simpleName;
  private String m_instanceOfFqn;
  private IFilter<IType> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public TypeQuery(List<IType> types) {
    m_types = types;
  }

  /**
   * Specify if {@link IType}s found by this query should be further searched for their inner {@link IType}s
   * (recursively).<br>
   *
   * @param b
   *          Set to <code>true</code> to include inner types recursively. Default <code>false</code>.
   * @return this
   */
  public TypeQuery withRecursiveInnerTypes(boolean b) {
    m_includeRecursiveInnerTypes = b;
    return this;
  }

  /**
   * Limit the {@link IType}s to the given fully qualified name (see {@link IType#name()}).
   *
   * @param name
   *          The {@link IType} fully qualified name. Default is no filtering.
   * @return this
   */
  public TypeQuery withName(String fullyQualifiedName) {
    m_name = fullyQualifiedName;
    return this;
  }

  /**
   * Limit the {@link IType}s to the given simple name (see {@link IType#elementName()}).
   *
   * @param name
   *          The {@link IType} simple name. Default is no filtering.
   * @return this
   */
  public TypeQuery withSimpleName(String simpleName) {
    m_simpleName = simpleName;
    return this;
  }

  /**
   * Limit the {@link IType}s to the ones that are <code>instanceof</code> the given fully qualified name.<br>
   * This means all resulting {@link IType}s must have the given fully qualified type name in their super hierarchy.
   *
   * @param typeFqn
   *          The fully qualified name.
   * @return this
   */
  public TypeQuery withInstanceOf(String typeFqn) {
    m_instanceOfFqn = typeFqn;
    return this;
  }

  /**
   * Limit the {@link IType}s to the ones that accept the given {@link IFilter}.
   *
   * @param filter
   *          The filter. Default none.
   * @return this
   */
  public TypeQuery withFilter(IFilter<IType> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * Limit the number of {@link IType}s to search.
   *
   * @param maxResultCount
   *          The maximum number of {@link IType}s to search. Default is unlimited.
   * @return this
   */
  public TypeQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IType t) {
    if (m_name != null && !m_name.equals(t.name())) {
      return false;
    }
    if (m_simpleName != null && !m_simpleName.equals(t.elementName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(t)) {
      return false;
    }
    if (m_instanceOfFqn != null && !t.isInstanceOf(m_instanceOfFqn)) {
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
        visitRec(new WrappedList<IType>(t.unwrap().getTypes()), result, maxCount);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  /**
   * Checks if there is at least one {@link IType} that fulfills this query.
   *
   * @return <code>true</code> if at least one {@link IType} fulfills this query, <code>false</code> otherwise.
   */
  public boolean existsAny() {
    return first() != null;
  }

  /**
   * Gets the first {@link IType} that fulfills this query.
   *
   * @return The first {@link IType} that fulfills this query or <code>null</code> if there is none.
   */
  public IType first() {
    List<IType> result = new ArrayList<>(1);
    visitRec(m_types, result, 1);
    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Gets all {@link IType}s that fulfill this query.
   *
   * @return A {@link List} with all {@link IType}s that fulfill this query. Never returns <code>null</code>.
   */
  public List<IType> list() {
    List<IType> result = new ArrayList<>(m_types.size());
    visitRec(m_types, result, m_maxResultCount);
    return result;
  }

}
