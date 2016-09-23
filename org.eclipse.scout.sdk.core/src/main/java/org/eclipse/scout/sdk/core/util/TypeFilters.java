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
package org.eclipse.scout.sdk.core.util;

import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * Contains {@link Predicate}s for {@link IType}s.
 */
public final class TypeFilters {

  private static final Predicate<IType> TOP_LEVEL_FILTER = new Predicate<IType>() {
    @Override
    public boolean test(IType type) {
      return type != null && type.declaringType() == null;
    }
  };
  private static final Predicate<IType> NO_GENERIC_FILTER = new Predicate<IType>() {
    @Override
    public boolean test(IType type) {
      return !type.hasTypeParameters();
    }
  };
  private static final Predicate<IType> NO_SURROUNDING_CONTEXT_TYPE_FILTER = new Predicate<IType>() {
    @Override
    public boolean test(IType type) {
      return type != null && !type.isParameterType() && (type.declaringType() == null || Flags.isStatic(type.flags()));
    }
  };

  private TypeFilters() {
  }

  /**
   * Creates and gets a {@link Predicate} that evaluates to <code>true</code> for all {@link IType}s that are
   * <code>instanceof</code> the given fully qualified name.
   *
   * @param type
   *          The fully qualified type name the candidates must be <code>instanceof</code>.
   * @return The created {@link Predicate}
   */
  public static Predicate<IType> instanceOf(final String type) {
    return new Predicate<IType>() {
      @Override
      public boolean test(IType candidate) {
        return candidate.isInstanceOf(type);
      }
    };
  }

  /**
   * Gets a {@link Predicate} that only accepts primary types (having {@link IType#declaringType()} == null).
   *
   * @return The primary type {@link Predicate}.
   */
  public static Predicate<IType> primaryType() {
    return TOP_LEVEL_FILTER;
  }

  /**
   * Returns an {@link Predicate} that accepts all {@link IType}s that have no surrounding context {@link IType}. <br>
   * More formally: Accepts all non-parameter types {@link IType}s that are either static or primary types (= have no
   * declaring type).
   *
   * @return an {@link Predicate} that accepts all {@link IType}s that have no surrounding context {@link IType}.
   */
  public static Predicate<IType> noSurroundingContext() {
    return NO_SURROUNDING_CONTEXT_TYPE_FILTER;
  }

  /**
   * @return An {@link Predicate} that only accepts {@link IType}s that are not parameterized (have not generics).
   */
  public static Predicate<IType> noGenerics() {
    return NO_GENERIC_FILTER;
  }
}
