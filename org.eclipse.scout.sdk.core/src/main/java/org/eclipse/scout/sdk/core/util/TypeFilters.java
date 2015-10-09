/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * Contains {@link IFilter}s for {@link IType}s.
 */
public final class TypeFilters {

  private static final IFilter<IType> TOP_LEVEL_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType type) {
      return type != null && type.declaringType() == null;
    }
  };
  private static final IFilter<IType> NO_GENERIC_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType type) {
      return !type.hasTypeParameters();
    }
  };
  private static final IFilter<IType> NO_SURROUNDING_CONTEXT_TYPE_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType type) {
      return type != null && !type.isParameterType() && (type.declaringType() == null || Flags.isStatic(type.flags()));
    }
  };

  private TypeFilters() {
  }

  /**
   * Creates and gets a {@link IFilter} that evaluates to <code>true</code> for all {@link IType}s that are
   * <code>instanceof</code> the given fully qualified name.
   *
   * @param type
   *          The fully qualified type name the candidates must be <code>instanceof</code>.
   * @return The created {@link IFilter}
   */
  public static IFilter<IType> instanceOf(final String type) {
    return new IFilter<IType>() {
      @Override
      public boolean evaluate(IType candidate) {
        return candidate.isInstanceOf(type);
      }
    };
  }

  /**
   * Gets a {@link IFilter} that only accepts primary types (having {@link IType#declaringType()} == null).
   *
   * @return The primary type {@link IFilter}.
   */
  public static IFilter<IType> primaryType() {
    return TOP_LEVEL_FILTER;
  }

  /**
   * Returns an {@link IFilter} that accepts all {@link IType}s that have no surrounding context {@link IType}. <br>
   * More formally: Accepts all non-parameter types {@link IType}s that are either static or primary types (= have no
   * declaring type).
   *
   * @return an {@link IFilter} that accepts all {@link IType}s that have no surrounding context {@link IType}.
   */
  public static IFilter<IType> noSurroundingContext() {
    return NO_SURROUNDING_CONTEXT_TYPE_FILTER;
  }

  /**
   * @return An {@link IFilter} that only accepts {@link IType}s that are not parameterized (have not generics).
   */
  public static IFilter<IType> noGenerics() {
    return NO_GENERIC_FILTER;
  }
}
