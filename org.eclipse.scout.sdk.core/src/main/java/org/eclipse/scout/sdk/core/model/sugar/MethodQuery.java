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

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.WrappedList;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link MethodQuery}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class MethodQuery {
  private final IType m_type;
  private boolean m_includeSuperClasses = false;
  private boolean m_includeSuperInterfaces = false;
  private String m_name;
  private IFilter<IMethod> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public MethodQuery(IType type) {
    m_type = type;
  }

  /**
   * Include / Exclude super classes and super types for visiting
   *
   * @param b
   *          default false
   * @return this
   */
  public MethodQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param b
   *          default false
   * @return this
   */
  public MethodQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * @param b
   *          default fasle
   * @return this
   */
  public MethodQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param name
   * @return this
   */
  public MethodQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * @param filter
   * @return this
   */
  public MethodQuery withFilter(IFilter<IMethod> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * @param maxResultCount
   * @return this
   */
  public MethodQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IMethod f) {
    if (m_name != null && !m_name.equals(f.elementName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(f)) {
      return false;
    }
    return true;
  }

  protected void visitRec(IType t, List<IMethod> result, int maxCount, boolean onlyTraverse) {
    if (t == null) {
      return;
    }
    if (!onlyTraverse) {
      for (IMethod f : new WrappedList<IMethod>(t.unwrap().getMethods())) {
        if (accept(f)) {
          result.add(f);
          if (result.size() >= maxCount) {
            return;
          }
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

  public IMethod first() {
    List<IMethod> result = new ArrayList<>(1);
    visitRec(m_type, result, 1, false);
    return result.isEmpty() ? null : result.get(0);
  }

  public List<IMethod> list() {
    List<IMethod> result = new ArrayList<>(m_type.unwrap().getMethods().size());
    visitRec(m_type, result, m_maxResultCount, false);
    return result;
  }

}
