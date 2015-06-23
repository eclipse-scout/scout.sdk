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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.Predicate;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 *
 */
public final class TypeFilters {

  private static final Predicate<IType> INTERFACE_FILTER = new Predicate<IType>() {
    @Override
    public boolean evaluate(IType candidate) {
      int flags = candidate.getFlags();
      return Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
  };
  private static final Predicate<IType> CLASS_FILTER = new Predicate<IType>() {
    @Override
    public boolean evaluate(IType candidate) {
      return isClass(candidate);
    }
  };
  private static final Predicate<IType> TOP_LEVEL_FILTER = new Predicate<IType>() {
    @Override
    public boolean evaluate(IType type) {
      return type != null && type.getDeclaringType() == null;
    }
  };
  private static final Predicate<IType> NO_GENERIC_FILTER = new Predicate<IType>() {
    @Override
    public boolean evaluate(IType type) {
      return !type.hasTypeParameters();
    }
  };
  private static final Predicate<IType> ENUM_TYPE_FILTER = new Predicate<IType>() {
    @Override
    public boolean evaluate(IType type) {
      int flags = type.getFlags();
      return Flags.isEnum(flags) && !Flags.isDeprecated(flags) && !Flags.isAbstract(flags);
    }
  };
  private static final Predicate<IType> NO_SURROUNDING_CONTEXT_TYPE_FILTER = new Predicate<IType>() {
    @Override
    public boolean evaluate(IType type) {
      return type != null && !type.isAnonymous() && (type.getDeclaringType() == null || Flags.isStatic(type.getFlags()));
    }
  };

  private TypeFilters() {
  }

  public static Predicate<IType> invertFilter(final Predicate<IType> filter) {
    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType type) {
        return !filter.evaluate(type);
      }
    };
  }

  public static Predicate<IType> getSubtypeFilter(final String type) {
    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType candidate) {
        return CoreUtils.isInstanceOf(candidate, type);
      }
    };
  }

  public static Predicate<IType> getSubtypeFilter(IType type) {
    return getSubtypeFilter(type.getName());
  }

  /**
   * Creates a new {@link Predicate<IType>} that only returns {@link IType}s where the simple name exactly matches the
   * given
   * typeName.
   *
   * @param typeName
   *          The simple name the types must have.
   * @return The newly created {@link Predicate<IType>}
   */
  public static Predicate<IType> getElementNameFilter(final String typeName) {
    return getElementNameFilter(typeName, true);
  }

  /**
   * Creates a new {@link Predicate<IType>} that only returns {@link IType}s where the simple name matches the given
   * typeName.
   *
   * @param typeName
   *          The simple name the types must have.
   * @param caseSensitive
   *          {@code true} if case-sensitive comparison should be performed, {@code false} otherwise.
   * @return The newly created {@link Predicate<IType>}
   */
  public static Predicate<IType> getElementNameFilter(final String typeName, boolean caseSensitive) {
    if (caseSensitive) {
      return new Predicate<IType>() {
        @Override
        public boolean evaluate(IType type) {
          return typeName.equals(type.getSimpleName());
        }
      };
    }
    else {
      return new Predicate<IType>() {
        @Override
        public boolean evaluate(IType type) {
          return typeName.equalsIgnoreCase(type.getSimpleName());
        }
      };
    }
  }

  /**
   * Creates and gets a new type filter that accepts all types where the simple name matches the given regex with the
   * given regex flags.
   *
   * @param regex
   *          The expression to use
   * @param regexFlags
   *          The regex flags to use. A bit mask that may include {@link Pattern#CASE_INSENSITIVE},
   *          {@link Pattern#MULTILINE}, {@link Pattern#DOTALL}, {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ},
   *          {@link Pattern#UNIX_LINES}, {@link Pattern#LITERAL} and {@link Pattern#COMMENTS}
   * @return the created filter
   */
  public static Predicate<IType> getRegexSimpleNameFilter(final String regex, int regexFlags) {
    final Pattern pat = Pattern.compile(regex, regexFlags);
    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType type) {
        return pat.matcher(type.getSimpleName()).matches();
      }
    };
  }

  /**
   * Creates and gets a new type filter that accepts all types where the simple name matches the given regex pattern.<br>
   * <b>Note: The given regex uses case-insensitive matching!</b>
   *
   * @param regex
   *          The regex to use for the matching.
   * @return the created filter
   */
  public static Predicate<IType> getRegexSimpleNameFilter(final String regex) {
    return getRegexSimpleNameFilter(regex, Pattern.CASE_INSENSITIVE);
  }

  public static Predicate<IType> getPrimaryTypeFilter() {
    return TOP_LEVEL_FILTER;
  }

  public static Predicate<IType> getInnerClasses(final IType declaringType) {
    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType candidate) {
        IType candidateDeclaringType = candidate.getDeclaringType();
        return candidateDeclaringType != null && candidateDeclaringType.equals(declaringType) && isClass(candidate);
      }
    };
  }

  /**
   * Creates a new {@link Predicate<IType>} that accepts all {@link IType}s that have at least all of the given flags
   * set.
   *
   * @param flags
   *          The flags of the types.
   * @return The newly created {@link Predicate<IType>}.
   */
  public static Predicate<IType> getFlagsFilter(final int flags) {
    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType type) {
        int typeFlags = type.getFlags();
        return ((typeFlags & flags) == flags);
      }
    };
  }

  /**
   * Gets a filter that accepts only types that are classes.<br>
   * A class is defined as a type that is neither abstract, an interface or deprecated.
   *
   * @return The filter that only accepts classes.
   * @see #isClass(IType)
   */
  public static Predicate<IType> getClassFilter() {
    return CLASS_FILTER;
  }

  public static Predicate<IType> getNotInTypes(IType... excludedTypes) {
    Set<IType> excludedSet = null;
    if (excludedTypes != null) {
      excludedSet = new HashSet<>(excludedTypes.length);
      for (IType t : excludedTypes) {
        if (t != null) {
          excludedSet.add(t);
        }
      }
    }
    return getNotInTypes(excludedSet);
  }

  /**
   * Returns an {@link Predicate<IType>} that accepts all {@link IType}s that have no surrounding context {@link IType}.<br>
   * More formally: Accepts all {@link IType}s that are either static or primary types (= have no declaring type).
   *
   * @return an {@link Predicate<IType>} that accepts all {@link IType}s that have no surrounding context {@link IType}.
   */
  public static Predicate<IType> getNoSurroundingContextTypeFilter() {
    return NO_SURROUNDING_CONTEXT_TYPE_FILTER;
  }

  public static Predicate<IType> getNotInTypes(final Set<IType> excludedTypes) {
    if (excludedTypes == null || excludedTypes.size() < 1) {
      return null; // no filter required
    }

    return new Predicate<IType>() {
      @Override
      public boolean evaluate(IType type) {
        return !excludedTypes.contains(type);
      }
    };
  }

  public static Predicate<IType> getInterfaceFilter() {
    return INTERFACE_FILTER;
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
    if (type.isAnonymous()) {
      return false;
    }
    int flags = type.getFlags();
    return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
  }

  /**
   * @return An {@link Predicate<IType>} that only accepts {@link IType}s that are not parameterized (have not
   *         generics).
   */
  public static Predicate<IType> getNoGenericTypesFilter() {
    return NO_GENERIC_FILTER;
  }

  /**
   * @return An {@link Predicate<IType>} that accepts all non-abstract and non-deprecated {@link Enum}s.
   */
  public static Predicate<IType> getEnumTypesFilter() {
    return ENUM_TYPE_FILTER;
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
