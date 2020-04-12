/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.spliterator.HierarchicalStreamBuilder;

/**
 * <h3>{@link SuperMethodQuery}</h3> Super method query that by default returns all {@link IMethod}s of all super
 * {@link IType}s including the starting {@link IMethod} itself with the same method identifier.
 *
 * @since 5.1.0
 */
public class SuperMethodQuery extends AbstractQuery<IMethod> {
  private final IMethod m_method;
  private final String m_methodId;

  private boolean m_includeSelf = true;
  private boolean m_includeSuperClasses = true;
  private boolean m_includeSuperInterfaces = true;

  public SuperMethodQuery(IMethod method) {
    m_method = method;
    m_methodId = method.identifier();
  }

  protected String getMethodIdentifier() {
    return m_methodId;
  }

  protected IMethod getMethod() {
    return m_method;
  }

  /**
   * Specifies if the starting {@link IMethod} itself should be part of the result.
   *
   * @param b
   *          {@code true} to include the starting {@link IMethod}, {@code false} otherwise. Default is {@code true}.
   * @return this
   */
  public SuperMethodQuery withSelf(boolean b) {
    m_includeSelf = b;
    return this;
  }

  protected boolean isIncludeSelf() {
    return m_includeSelf;
  }

  /**
   * Include or exclude super types visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          {@code true} if all super classes and super interfaces should be checked for {@link IMethod}s. Default is
   *          {@code true}.
   * @return this
   */
  public SuperMethodQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Include or exclude super class visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          {@code true} if all super classes should be checked for {@link IMethod}s. Default is {@code true}.
   * @return this
   */
  public SuperMethodQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  /**
   * Include or exclude super interface visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          {@code true} if all super interfaces should be checked for {@link IMethod}s. Default is {@code true}.
   * @return this
   */
  public SuperMethodQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  @Override
  protected Stream<IMethod> createStream() {
    return new HierarchicalStreamBuilder<IMethod>()
        .withStartType(isIncludeSelf())
        .withSuperClasses(isIncludeSuperClasses())
        .withSuperInterfaces(isIncludeSuperInterfaces())
        .build(getMethod().declaringType(), level -> MethodQuery.findMatchingMethods(level, getMethodIdentifier()));
  }
}
