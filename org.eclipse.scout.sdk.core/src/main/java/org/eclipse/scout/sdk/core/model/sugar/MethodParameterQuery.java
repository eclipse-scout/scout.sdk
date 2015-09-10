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
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link MethodParameterQuery}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class MethodParameterQuery {
  private final IMethod m_method;
  private String m_name;
  private IType m_dataType;
  private IFilter<IMethodParameter> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public MethodParameterQuery(IMethod method) {
    m_method = method;
  }

  /**
   * @param name
   * @return this
   */
  public MethodParameterQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * @param dataType
   * @return this
   */
  public MethodParameterQuery withDataType(IType dataType) {
    m_dataType = dataType;
    return this;
  }

  /**
   * @param filter
   * @return this
   */
  public MethodParameterQuery withFilter(IFilter<IMethodParameter> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * @param maxResultCount
   * @return this
   */
  public MethodParameterQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IMethodParameter p) {
    if (m_name != null && !m_name.equals(p.getElementName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(p)) {
      return false;
    }
    if (m_dataType != null && !m_dataType.getSignature().equals(p.getDataType().getSignature())) {
      return false;
    }
    return true;
  }

  protected void visit(IMethod m, List<IMethodParameter> result, int maxCount) {
    for (IMethodParameter p : m.getParameters()) {
      if (accept(p)) {
        result.add(p);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  public boolean exists() {
    return first() != null;
  }

  public IMethodParameter first() {
    ArrayList<IMethodParameter> result = new ArrayList<>(1);
    visit(m_method, result, 1);
    return result.isEmpty() ? null : result.get(0);
  }

  public List<IMethodParameter> list() {
    ArrayList<IMethodParameter> result = new ArrayList<>();
    visit(m_method, result, m_maxResultCount);
    return result;
  }

}
