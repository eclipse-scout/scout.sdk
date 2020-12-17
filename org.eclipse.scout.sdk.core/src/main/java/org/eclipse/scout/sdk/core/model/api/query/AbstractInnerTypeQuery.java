/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.spliterator.InnerTypeSpliterator;

/**
 * <h3>{@link AbstractInnerTypeQuery}</h3> Inner types query that by default returns the given {@link IType} list.
 *
 * @since 5.1.0
 */
public abstract class AbstractInnerTypeQuery<T extends AbstractInnerTypeQuery<T>> extends AbstractQuery<IType> implements Predicate<IType> {

  private final Spliterator<IType> m_innerTypes;
  private final T m_thisInstance;

  private boolean m_includeRecursiveInnerTypes;
  private ApiFunction<?, IClassNameSupplier> m_name;
  private ApiFunction<?, IClassNameSupplier> m_simpleName;
  private ApiFunction<?, IClassNameSupplier> m_instanceOf;
  private int m_flags = -1;

  @SuppressWarnings("unchecked")
  protected AbstractInnerTypeQuery(Spliterator<IType> innerTypes) {
    m_innerTypes = innerTypes;
    m_thisInstance = (T) this;
  }

  protected Spliterator<IType> getInnerTypeSpliterator() {
    return m_innerTypes;
  }

  /**
   * Specify if {@link IType}s found by this query should be further searched for their inner {@link IType}s
   * (recursively).<br>
   * Default is {@code false}.
   *
   * @param b
   *          Set to {@code true} to include inner types recursively.
   * @return this
   */
  public T withRecursiveInnerTypes(boolean b) {
    m_includeRecursiveInnerTypes = b;
    return m_thisInstance;
  }

  protected boolean isIncludeRecursiveInnerTypes() {
    return m_includeRecursiveInnerTypes;
  }

  /**
   * Limit the {@link IType}s to the ones having at least all of the given flags.<br>
   * Default is no filtering.
   *
   * @param flags
   *          The flags that must exist on the {@link IType}.
   * @return this
   * @see Flags
   */
  public T withFlags(int flags) {
    m_flags = flags;
    return m_thisInstance;
  }

  protected int getFlags() {
    return m_flags;
  }

  /**
   * Limit the {@link IType}s to the given fully qualified name (see {@link IType#name()}).<br>
   * Default is no filtering.
   *
   * @param fullyQualifiedName
   *          The {@link IType} fully qualified name.
   * @return this
   */
  public T withName(CharSequence fullyQualifiedName) {
    var supplier = IClassNameSupplier.raw(fullyQualifiedName);
    return withNameFrom(null, api -> supplier);
  }

  /**
   * Limit the {@link IType}s to the {@link IClassNameSupplier} returned by the given nameFunction.<br>
   * <b>Example:</b> {@code type.innerTypes().withNameFrom(IJavaApi.class, IJavaApi::List)}.
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
  public <API extends IApiSpecification> T withNameFrom(Class<API> api, Function<API, IClassNameSupplier> nameFunction) {
    if (nameFunction == null) {
      m_name = null;
    }
    else {
      m_name = new ApiFunction<>(api, nameFunction);
    }
    return m_thisInstance;
  }

  protected ApiFunction<?, IClassNameSupplier> getName() {
    return m_name;
  }

  /**
   * Limit the {@link IType}s to the given simple name (see {@link IType#elementName()}).<br>
   * Default is no filtering.
   *
   * @param simpleName
   *          The {@link IType} simple name.
   * @return this
   */
  public T withSimpleName(CharSequence simpleName) {
    var supplier = IClassNameSupplier.raw(simpleName);
    return withSimpleNameFrom(null, api -> supplier);
  }

