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
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;

public class SupersQuery extends AbstractQuery<IES6Class> {

  private final ES6ClassSpi m_es6ClassSpi;

  private boolean m_includeSelf = true;
  private boolean m_includeSuperClasses = true;
  private boolean m_includeSuperInterfaces = true;

  private String m_name;

  public SupersQuery(ES6ClassSpi es6Class) {
    m_es6ClassSpi = es6Class;
  }

  protected ES6ClassSpi es6Class() {
    return m_es6ClassSpi;
  }

  public SupersQuery withSelf(boolean b) {
    m_includeSelf = b;
    return this;
  }

  protected boolean isIncludeSelf() {
    return m_includeSelf;
  }

  public SupersQuery withAllSupers(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  public SupersQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  public SupersQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  public SupersQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
  }

  public boolean test(@SuppressWarnings("TypeMayBeWeakened") ES6ClassSpi t) {
    var name = getName();
    return name == null || name.equals(t.name());
  }

  @Override
  protected Stream<IES6Class> createStream() {
    return StreamSupport.stream(new SupersSpliterator(es6Class(), isIncludeSuperClasses(), isIncludeSuperInterfaces(), isIncludeSelf()), false)
        .filter(this::test)
        .map(ES6ClassSpi::api);
  }
}
