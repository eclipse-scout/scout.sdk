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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Filters;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.TypeFilters;

/**
 * <h3>{@link SuperTypeQuery}</h3>
 * <p>
 * Super type query that by default includes all super classes and super interface (recursive) and the start
 * {@link IType} itself.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class SuperTypeQuery {
  private final IType m_type;
  private boolean m_includeSelf = true;
  private boolean m_includeSuperClasses = true;
  private boolean m_includeSuperInterfaces = true;
  private String m_name;
  private String m_simpleName;
  private int m_flags = -1;
  private IFilter<IType> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public SuperTypeQuery(IType type) {
    m_type = type;
  }

  /**
   * Specifies if the starting {@link IType} itself should be part of the result.
   *
   * @param b
   *          <code>true</code> to include the starting {@link IType}, <code>false</code> otherwise. Default is
   *          <code>true</code>.
   * @return this
   */
  public SuperTypeQuery withSelf(boolean b) {
    m_includeSelf = b;
    return this;
  }

  /**
   * Include or exclude super types visiting when searching for {@link IType}s.
   *
   * @param b
   *          <code>true</code> if all super classes and super interfaces should be part of the result. Default is
   *          <code>true</code>.
   * @return this
   */
  public SuperTypeQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Include or exclude super classes in the result.
   *
   * @param b
   *          <code>true</code> if all super classes should be part of the result. Default is <code>true</code>.
   * @return this
   */
  public SuperTypeQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * Include or exclude super interfaces in the result.
   *
   * @param b
   *          <code>true</code> if all super interfaces should be part of the result (recursively). Default is
   *          <code>true</code>.
   * @return this
   */
  public SuperTypeQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Limit the {@link IType}s to the ones having at least all of the given flags.
   *
   * @param flags
   *          The flags that must exist on the {@link IType}.
   * @return this
   * @see Flags
   */
  public SuperTypeQuery withFlags(int flags) {
    m_flags = flags;
    return this;
  }

  /**
   * Limit the result to {@link IType}s with the given fully qualified name (see {@link IType#name()}).
   *
   * @param name
   *          The fully qualified name to limit to.
   * @return this
   */
  public SuperTypeQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * Limit the result to {@link IType}s with the given sipmle name (see {@link IType#elementName()}).
   *
   * @param name
   *          The simple name to limit to.
   * @return this
   */
  public SuperTypeQuery withSimpleName(String simpleName) {
    m_simpleName = simpleName;
    return this;
  }

  /**
   * Limit the {@link IType}s to the ones that accept the given {@link IFilter}.
   *
   * @param filter
   *          The filter. Default none.
   * @return this
   * @see TypeFilters
   * @see Filters
   */
  public SuperTypeQuery withFilter(IFilter<IType> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * Limit the number of {@link IType}s to search.
   *
   * @param maxResultCount
   *          The maximum number of {@link IType} to search. Default is unlimited.
   * @return this
   */
  public SuperTypeQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IType t) {
    if (!m_includeSelf && m_type == t) {
      return false;
    }
    if (m_flags >= 0 && (t.flags() & m_flags) != m_flags) {
      return false;
    }
    if (m_name != null && !m_name.equals(t.name())) {
      return false;
    }
    if (m_simpleName != null && !m_simpleName.equals(t.elementName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(t)) {
      return false;
    }
    return true;
  }

  protected void visitRec(IType t, Set<IType> result, int maxCount, boolean onlyTraverse) {
    if (t == null) {
      return;
    }
    if (!onlyTraverse && accept(t)) {
      result.add(t);
      if (result.size() >= maxCount) {
        return;
      }
    }
    if (m_includeSuperClasses || m_includeSuperInterfaces) {
      visitRec(t.superClass(), result, maxCount, !m_includeSuperClasses);
      if (result.size() >= maxCount) {
        return;
      }
    }

    if (m_includeSuperInterfaces) {
      for (IType superInterface : t.superInterfaces()) {
        visitRec(superInterface, result, maxCount, false);
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
    Set<IType> result = new HashSet<>(1);
    visitRec(m_type, result, 1, false);
    return result.isEmpty() ? null : result.iterator().next();
  }

  /**
   * Gets all {@link IType}s that fulfill this query.
   *
   * @return A {@link List} with all {@link IType}s that fulfill this query. Never returns <code>null</code>.
   */
  public List<IType> list() {
    Set<IType> result = new HashSet<>();
    visitRec(m_type, result, m_maxResultCount, false);
    return new ArrayList<>(result);
  }

}