  /**
   * Limit the {@link IType}s to the {@link IClassNameSupplier} returned by the given simpleNameFunction.<br>
   * <b>Example:</b> {@code type.innerTypes().withSimpleNameFrom(IJavaApi.class, IJavaApi::List)}.
   * 
   * @param api
   *          The api type that defines the type. An instance of this API is passed to the simpleNameFunction. May be
   *          {@code null} in case the given simpleNameFunction can handle a {@code null} input.
   * @param simpleNameFunction
   *          A {@link Function} to be called to obtain the simple type name to search.
   * @param <API>
   *          The API type that contains the class name
   * @return this
   */
  public <API extends IApiSpecification> T withSimpleNameFrom(Class<API> api, Function<API, IClassNameSupplier> simpleNameFunction) {
    if (simpleNameFunction == null) {
      m_simpleName = null;
    }
    else {
      m_simpleName = new ApiFunction<>(api, simpleNameFunction);
    }
    return m_thisInstance;
  }

  protected ApiFunction<?, IClassNameSupplier> getSimpleName() {
    return m_simpleName;
  }

  /**
   * Limit the {@link IType}s to the ones that are {@code instanceof} the given fully qualified name.<br>
   * This means all resulting {@link IType}s must have the given fully qualified type name in their super hierarchy.<br>
   * Default is no filtering.
   *
   * @param typeFqn
   *          The fully qualified name.
   * @return this
   */
  public T withInstanceOf(CharSequence typeFqn) {
    return withInstanceOf(IClassNameSupplier.raw(typeFqn));
  }

  /**
   * Limit the {@link IType}s to the ones that are {@code instanceof} the given {@link IClassNameSupplier}.<br>
   * This means all resulting {@link IType}s must have the fully qualified name of the {@link IClassNameSupplier} in
   * their super hierarchy.<br>
   * Default is no filtering.
   *
   * @param typeFqnSupplier
   *          The {@link IClassNameSupplier} specifying the super type or {@code null} for no filtering.
   * @return this
   */
  public T withInstanceOf(IClassNameSupplier typeFqnSupplier) {
    return withInstanceOfFrom(null, api -> typeFqnSupplier);
  }

  /**
   * Limit the {@link IType}s to the ones that ar {@code instanceof} the {@link IClassNameSupplier} returned by the
   * given nameFunction.<br>
   * <b>Example:</b> {@code type.innerTypes().withInstanceOfFrom(IJavaApi.class, IJavaApi::List)}.
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
  public <API extends IApiSpecification> T withInstanceOfFrom(Class<API> api, Function<API, IClassNameSupplier> nameFunction) {
    if (nameFunction == null) {
      m_instanceOf = null;
    }
    else {
      m_instanceOf = new ApiFunction<>(api, nameFunction);
    }
    return m_thisInstance;
  }

  protected ApiFunction<?, IClassNameSupplier> getInstanceOf() {
    return m_instanceOf;
  }

  /**
   * Tests if the given {@link IType} fulfills the filter criteria of this query.
   */
  @Override
  public boolean test(IType t) {
    var context = t.javaEnvironment();

    var flags = getFlags();
    if (flags >= 0 && (t.flags() & flags) != flags) {
      return false;
    }

    var instanceOf = Optional.ofNullable(getInstanceOf())
        .flatMap(nameFilter -> nameFilter.apply(context))
        .orElse(null);
    if (instanceOf != null && !t.isInstanceOf(instanceOf)) {
      return false;
    }

    var name = Optional.ofNullable(getName())
        .flatMap(nameFilter -> nameFilter.apply(context))
        .map(IClassNameSupplier::fqn)
        .orElse(null);
    if (name != null && !name.equals(t.name())) {
      return false;
    }

    var simpleName = Optional.ofNullable(getSimpleName())
        .flatMap(simpleNameFilter -> simpleNameFilter.apply(context))
        .map(IClassNameSupplier::simpleName)
        .orElse(null);
    return simpleName == null || simpleName.equals(t.elementName());
  }

  @Override
  public Stream<IType> stream() {
    return super.stream().filter(this);
  }

  @Override
  protected Stream<IType> createStream() {
    return StreamSupport.stream(new InnerTypeSpliterator(getInnerTypeSpliterator(), isIncludeRecursiveInnerTypes()), false);
  }
}
