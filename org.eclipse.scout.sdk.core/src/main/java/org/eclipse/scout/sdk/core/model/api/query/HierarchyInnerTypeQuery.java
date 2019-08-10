/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.spliterator.HierarchicalStreamBuilder;
import org.eclipse.scout.sdk.core.model.api.spliterator.InnerTypeSpliterator;

/**
 * <h3>{@link HierarchyInnerTypeQuery}</h3>
 *
 * @since 6.1.0
 */
public class HierarchyInnerTypeQuery extends AbstractInnerTypeQuery<HierarchyInnerTypeQuery> {

  private final IType m_ownerType;
  private boolean m_includeSuperClasses;

  public HierarchyInnerTypeQuery(IType owner) {
    super(InnerTypeSpliterator.innerTypesOf(owner));
    m_ownerType = owner;
  }

  protected IType getOwnerType() {
    return m_ownerType;
  }

  /**
   * Include or exclude super class visiting when searching for {@link IType}s.
   *
   * @param b
   *          {@code true} if all super classes should be checked for {@link IType}s. Default is {@code false}.
   * @return this
   */
  public HierarchyInnerTypeQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  public boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  @Override
  protected Stream<IType> createStream() {
    if (isIncludeSuperClasses()) {
      return new HierarchicalStreamBuilder<IType>()
          .withSuperClasses(true)
          .withStartType(true)
          .build(getOwnerType(), level -> new InnerTypeSpliterator(level, isIncludeRecursiveInnerTypes()));
    }
    return super.createStream();
  }
}
