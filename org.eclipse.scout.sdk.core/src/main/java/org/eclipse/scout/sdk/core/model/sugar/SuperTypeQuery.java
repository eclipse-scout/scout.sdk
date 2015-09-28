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
 * <h3>{@link SuperTypeQuery}</h3>
 * <p>
 * By default all recursive super classes and recursive super interface are included
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
  private IFilter<IType> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public SuperTypeQuery(IType type) {
    m_type = type;
  }

  /**
   * @param b
   *          default true
   * @return this
   */
  public SuperTypeQuery withSelf(boolean b) {
    m_includeSelf = b;
    return this;
  }

  /**
   * Include / Exclude super classes and super types for visiting
   *
   * @param b
   *          default false
   * @return this
   */
  public SuperTypeQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param b
   *          default true
   * @return this
   */
  public SuperTypeQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * @param b
   *          default true
   * @return this
   */
  public SuperTypeQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param name
   * @return this
   */
  public SuperTypeQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * @param simpleName
   * @return this
   */
  public SuperTypeQuery withSimpleName(String simpleName) {
    m_simpleName = simpleName;
    return this;
  }

  /**
   * @param filter
   * @return this
   */
  public SuperTypeQuery withFilter(IFilter<IType> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * @param maxResultCount
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

  protected void visitRec(IType t, List<IType> result, int maxCount, boolean onlyTraverse) {
    if (t == null) {
      return;
    }
    if (!onlyTraverse) {
      if (accept(t)) {
        result.add(t);
        if (result.size() >= maxCount) {
          return;
        }
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

  public boolean existsAny() {
    return first() != null;
  }

  public IType first() {
    List<IType> result = new ArrayList<>(1);
    visitRec(m_type, result, 1, false);
    return result.isEmpty() ? null : result.get(0);
  }

  public List<IType> list() {
    List<IType> result = new ArrayList<>();
    visitRec(m_type, result, m_maxResultCount, false);
    return result;
  }

}
