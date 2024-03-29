/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.query;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.java.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.query.AbstractQuery;

/**
 * <h3>{@link MethodParameterQuery}</h3> Method parameter query that by default returns all parameters of a method.
 *
 * @since 5.1.0
 */
public class MethodParameterQuery extends AbstractQuery<IMethodParameter> implements Predicate<IMethodParameter> {

  private final MethodSpi m_method;

  private String m_name;
  private String m_dataTypeFqn;

  public MethodParameterQuery(MethodSpi method) {
    m_method = method;
  }

  protected MethodSpi getMethod() {
    return m_method;
  }

  /**
   * Limits the {@link IMethodParameter}s to the one with the given name.
   *
   * @param name
   *          The name to search. Default is not filtering on name.
   * @return this
   */
  public MethodParameterQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
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

  protected String getDataType() {
    return m_dataTypeFqn;
  }

  /**
   * Tests if the given {@link IMethodParameter} fulfills the filter criteria of this query.
   */
  @Override
  public boolean test(IMethodParameter p) {
    var name = getName();
    if (name != null && !name.equals(p.elementName())) {
      return false;
    }

    var dataTypeFqn = getDataType();
    return dataTypeFqn == null || dataTypeFqn.equals(p.dataType().name());
  }

  @Override
  protected Stream<IMethodParameter> createStream() {
    return WrappingSpliterator.<IMethodParameter> stream(getMethod().getParameters())
        .filter(this);
  }
}
