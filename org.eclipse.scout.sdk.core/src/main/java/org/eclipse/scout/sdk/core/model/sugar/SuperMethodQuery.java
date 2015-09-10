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
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link SuperMethodQuery}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class SuperMethodQuery {
  private final IMethod m_method;
  private boolean m_includeSelf = true;
  private boolean m_includeSuperClasses = true;
  private boolean m_includeSuperInterfaces = true;
  private IFilter<IMethod> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public SuperMethodQuery(IMethod method) {
    m_method = method;
  }

  /**
   * @param b
   *          default true
   * @return this
   */
  public SuperMethodQuery withSelf(boolean b) {
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
  public SuperMethodQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param b
   *          default true
   * @return this
   */
  public SuperMethodQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * @param b
   *          default true
   * @return this
   */
  public SuperMethodQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param filter
   * @return this
   */
  public SuperMethodQuery withFilter(IFilter<IMethod> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * @param maxResultCount
   * @return this
   */
  public SuperMethodQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IMethod m) {
    if (!m_includeSelf && m_method == m) {
      return false;
    }
    if (!m_method.getElementName().equals(m.getElementName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(m)) {
      return false;
    }
    return true;
  }

  protected void visitRec(IType t, List<IMethod> result, int maxCount) {
    if (t == null) {
      return;
    }
    for (IMethod m : t.getMethods()) {
      if (accept(m)) {
        result.add(m);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
    if (m_includeSuperClasses) {
      visitRec(t.getSuperClass(), result, maxCount);
      if (result.size() >= maxCount) {
        return;
      }
    }

    if (m_includeSuperInterfaces) {
      for (IType superInterface : t.getSuperInterfaces()) {
        visitRec(superInterface, result, maxCount);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  public boolean exists() {
    return first() != null;
  }

  public IMethod first() {
    ArrayList<IMethod> result = new ArrayList<>(1);
    visitRec(m_method.getDeclaringType(), result, 1);
    return result.isEmpty() ? null : result.get(0);
  }

  public List<IMethod> list() {
    ArrayList<IMethod> result = new ArrayList<>();
    visitRec(m_method.getDeclaringType(), result, m_maxResultCount);
    return result;
  }

}
