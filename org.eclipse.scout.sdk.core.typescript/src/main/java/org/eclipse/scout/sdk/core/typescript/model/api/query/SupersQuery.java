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
import org.eclipse.scout.sdk.core.typescript.model.api.spliterator.SupersSpliterator;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;

/**
 * By default, returns all super classes and super interfaces recursively without the start {@link IES6Class} itself.
 */
public class SupersQuery extends AbstractQuery<IES6Class> {

  private final ES6ClassSpi m_es6ClassSpi;

  private boolean m_includeSelf = false;
  private boolean m_includeSuperClasses = true;
  private boolean m_includeSuperInterfaces = true;

  private String m_name;

  public SupersQuery(ES6ClassSpi es6Class) {
    m_es6ClassSpi = es6Class;
  }

  protected ES6ClassSpi es6Class() {
    return m_es6ClassSpi;
  }

  /**
   * Specifies if the start {@link IES6Class} should be included or not. Default is {@code false}.
   * 
   * @param includeSelf
   *          {@code true} to include the start {@link IES6Class}, {@code false} otherwise.
   * @return This query.
   */
  public SupersQuery withSelf(boolean includeSelf) {
    m_includeSelf = includeSelf;
    return this;
  }

  protected boolean isIncludeSelf() {
    return m_includeSelf;
  }

  /**
   * Specifies if super classes and super interfaces should be included. Default is {@code true}.<br>
   * If the super hierarchy contains the same interface multiple times, it is only returned once.
   *
   * @param includeAllSupers
   *          {@code true} to include super interfaces and super classes recursively, {@code false} to include nothing.
   * @return This query.
   */
  public SupersQuery withAllSupers(boolean includeAllSupers) {
    m_includeSuperClasses = includeAllSupers;
    m_includeSuperInterfaces = includeAllSupers;
    return this;
  }

  /**
   * Specifies if super classes should be included. Default is {@code true}.
   *
   * @param includeSuperClasses
   *          {@code true} to include super classes recursively, {@code false} to exclude super classes.
   * @return This query.
   */
  public SupersQuery withSuperClasses(boolean includeSuperClasses) {
    m_includeSuperClasses = includeSuperClasses;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  /**
   * Specifies if super interfaces should be included. Default is {@code true}.<br>
   * If the super hierarchy contains the same interface multiple times, it is only returned once.
   *
   * @param includeSuperInterfaces
   *          {@code true} to include super interfaces recursively, {@code false} to exclude super interfaces.
   * @return This query.
   */
  public SupersQuery withSuperInterfaces(boolean includeSuperInterfaces) {
    m_includeSuperInterfaces = includeSuperInterfaces;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  /**
   * Limit the super classes or interfaces to the ones having exactly the given name. Default is no filtering by name.
   * 
   * @param name
   *          The name or {@code null} if no filtering by name is required.
   * @return This query.
   */
  public SupersQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
  }

  protected boolean test(@SuppressWarnings("TypeMayBeWeakened") ES6ClassSpi t) {
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
