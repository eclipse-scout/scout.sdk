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

import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * Contains {@link IFilter}s for {@link IType}s.
 */
public final class TypeFilters {

  private static final IFilter<IType> INTERFACE_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType candidate) {
      int flags = candidate.flags();
      return Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
  };
  private static final IFilter<IType> CLASS_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType candidate) {
      return isClass(candidate);
    }
  };
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
  private static final IFilter<IType> ENUM_TYPE_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType type) {
      int flags = type.flags();
      return Flags.isEnum(flags) && !Flags.isDeprecated(flags) && !Flags.isAbstract(flags);
    }
  };
  private static final IFilter<IType> NO_SURROUNDING_CONTEXT_TYPE_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType type) {
      return type != null && !type.isAnonymous() && (type.declaringType() == null || Flags.isStatic(type.flags()));
    }
  };

  private TypeFilters() {
  }

  /**
   * Creates and gets a {@link IFilter} that evaluates to <code>true</code> for all {@link IType}s that are not of the
   * given {@link IType}.
   *
   * @param type
   *          The {@link IType} to exclude
   * @return The created {@link IFilter}
   */
  public static IFilter<IType> exclude(final IType type) {
    return new IFilter<IType>() {
      @Override
      public boolean evaluate(IType candidate) {
        return !type.name().equals(candidate.name());
      }
    };
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
   * Creates and gets a {@link IFilter} that evaluates to <code>true</code> for all {@link IType}s that are
   * <code>instanceof</code> the given {@link IType}.
   *
   * @param type
   *          The super {@link IType}.
   * @return The created {@link IFilter}
   */
  public static IFilter<IType> instanceOf(IType type) {
    return instanceOf(type.name());
  }

  /**
   * Creates a new {@link IFilter} that only returns {@link IType}s where the simple name exactly matches the given
   * typeName (case sensitive).
   *
   * @param typeName
   *          The simple name the types must have.
   * @return The newly created {@link IFilter}
   */
  public static IFilter<IType> simpleName(final String typeName) {
    return new IFilter<IType>() {
      @Override
      public boolean evaluate(IType type) {
        return typeName.equals(type.elementName());
      }
    };
  }

  /**
   * Creates and gets a new {@link IFilter} that accepts all types where the simple name matches the given regular
   * expression.
   *
   * @param regex
   *          The regular expression {@link Pattern} to use.
   * @return the created filter
   * @see Pattern
   */
  public static IFilter<IType> simpleNameRegex(final Pattern regex) {
    return new IFilter<IType>() {
      @Override
      public boolean evaluate(IType type) {
        return regex.matcher(type.elementName()).matches();
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
   * Creates a new {@link IFilter} that accepts all {@link IType}s that have at least all of the given flags.
   *
   * @param flags
   *          The flags of the types.
   * @return The newly created {@link IFilter}.
   */
  public static IFilter<IType> flags(final int flags) {
    return new IFilter<IType>() {
      @Override
      public boolean evaluate(IType type) {
        int typeFlags = type.flags();
        return ((typeFlags & flags) == flags);
      }
    };
  }

  /**
   * Gets a {@link IFilter} that accepts only types that are classes.<br>
   * A class is defined as a type that is neither anonymous, abstract, an interface or deprecated.
   *
   * @return The {@link IFilter} that only accepts classes.
   * @see #isClass(IType)
   */
  public static IFilter<IType> classes() {
    return CLASS_FILTER;
  }

  /**
   * Gets a {@link IFilter} that only accepts interface {@link IType}s that are not deprecated.
   *
   * @return The {@link IFilter} only accepting interfaces whicht are not deprecated.
   */
  public static IFilter<IType> interfaces() {
    return INTERFACE_FILTER;
  }

  /**
   * @return An {@link IFilter} that accepts all non-abstract and non-deprecated {@link Enum}s.
   */
  public static IFilter<IType> enums() {
    return ENUM_TYPE_FILTER;
  }

  /**
   * Returns an {@link IFilter} that accepts all {@link IType}s that have no surrounding context {@link IType}. <br>
   * More formally: Accepts all non-anonymous {@link IType}s that are either static or primary types (= have no
   * declaring type).
   *
   * @return an {@link IFilter} that accepts all {@link IType}s that have no surrounding context {@link IType}.
   */
  public static IFilter<IType> noSurroundingContext() {
    return NO_SURROUNDING_CONTEXT_TYPE_FILTER;
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
    int flags = type.flags();
    return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
  }

  /**
   * @return An {@link IFilter} that only accepts {@link IType}s that are not parameterized (have not generics).
   */
  public static IFilter<IType> noGenerics() {
    return NO_GENERIC_FILTER;
  }
}
