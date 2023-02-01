/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.query;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi;

public class FunctionQuery extends AbstractQuery<IFunction> {

  private final ES6ClassSpi m_es6ClassSpi;
  private String m_name;

  public FunctionQuery(ES6ClassSpi es6Class) {
    m_es6ClassSpi = es6Class;
  }

  public FunctionQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
  }

  protected ES6ClassSpi es6Class() {
    return m_es6ClassSpi;
  }

  @Override
  protected Stream<IFunction> createStream() {
    return es6Class().functions().stream()
        .filter(this::test)
        .map(FunctionSpi::api);
  }

  protected boolean test(FunctionSpi function) {
    var name = getName();
    return name == null || name.equals(function.name());
  }
}
