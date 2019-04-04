/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api.spliterator;

import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link HierarchicalStreamBuilder}</h3>
 *
 * @since 6.1.0
 */
public class HierarchicalStreamBuilder<ELEMENT> {

  private boolean m_includeSuperClasses;
  private boolean m_includeSuperInterfaces;
  private boolean m_includeStartType;

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  public HierarchicalStreamBuilder<ELEMENT> withSuperClasses(boolean includeSuperClasses) {
    m_includeSuperClasses = includeSuperClasses;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  public HierarchicalStreamBuilder<ELEMENT> withSuperInterfaces(boolean includeSuperInterfaces) {
    m_includeSuperInterfaces = includeSuperInterfaces;
    return this;
  }

  protected boolean isIncludeStartType() {
    return m_includeStartType;
  }

  public HierarchicalStreamBuilder<ELEMENT> withStartType(boolean includeStartType) {
    m_includeStartType = includeStartType;
    return this;
  }

  public Stream<ELEMENT> build(IType startType, Function<IType, Spliterator<ELEMENT>> levelSpliteratorProvider) {
    return StreamSupport.stream(new HierarchicalSpliterator<>(Ensure.notNull(startType), isIncludeSuperClasses(), isIncludeSuperInterfaces(), isIncludeStartType(), Ensure.notNull(levelSpliteratorProvider)), false);
  }
}
