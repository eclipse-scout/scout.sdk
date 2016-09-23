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
import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;

/**
 * <h3>{@link MethodParameterQuery}</h3> Method parameter query that by default returns all parameters of a method.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class MethodParameterQuery {
  private final MethodSpi m_method;
  private String m_name;
  private String m_dataTypeFqn;
  private Predicate<IMethodParameter> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public MethodParameterQuery(MethodSpi method) {
    m_method = method;
  }

  /**
   * Limits the {@link IMethodParameter}s to the one with the given name.
   *
   * @param name
   *          The name to search. Default is no filtering on name.
   * @return
   */
  public MethodParameterQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * Limits the {@link IMethodParameter}s to the ones with the given data type fully qualified name.
   *
   * @param dataTypeFqn
   *          The data type to limit the result to. Default is no data type filter.
   * @return this
   */
  public MethodParameterQuery withDataType(String dataTypeFqn) {
    m_dataTypeFqn = dataTypeFqn;
    return this;
  }

  /**
   * Limit the {@link IMethodParameter}s to the ones that accept the given {@link Predicate}.
   *
   * @param filter
   *          The filter. Default none.
   * @return this
   */
  public MethodParameterQuery withFilter(Predicate<IMethodParameter> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * Limit the number of {@link IMethodParameter}s to search.
   *
   * @param maxResultCount
   *          The maximum number of {@link IMethodParameter}s to search. Default is unlimited.
   * @return this
   */
  public MethodParameterQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IMethodParameter p) {
    if (m_name != null && !m_name.equals(p.elementName())) {
      return false;
    }
    if (m_filter != null && !m_filter.test(p)) {
      return false;
    }
    if (m_dataTypeFqn != null && !m_dataTypeFqn.equals(p.dataType().name())) {
      return false;
    }
    return true;
  }

  protected void visit(MethodSpi m, List<IMethodParameter> result, int maxCount) {
    for (MethodParameterSpi spi : m.getParameters()) {
      IMethodParameter p = spi.wrap();
      if (accept(p)) {
        result.add(p);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  /**
   * Checks if there is at least one {@link IMethodParameter} that fulfills this query.
   *
   * @return <code>true</code> if at least one {@link IMethodParameter} fulfills this query, <code>false</code>
   *         otherwise.
   */
  public boolean existsAny() {
    return first() != null;
  }

  /**
   * Gets the first {@link IMethodParameter} that fulfills this query.
   *
   * @return The first {@link IMethodParameter} that fulfills this query or <code>null</code> if there is none.
   */
  public IMethodParameter first() {
    List<IMethodParameter> result = new ArrayList<>(1);
    visit(m_method, result, 1);
    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Gets all {@link IMethodParameter}s that fulfill this query.
   *
   * @return A {@link List} with all {@link IMethodParameter}s that fulfill this query. Never returns <code>null</code>.
   */
  public List<IMethodParameter> list() {
    List<IMethodParameter> result = new ArrayList<>(m_method.getParameters().size());
    visit(m_method, result, m_maxResultCount);
    return result;
  }

}
