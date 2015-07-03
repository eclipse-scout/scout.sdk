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
package org.eclipse.scout.sdk.s2e.util;

import java.util.Objects;

import org.apache.commons.collections4.Predicate;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;

/**
 * <h3>{@link JdtTypeFilters}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class JdtTypeFilters {
  private JdtTypeFilters() {
  }

  public static Predicate<IType> getClassFilter() {
    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType candidate) {
        return isClass(candidate);
      }
    };
  }

  /**
   * Gets if the given type is a class.<br>
   * A class is defined as a type that is neither an anonymous, abstract, interface or deprecated type.
   *
   * @param type
   *          The type to check
   * @return true if the given type has none of the following flags: abstract, interface, deprecated
   * @see Flags#isAbstract(int)
   * @see Flags#isInterface(int)
   * @see Flags#isDeprecated(int)
   * @see IType#isAnonymous()
   */
  protected static boolean isClass(IType type) {
    try {
      if (!JdtUtils.exists(type)) {
        return false;
      }
      if (type.isAnonymous()) {
        return false;
      }
      int flags = type.getFlags();
      return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
    catch (JavaModelException e) {
      S2ESdkActivator.logWarning("Unable to retrieve flags of type '" + type.getFullyQualifiedName() + "'.", e);
      return false;
    }
  }

  public static Predicate<IType> getSubtypeFilter(final String typeFqn) {
    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType candidate) {
        if (!JdtUtils.exists(candidate)) {
          return false;
        }
        if (Objects.equals(candidate.getFullyQualifiedName(), typeFqn)) {
          return true;
        }

        try {
          IType[] supertypes = candidate.newSupertypeHierarchy(null).getAllTypes();
          for (IType t : supertypes) {
            if (Objects.equals(t.getFullyQualifiedName(), typeFqn)) {
              return true;
            }
          }
        }
        catch (JavaModelException e) {
          S2ESdkActivator.logWarning("Unable to gets uper hierarchy for type '" + candidate.getFullyQualifiedName() + "'.", e);
        }

        return false;
      }
    };
  }

  @SafeVarargs
  private static Predicate<IType> getMultiFilter(final boolean or, final Predicate<IType>... filters) {
    if (filters == null || filters.length < 1) {
      return null;
    }
    if (filters.length == 1) {
      return filters[0];
    }

    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType candidate) {
        for (Predicate<IType> f : filters) {
          if (f != null) {
            boolean accepted = f.evaluate(candidate);
            if (or == accepted) {
              return accepted;
            }
          }
        }
        return !or;
      }
    };
  }

  @SafeVarargs
  public static Predicate<IType> getMultiFilterOr(final Predicate<IType>... filters) {
    return getMultiFilter(true, filters);
  }

  @SafeVarargs
  public static Predicate<IType> getMultiFilterAnd(final Predicate<IType>... filters) {
    return getMultiFilter(false, filters);
  }
}
