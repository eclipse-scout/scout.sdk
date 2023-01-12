/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.spliterator.SuperTypeHierarchySpliterator;

/**
 * <h3>{@link SuperTypeQuery}</h3>
 * <p>
 * Super type query that by default includes all super classes and super interface (recursive) and the start
 * {@link IType} itself.
 *
 * @since 5.1.0
 */
public class SuperTypeQuery extends AbstractQuery<IType> implements Predicate<IType> {

  private final IType m_type;

  private boolean m_includeSelf = true;
  private boolean m_includeSuperClasses = true;
  private boolean m_includeSuperInterfaces = true;

  private ApiFunction<?, ITypeNameSupplier> m_name;
  private String m_simpleName;
  private int m_flags = -1;

  public SuperTypeQuery(IType type) {
    m_type = type;
  }

  protected IType getType() {
    return m_type;
  }

  /**
   * Specifies if the starting {@link IType} itself should be part of the result.
   *
   * @param b
   *          {@code true} to include the starting {@link IType}, {@code false} otherwise. Default is {@code true}.
   * @return this
   */
  public SuperTypeQuery withSelf(boolean b) {
    m_includeSelf = b;
    return this;
  }

  protected boolean isIncludeSelf() {
    return m_includeSelf;
  }

  /**
   * Include or exclude super types visiting when searching for {@link IType}s.
   *
   * @param b
   *          {@code true} if all super classes and super interfaces should be part of the result. Default is
   *          {@code true}.
   * @return this
   */
  public SuperTypeQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Include or exclude super classes in the result.
   *
   * @param b
   *          {@code true} if all super classes should be part of the result. Default is {@code true}.
   * @return this
   */
  public SuperTypeQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  /**
   * Include or exclude super interfaces in the result.
   *
   * @param b
   *          {@code true} if all super interfaces should be part of the result (recursively). Default is {@code true}.
   * @return this
   */
  public SuperTypeQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  /**
   * Limit the {@link IType}s to the ones having at least all of the given flags.
   *
   * @param flags
   *          The flags that must exist on the {@link IType}.
   * @return this
   * @see Flags
   */
  public SuperTypeQuery withFlags(int flags) {
    m_flags = flags;
    return this;
  }

  protected int getFlags() {
    return m_flags;
  }

  /**
   * Limit the result to {@link IType}s with the given fully qualified name (see {@link IType#name()}).
   *
   * @param fullyQualifiedName
   *          The fully qualified name to limit to.
   * @return this
   */
  public SuperTypeQuery withName(CharSequence fullyQualifiedName) {
    return withNameFrom(null, api -> ITypeNameSupplier.of(fullyQualifiedName));
  }

  /**
   * Limit the {@link IType}s to the {@link ITypeNameSupplier} returned by the given nameFunction.<br>
   * <b>Example:</b> {@code type.superTypes().withNameFrom(IJavaApi.class, IJavaApi::List)}.
   *
   * @param api
   *          The api type that defines the type. An instance of this API is passed to the nameFunction. May be
   *          {@code null} in case the given nameFunction can handle a {@code null} input.
   * @param nameFunction
   *          A {@link Function} to be called to obtain the fully qualified type name to search.
   * @param <API>
   *          The API type that contains the class name
   * @return this
   */
  public <API extends IApiSpecification> SuperTypeQuery withNameFrom(Class<API> api, Function<API, ITypeNameSupplier> nameFunction) {
    if (nameFunction == null) {
      m_name = null;
    }
    else {
      m_name = new ApiFunction<>(api, nameFunction);
    }
    return this;
  }

  protected ApiFunction<?, ITypeNameSupplier> getName() {
    return m_name;
  }

  /**
   * Limit the result to {@link IType}s with the given simple name (see {@link IType#elementName()}).
   *
   * @param simpleName
   *          The simple name to limit to.
   * @return this
   */
  public SuperTypeQuery withSimpleName(String simpleName) {
    m_simpleName = simpleName;
    return this;
  }

  protected String getSimpleName() {
    return m_simpleName;
  }

  /**
   * Tests if the given {@link IType} fulfills the filter criteria of this query.
   */
  @Override
  public boolean test(IType t) {
    var flags = getFlags();
    if (flags >= 0 && (t.flags() & flags) != flags) {
      return false;
    }

    var name = getName();
    if (name != null) {
      var fqnMatches = name.apply(t.javaEnvironment())
          .map(ITypeNameSupplier::fqn)
          .map(fqn -> fqn.equals(t.name()))
          .orElse(false);
      if (!fqnMatches) {
        return false;
      }
    }

    var simpleName = getSimpleName();
    return simpleName == null || simpleName.equals(t.elementName());
  }

  @Override
  protected Stream<IType> createStream() {
    return StreamSupport.stream(new SuperTypeHierarchySpliterator(getType(), isIncludeSuperClasses(), isIncludeSuperInterfaces(), isIncludeSelf()), false)
        .filter(this);
  }
}
