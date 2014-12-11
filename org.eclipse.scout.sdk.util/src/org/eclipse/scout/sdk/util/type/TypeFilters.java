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
package org.eclipse.scout.sdk.util.type;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.ScoutSdkUtilCore;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 *
 */
public class TypeFilters {

  private static final ITypeFilter INTERFACE_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType candidate) {
      try {
        int flags = candidate.getFlags();
        return Flags.isInterface(flags) && !Flags.isDeprecated(flags);
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logWarning("could not evalutate flags of type '" + candidate.getFullyQualifiedName() + "'.", e);
        return false;
      }
    }
  };
  private static final ITypeFilter CLASS_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType candidate) {
      return isClass(candidate);
    }
  };
  private static final ITypeFilter TOP_LEVEL_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      return type != null && type.getDeclaringType() == null;
    }
  };
  private static final ITypeFilter IN_WORKSPACE_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      return !type.isBinary() && !type.isReadOnly();
    }
  };
  private static final ITypeFilter NO_GENERIC_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      return !TypeUtility.isGenericType(type);
    }
  };
  private static final ITypeFilter ENUM_TYPE_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      try {
        int flags = type.getFlags();
        return Flags.isEnum(flags) && !Flags.isDeprecated(flags) && !Flags.isAbstract(flags);
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logWarning("could not evalutate flags of type '" + type.getFullyQualifiedName() + "'.", e);
        return false;
      }
    }
  };
  private static final ITypeFilter NO_SURROUNDING_CONTEXT_TYPE_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      try {
        return type != null && !type.isAnonymous() && (type.getDeclaringType() == null || Flags.isStatic(type.getFlags()));
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logWarning("could not evalutate flags of type '" + type.getFullyQualifiedName() + "'.", e);
        return false;
      }
    }
  };

  protected TypeFilters() {
  }

  public static ITypeFilter invertFilter(final ITypeFilter filter) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return !filter.accept(type);
      }
    };
  }

  /**
   * @param fqn
   * @return
   * @note this filter is expensive - for every candidate a super type hierarchy will be created. Use
   *       {@link TypeFilters#getSubtypeFilter(IType, ITypeHierarchy)} when ever possible.
   */
  public static ITypeFilter getSubtypeFilter(String fqn) {
    IType type = TypeUtility.getType(fqn);
    if (TypeUtility.exists(type)) {
      return getSubtypeFilter(type);
    }
    else {
      return new ITypeFilter() {
        @Override
        public boolean accept(IType t) {
          return false;
        }
      };
    }
  }

  /**
   * @param type
   * @return
   * @note this filter is expensive - for every candidate a super type hierarchy will be created. Use
   *       {@link TypeFilters#getSubtypeFilter(IType, ITypeHierarchy)} when ever possible.
   */
  public static ITypeFilter getSubtypeFilter(final IType type) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        ITypeHierarchy hierarchy = ScoutSdkUtilCore.getHierarchyCache().getSupertypeHierarchy(candidate);
        return hierarchy != null && hierarchy.contains(type);
      }
    };
  }

  public static ITypeFilter getSubtypeFilter(final IType type, final ITypeHierarchy hierarchy) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        return hierarchy.isSubtype(type, candidate);
      }
    };
  }

  /**
   * Creates a new {@link ITypeFilter} that only returns {@link IType}s where the simple name exactly matches the given
   * typeName.
   *
   * @param typeName
   *          The simple name the types must have.
   * @return The newly created {@link ITypeFilter}
   */
  public static ITypeFilter getElementNameFilter(final String typeName) {
    return getElementNameFilter(typeName, true);
  }

  /**
   * Creates a new {@link ITypeFilter} that only returns {@link IType}s where the simple name matches the given
   * typeName.
   *
   * @param typeName
   *          The simple name the types must have.
   * @param caseSensitive
   *          {@code true} if case-sensitive comparison should be performed, {@code false} otherwise.
   * @return The newly created {@link ITypeFilter}
   */
  public static ITypeFilter getElementNameFilter(final String typeName, boolean caseSensitive) {
    if (caseSensitive) {
      return new ITypeFilter() {
        @Override
        public boolean accept(IType type) {
          return typeName.equals(type.getElementName());
        }
      };
    }
    else {
      return new ITypeFilter() {
        @Override
        public boolean accept(IType type) {
          return typeName.equalsIgnoreCase(type.getElementName());
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
  public static ITypeFilter getRegexSimpleNameFilter(final String regex, int regexFlags) {
    final Pattern pat = Pattern.compile(regex, regexFlags);
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return pat.matcher(type.getElementName()).matches();
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
  public static ITypeFilter getRegexSimpleNameFilter(final String regex) {
    return getRegexSimpleNameFilter(regex, Pattern.CASE_INSENSITIVE);
  }

  public static ITypeFilter getTypesOnClasspath(final IJavaProject project) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        if (!TypeUtility.exists(candidate)) {
          return false;
        }
        return TypeUtility.isOnClasspath(candidate, project);
      }
    };
  }

  public static ITypeFilter getInWorkspaceFilter() {
    return IN_WORKSPACE_FILTER;
  }

  public static ITypeFilter getPrimaryTypeFilter() {
    return TOP_LEVEL_FILTER;
  }

  public static ITypeFilter getInnerClasses(final IType declaringType) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        if (!TypeUtility.exists(candidate)) {
          return false;
        }
        IType candidateDeclaringType = candidate.getDeclaringType();
        return candidateDeclaringType != null && candidateDeclaringType.equals(declaringType) && isClass(candidate);
      }
    };
  }

  /**
   * Creates a new {@link ITypeFilter} that accepts all {@link IType}s that have at least all of the given flags set.
   *
   * @param flags
   *          The flags of the types.
   * @return The newly created {@link ITypeFilter}.
   */
  public static ITypeFilter getFlagsFilter(final int flags) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        try {
          int typeFlags = type.getFlags();
          return ((typeFlags & flags) == flags);
        }
        catch (JavaModelException e) {
          SdkUtilActivator.logError("could not filter type '" + type.getFullyQualifiedName() + "'.", e);
          return false;
        }
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
  public static ITypeFilter getClassFilter() {
    return CLASS_FILTER;
  }

  public static ITypeFilter getNotInTypes(IType... excludedTypes) {
    Set<IType> excludedSet = null;
    if (excludedTypes != null) {
      excludedSet = new HashSet<IType>(excludedTypes.length);
      for (IType t : excludedTypes) {
        if (t != null) {
          excludedSet.add(t);
        }
      }
    }
    return getNotInTypes(excludedSet);
  }

  /**
   * Returns an {@link ITypeFilter} that accepts all {@link IType}s that have no surrounding context {@link IType}.<br>
   * More formally: Accepts all {@link IType}s that are either static or primary types (= have no declaring type).
   *
   * @return an {@link ITypeFilter} that accepts all {@link IType}s that have no surrounding context {@link IType}.
   */
  public static ITypeFilter getNoSurroundingContextTypeFilter() {
    return NO_SURROUNDING_CONTEXT_TYPE_FILTER;
  }

  public static ITypeFilter getNotInTypes(final Set<IType> excludedTypes) {
    if (excludedTypes == null || excludedTypes.size() < 1) {
      return null; // no filter required
    }

    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return !excludedTypes.contains(type);
      }
    };
  }

  public static ITypeFilter getInterfaceFilter() {
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
    try {
      if (!TypeUtility.exists(type)) {
        return false;
      }
      if (type.isAnonymous()) {
        return false;
      }
      int flags = type.getFlags();
      return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not evalutate flags of type '" + type.getFullyQualifiedName() + "'.", e);
      return false;
    }
  }

  /**
   * @return An {@link ITypeFilter} that only accepts {@link IType}s that are not parameterized (have not generics).
   */
  public static ITypeFilter getNoGenericTypesFilter() {
    return NO_GENERIC_FILTER;
  }

  /**
   * @return An {@link ITypeFilter} that accepts all non-abstract and non-deprecated {@link Enum}s.
   */
  public static ITypeFilter getEnumTypesFilter() {
    return ENUM_TYPE_FILTER;
  }

  private static ITypeFilter getMultiTypeFilter(final boolean or, final ITypeFilter... filters) {
    if (filters == null || filters.length < 1) {
      return null;
    }
    if (filters.length == 1) {
      return filters[0];
    }

    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        for (ITypeFilter f : filters) {
          if (f != null) {
            boolean accepted = f.accept(candidate);
            if (or == accepted) {
              return accepted;
            }
          }
        }
        return !or;
      }
    };
  }

  public static ITypeFilter getMultiTypeFilterOr(final ITypeFilter... filters) {
    return getMultiTypeFilter(true, filters);
  }

  public static ITypeFilter getMultiTypeFilterAnd(final ITypeFilter... filters) {
    return getMultiTypeFilter(false, filters);
  }

  public static ITypeFilter getTypeParamSuperTypeFilter(String baseSig, String paramDefiningSuperTypeFqn, int paramIndex) {
    return getTypeParamFilter(baseSig, paramDefiningSuperTypeFqn, paramIndex, false);
  }

  /**
   * Creates and gets a new filter that returns all types whose type parameter is a sub-type of a given base parameter<br>
   * <br>
   * Note: This filter is expensive! Use only on small lists.
   *
   * @param baseSig
   *          The base signature the type parameter must be a sub-type of.
   * @param paramDefiningSuperTypeFqn
   *          The fully qualified name of the class defining the type parameter
   * @param paramIndex
   *          The index of the type parameter
   * @return The new created filter.
   */
  public static ITypeFilter getTypeParamSubTypeFilter(String baseSig, String paramDefiningSuperTypeFqn, int paramIndex) {
    return getTypeParamFilter(baseSig, paramDefiningSuperTypeFqn, paramIndex, true);
  }

  private static ITypeFilter getTypeParamFilter(final String baseSig, final String paramDefiningSuperTypeFqn, final int paramIndex, final boolean sub) {
    if (baseSig == null) {
      return new ITypeFilter() {
        @Override
        public boolean accept(IType type) {
          return true;
        }
      };
    }

    final IType baseType = TypeUtility.getTypeBySignature(baseSig);
    final String objectSig = SignatureCache.createTypeSignature(Object.class.getName());
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        try {
          String typeParamSig = SignatureUtility.resolveTypeParameter(type, paramDefiningSuperTypeFqn, paramIndex);
          if (typeParamSig != null) {
            if (objectSig.equals(typeParamSig)) {
              return true;
            }
            IType typeParam = TypeUtility.getTypeBySignature(typeParamSig);
            if (TypeUtility.exists(typeParam)) {
              if (sub) {
                return TypeUtility.getSupertypeHierarchy(typeParam).contains(baseType);
              }
              else {
                return TypeUtility.getSupertypeHierarchy(baseType).contains(typeParam);
              }
            }
          }
          return true; // generic is not specified -> Object
        }
        catch (CoreException e) {
          SdkUtilActivator.logWarning("Could not evaluate generic parameters of type '" + type.getFullyQualifiedName() + "'.", e);
          return false;
        }
      }
    };
  }
}
