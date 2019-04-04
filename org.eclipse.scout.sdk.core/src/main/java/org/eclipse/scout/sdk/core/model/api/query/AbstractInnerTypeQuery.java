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
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
  private String m_name;
  private String m_simpleName;
  private String m_instanceOfFqn;
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
  public T withName(String fullyQualifiedName) {
    m_name = fullyQualifiedName;
    return m_thisInstance;
  }

  protected String getName() {
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
  public T withSimpleName(String simpleName) {
    m_simpleName = simpleName;
    return m_thisInstance;
  }

  protected String getSimpleName() {
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
  public T withInstanceOf(String typeFqn) {
    m_instanceOfFqn = typeFqn;
    return m_thisInstance;
  }

  protected String getInstanceOf() {
    return m_instanceOfFqn;
  }

  /**
   * Tests if the given {@link IType} fulfills the filter criteria of this query.
   */
  @Override
  public boolean test(IType t) {
    String name = getName();
    if (name != null && !name.equals(t.name())) {
      return false;
    }

    String simpleName = getSimpleName();
    if (simpleName != null && !simpleName.equals(t.elementName())) {
      return false;
    }

    int flags = getFlags();
    if (flags >= 0 && (t.flags() & flags) != flags) {
      return false;
    }

    String instanceOf = getInstanceOf();
    return instanceOf == null || t.isInstanceOf(instanceOf);
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
