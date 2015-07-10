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
package org.eclipse.scout.sdk.core.model;

import java.util.regex.Pattern;

import org.apache.commons.collections.Predicate;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 * Contains {@link Predicate}s for {@link IType}s.
 */
public final class TypeFilters {

  private static final Predicate/*<IType>*/ INTERFACE_FILTER = new Predicate/*<IType>*/() {
    @Override
    public boolean evaluate(Object candidate) {
      int flags = ((IType) candidate).getFlags();
      return Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
  };
  private static final Predicate/*<IType>*/ CLASS_FILTER = new Predicate/*<IType>*/() {
    @Override
    public boolean evaluate(Object candidate) {
      return isClass((IType) candidate);
    }
  };
  private static final Predicate/*<IType>*/ TOP_LEVEL_FILTER = new Predicate/*<IType>*/() {
    @Override
    public boolean evaluate(Object type) {
      return type != null && ((IType) type).getDeclaringType() == null;
    }
  };
  private static final Predicate/*<IType>*/ NO_GENERIC_FILTER = new Predicate/*<IType>*/() {
    @Override
    public boolean evaluate(Object type) {
      return !((IType) type).hasTypeParameters();
    }
  };
  private static final Predicate/*<IType>*/ ENUM_TYPE_FILTER = new Predicate/*<IType>*/() {
    @Override
    public boolean evaluate(Object type) {
      int flags = ((IType) type).getFlags();
      return Flags.isEnum(flags) && !Flags.isDeprecated(flags) && !Flags.isAbstract(flags);
    }
  };
  private static final Predicate/*<IType>*/ NO_SURROUNDING_CONTEXT_TYPE_FILTER = new Predicate/*<IType>*/() {
    @Override
    public boolean evaluate(Object type) {
      return type != null && !((IType) type).isAnonymous() && (((IType) type).getDeclaringType() == null || Flags.isStatic(((IType) type).getFlags()));
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
  public static Predicate/*<IType>*/ getSubtypeFilter(final String type) {
    return new Predicate/*<IType>*/() {
      @Override
      public boolean evaluate(Object candidate) {
        return CoreUtils.isInstanceOf((IType) candidate, type);
      }
    };
  }

  /**
   * Creates and gets a {@link Predicate} that evaluates to <code>true</code> for all {@link IType}s that are
   * <code>instanceof</code> the given {@link IType}.
   *
   * @param type
   *          The super {@link IType}.
   * @return The created {@link Predicate}
   */
  public static Predicate/*<IType>*/ getSubtypeFilter(IType type) {
    return getSubtypeFilter(type.getName());
  }

  /**
   * Creates a new {@link Predicate} that only returns {@link IType}s where the simple name exactly matches the
   * given typeName.
   *
   * @param typeName
   *          The simple name the types must have.
   * @return The newly created {@link Predicate}
   */
  public static Predicate/*<IType>*/ getElementNameFilter(final String typeName) {
    return getElementNameFilter(typeName, true);
  }

  /**
   * Creates a new {@link Predicate} that only returns {@link IType}s where the simple name matches the given
   * typeName.
   *
   * @param typeName
   *          The simple name the types must have.
   * @param caseSensitive
   *          {@code true} if case-sensitive comparison should be performed, {@code false} otherwise.
   * @return The newly created {@link Predicate}
   */
  public static Predicate/*<IType>*/ getElementNameFilter(final String typeName, boolean caseSensitive) {
    if (caseSensitive) {
      return new Predicate/*<IType>*/() {
        @Override
        public boolean evaluate(Object type) {
          return typeName.equals(((IType) type).getSimpleName());
        }
      };
    }
    return new Predicate/*<IType>*/() {
      @Override
      public boolean evaluate(Object type) {
        return typeName.equalsIgnoreCase(((IType) type).getSimpleName());
      }
    };
  }

  /**
   * Creates and gets a new {@link Predicate} that accepts all types where the simple name matches the given regular
   * expression with the given expression flags.
   *
   * @param regex
   *          The expression to use
   * @param regexFlags
   *          The regex flags to use. A bit mask that may include {@link Pattern#CASE_INSENSITIVE},
   *          {@link Pattern#MULTILINE}, {@link Pattern#DOTALL}, {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ},
   *          {@link Pattern#UNIX_LINES}, {@link Pattern#LITERAL} and {@link Pattern#COMMENTS}
   * @return the created filter
   * @see Pattern
   */
  public static Predicate/*<IType>*/ getRegexSimpleNameFilter(final String regex, int regexFlags) {
    final Pattern pat = Pattern.compile(regex, regexFlags);
    return new Predicate/*<IType>*/() {
      @Override
      public boolean evaluate(Object type) {
        return pat.matcher(((IType) type).getSimpleName()).matches();
      }
    };
  }

  /**
   * Creates and gets a new {@link Predicate} that accepts all types where the simple name matches the given regular
   * expression.
   * <br>
   * <b>Note: The given regex uses case-insensitive matching!</b>
   *
   * @param regex
   *          The regex to use for the matching.
   * @return the created filter
   * @see Pattern#CASE_INSENSITIVE
   */
  public static Predicate/*<IType>*/ getRegexSimpleNameFilter(final String regex) {
    return getRegexSimpleNameFilter(regex, Pattern.CASE_INSENSITIVE);
  }

  /**
   * Gets a {@link Predicate} that only accepts primary types (having {@link IType#getDeclaringType()} == null).
   *
   * @return The primary type {@link Predicate}.
   */
  public static Predicate/*<IType>*/ getPrimaryTypeFilter() {
    return TOP_LEVEL_FILTER;
  }

  /**
   * Creates a new {@link Predicate} that accepts all {@link IType}s that have at least all of the given flags.
   *
   * @param flags
   *          The flags of the types.
   * @return The newly created {@link Predicate}.
   */
  public static Predicate/*<IType>*/ getFlagsFilter(final int flags) {
    return new Predicate/*<IType>*/() {
      @Override
      public boolean evaluate(Object type) {
        int typeFlags = ((IType) type).getFlags();
        return ((typeFlags & flags) == flags);
      }
    };
  }

  /**
   * Gets a {@link Predicate} that accepts only types that are classes.<br>
   * A class is defined as a type that is neither anonymous, abstract, an interface or deprecated.
   *
   * @return The {@link Predicate} that only accepts classes.
   * @see #isClass(IType)
   */
  public static Predicate/*<IType>*/ getClassFilter() {
    return CLASS_FILTER;
  }

  /**
   * Returns an {@link Predicate} that accepts all {@link IType}s that have no surrounding context {@link IType}.
   * <br>
   * More formally: Accepts all {@link IType}s that are either static or primary types (= have no declaring type).
   *
   * @return an {@link Predicate} that accepts all {@link IType}s that have no surrounding context {@link IType}.
   */
  public static Predicate/*<IType>*/ getNoSurroundingContextTypeFilter() {
    return NO_SURROUNDING_CONTEXT_TYPE_FILTER;
  }

  /**
   * Gets a {@link Predicate} that only accepts interface {@link IType}s.
   *
   * @return The {@link Predicate} only accepting interfaces.
   */
  public static Predicate/*<IType>*/ getInterfaceFilter() {
    return INTERFACE_FILTER;
  }

  /**
   * Gets if the given type is a class.<br>
   * A class is defined as a type that is neither an anonymous, abstract, interface or a deprecated type.
   *
   * @param type
   *          The type to check
   * @return true if the given type is not anonymous and has none of the following flags: abstract, interface,
   *         deprecated
   * @see Flags#isAbstract(int)
   * @see Flags#isInterface(int)
   * @see Flags#isDeprecated(int)
   * @see IType#isAnonymous()
   */
  protected static boolean isClass(IType type) {
    if (type.isAnonymous()) {
      return false;
    }
    int flags = type.getFlags();
    return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
  }

  /**
   * @return An {@link Predicate} that only accepts {@link IType}s that are not parameterized (have not
   *         generics).
   */
  public static Predicate/*<IType>*/ getNoGenericTypesFilter() {
    return NO_GENERIC_FILTER;
  }

  /**
   * @return An {@link Predicate} that accepts all non-abstract and non-deprecated {@link Enum}s.
   */
  public static Predicate/*<IType>*/ getEnumTypesFilter() {
    return ENUM_TYPE_FILTER;
  }

  @SafeVarargs
  private static Predicate/*<IType>*/ getMultiFilter(final boolean or, final Predicate/*<IType>*/... filters) {
    if (filters == null || filters.length < 1) {
      return null;
    }
    if (filters.length == 1) {
      return filters[0];
    }

    return new Predicate/*<IType>*/() {
      @Override
      public boolean evaluate(Object candidate) {
        for (Predicate f : filters) {
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

  /**
   * Gets a {@link Predicate} which evaluates to <code>true</code> if at least one of the given not-null
   * {@link Predicate}s evaluates to <code>true</code>.
   *
   * @param filters
   *          The {@link Predicate}s to evaluate.
   * @return The created {@link Predicate}.
   */
  @SafeVarargs
  public static Predicate/*<IType>*/ getMultiFilterOr(final Predicate/*<IType>*/... filters) {
    return getMultiFilter(true, filters);
  }

  /**
   * Gets a {@link Predicate} which evaluates to <code>true</code> if all the given not-null {@link Predicate}s evaluate
   * to <code>true</code>.
   *
   * @param filters
   *          The {@link Predicate}s to evaluate.
   * @return The created {@link Predicate}.
   */
  @SafeVarargs
  public static Predicate/*<IType>*/ getMultiFilterAnd(final Predicate/*<IType>*/... filters) {
    return getMultiFilter(false, filters);
  }
}
