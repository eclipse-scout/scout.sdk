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
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;

public class SubTypeQuery extends AbstractQuery<IES6Class> {

  private final ES6ClassSpi m_es6ClassSpi;
  private boolean m_recursive = true;
  private boolean m_includeSelf = false;

  public SubTypeQuery(ES6ClassSpi es6Class) {
    m_es6ClassSpi = es6Class;
  }

  protected ES6ClassSpi es6Class() {
    return m_es6ClassSpi;
  }

  public SubTypeQuery withSelf(boolean b) {
    m_includeSelf = b;
    return this;
  }

  protected boolean isIncludeSelf() {
    return m_includeSelf;
  }

  public SubTypeQuery withRecursive(boolean b) {
    m_recursive = b;
    return this;
  }

  protected boolean isRecursive() {
    return m_recursive;
  }

  @Override
  protected Stream<IES6Class> createStream() {
    var stream = es6Class().inheritors(isRecursive());
    if (isIncludeSelf()) {
      stream = Stream.concat(Stream.of(es6Class()), stream);
    }
    return stream.map(ES6ClassSpi::api);
  }
}
