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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 *
 */
public class TypeFilters {

  private final static ITypeFilter INTERFACE_FILTER = new ITypeFilter() {
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
  private final static ITypeFilter EXISTS_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      return TypeUtility.exists(type);
    }
  };
  private final static ITypeFilter CLASS_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType candidate) {
      return isClass(candidate);
    }
  };
  private final static ITypeFilter TOP_LEVEL_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      return TypeUtility.exists(type) && !TypeUtility.exists(type.getDeclaringType());
    }
  };
  private final static ITypeFilter IN_WORKSPACE_FILTER = new ITypeFilter() {
    @Override
    public boolean accept(IType type) {
      return !type.isBinary() && !type.isReadOnly();
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
   * @param type
   * @return
   * @note this filter is expensive - for every candidate a super type hierarchy will be created. Use
   *       {@link TypeFilters#getSubtypeFilter(IType, ITypeHierarchy)} when ever possible.
   */
  public static ITypeFilter getSubtypeFilter(final IType type) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        try {
          org.eclipse.jdt.core.ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
          return hierarchy.contains(type);
        }
        catch (JavaModelException e) {
          SdkUtilActivator.logWarning("could not create super hierarchy of '" + candidate.getElementName() + "'.", e);
          return false;
        }
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

  public static ITypeFilter getElementNameFilter(final String typeName) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return type.getElementName().equals(typeName);
      }
    };
  }

  /**
   * Creates and gets a new type filter that accepts all types where the simple name matches the given regex with the
   * given regex flags.
   * 
   * @param regex
   *          The expression to use
   * @param flags
   *          The regex flags to use. A bit mask that may include {@link Pattern#CASE_INSENSITIVE},
   *          {@link Pattern#MULTILINE}, {@link Pattern#DOTALL}, {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ},
   *          {@link Pattern#UNIX_LINES}, {@link Pattern#LITERAL} and {@link Pattern#COMMENTS}
   * @return the created filter
   */
  public static ITypeFilter getRegexSimpleNameFilter(final String regex, int flags) {
    final Pattern pat = Pattern.compile(regex, flags);
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

  public static ITypeFilter getInnerTypeFilter(final IType type) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType t) {
        if (t != null) {
          if (t.equals(type)) {
            return true;
          }
          else {
            return accept(t.getDeclaringType());
          }
        }
        return false;
      }
    };
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

  public static ITypeFilter getTopLevelTypeFilter() {
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

  public static ITypeFilter getInHierarchyFilter(final ITypeHierarchy hierarchy) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return hierarchy.contains(type);
      }
    };
  }

  public static ITypeFilter getAbstractOnClasspath(final IJavaProject project) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        try {
          int flags = type.getFlags();
          return !Flags.isInterface(flags) && Flags.isAbstract(flags) && !Flags.isDeprecated(flags) && TypeUtility.isOnClasspath(type, project);
        }
        catch (JavaModelException e) {
          SdkUtilActivator.logError("could not filter type '" + type.getFullyQualifiedName() + "'.", e);
          return false;
        }
      }
    };
  }

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

  public static ITypeFilter getExistingFilter() {
    return EXISTS_FILTER;
  }

  public static ITypeFilter getNotInTypes(IType[]... excludedTypes) {
    HashSet<IType> excludedSet = new HashSet<IType>();
    if (excludedTypes != null) {
      for (IType[] list : excludedTypes) {
        for (IType t : list) {
          excludedSet.add(t);
        }
      }
    }
    return getNotInTypes(excludedSet);
  }

  public static ITypeFilter getNotInTypes(IType... excludedTypes) {
    HashSet<IType> excludedSet = null;
    if (excludedTypes != null) {
      excludedSet = new HashSet<IType>(excludedTypes.length);
      for (IType t : excludedTypes) {
        excludedSet.add(t);
      }
    }
    else {
      excludedSet = new HashSet<IType>(0);
    }
    return getNotInTypes(excludedSet);
  }

  public static ITypeFilter getNotInTypes(final Set<IType> excludedTypes) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        if (excludedTypes == null) {
          return true;
        }
        return !excludedTypes.contains(type);
      }
    };
  }

  public static ITypeFilter getInterfaceFilter() {
    return INTERFACE_FILTER;
  }

  /**
   * Gets if the given type is a class.<br>
   * A class is defined as a type that is neither abstract, an interface or deprecated.
   * 
   * @param type
   *          The type to check
   * @return true if the given type has none of the following flags: abstract, interface, deprecated
   * @see Flags#isAbstract(int)
   * @see Flags#isInterface(int)
   * @see Flags#isDeprecated(int)
   */
  protected static boolean isClass(IType type) {
    try {
      int flags = type.getFlags();
      return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not evalutate flags of type '" + type.getFullyQualifiedName() + "'.", e);
      return false;
    }
  }

  public static ITypeFilter getMultiTypeFilterOr(final ITypeFilter... filters) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        if (filters == null) {
          return true;
        }
        else {
          for (ITypeFilter f : filters) {
            if (f.accept(candidate)) {
              return true;
            }
          }
          return false;
        }
      }
    };
  }

  public static ITypeFilter getMultiTypeFilter(final ITypeFilter... filters) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        if (filters == null) {
          return true;
        }
        else {
          for (ITypeFilter f : filters) {
            if (!f.accept(candidate)) {
              return false;
            }
          }
          return true;
        }
      }
    };
  }
}
