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
package org.eclipse.scout.sdk.workspace.type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

/**
 *
 */
public class TypeFilters {

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
          ScoutSdk.logWarning("could not create super hierarchy of '" + candidate.getElementName() + "'.", e);
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

  public static ITypeFilter getRegexSimpleNameFilter(final String regex) {
    return new ITypeFilter() {

      @Override
      public boolean accept(IType type) {
        return type.getElementName().matches(regex);
      }
    };
  }

  public static ITypeFilter getCompilationUnitFilter(final ICompilationUnit icu) {
    return new ITypeFilter() {
      public boolean accept(IType type) {
        if (!type.isBinary()) {
          return icu.equals(type.getCompilationUnit());
        }
        return false;
      }
    };
  }

  public static ITypeFilter getInnterTypeFilter(final IType type) {
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

  public static ITypeFilter getPackageFilter(final IPackageFragment packageFragment) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return type.getPackageFragment().equals(packageFragment);
      }
    };
  }

  public static ITypeFilter getTypesInScoutProject(IScoutProject project, boolean includeSubprojects) {
    final HashSet<IJavaProject> projects = new HashSet<IJavaProject>();
    collectJavaProjects(projects, project, includeSubprojects);
    return new ITypeFilter() {

      @Override
      public boolean accept(IType candidate) {
        if (!TypeUtility.exists(candidate) || candidate.isBinary()) {
          return false;
        }
        return projects.contains(candidate.getJavaProject()) && isClass(candidate);
      }
    };
  }

  private static void collectJavaProjects(HashSet<IJavaProject> collector, IScoutProject project, boolean includeSubprojects) {
    if (project != null) {
      for (IScoutBundle b : project.getAllScoutBundles()) {
        collector.add(b.getJavaProject());
      }
      if (includeSubprojects) {
        for (IScoutProject p : project.getSubProjects()) {
          collectJavaProjects(collector, p, includeSubprojects);
        }
      }
    }
  }

  public static ITypeFilter getTypesInProjects(IJavaProject[] projects) {
    final HashSet<IJavaProject> finalProjects = new HashSet<IJavaProject>(Arrays.asList(projects));
    return new ITypeFilter() {

      @Override
      public boolean accept(IType candidate) {
        if (!TypeUtility.exists(candidate) || candidate.isBinary()) {
          return false;
        }
        return finalProjects.contains(candidate.getJavaProject()) && isClass(candidate);
      }
    };
  }

  public static ITypeFilter getClassesInProject(final IJavaProject project) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        if (candidate == null || !candidate.exists() || candidate.isBinary()) {
          return false;
        }
        return candidate.getJavaProject().equals(project) && isClass(candidate);
      }
    };
  }

  public static ITypeFilter getInScoutBundles(final IScoutBundle... bundles) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        if (bundles != null) {
          for (IScoutBundle b : bundles) {
            if (b.contains(type)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  public static ITypeFilter getInScoutProject(final IScoutProject project) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        if (candidate == null || !candidate.exists()) {
          return false;
        }
        return project.contains(candidate);
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
    return new ITypeFilter() {

      @Override
      public boolean accept(IType type) {
        return !type.isBinary() && !type.isReadOnly();
      }
    };
  }

  public static ITypeFilter getToplevelTypeFilter() {
    return new ITypeFilter() {

      @Override
      public boolean accept(IType type) {
        return TypeUtility.exists(type) && !TypeUtility.exists(type.getDeclaringType());
      }
    };
  }

  public static ITypeFilter getInnerClasses(final IType declaringType) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        if (candidate == null || !candidate.exists() || candidate.isBinary()) {
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
          ScoutSdk.logError("could not filter type '" + type.getFullyQualifiedName() + "'.", e);
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
          ScoutSdk.logError("could not filter type '" + type.getFullyQualifiedName() + "'.", e);
          return false;
        }
      }
    };
  }

  public static ITypeFilter getClassFilter() {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        return isClass(candidate);
      }
    };
  }

  public static ITypeFilter getExistingFilter() {
    return new ITypeFilter() {
      public boolean accept(IType type) {
        return type != null && type.exists();
      }
    };
  }

  public static ITypeFilter getNotInTypes(final Set<IType> excludedTypes) {
    return new ITypeFilter() {
      public boolean accept(IType type) {
        return !excludedTypes.contains(type);
      }
    };
  }

  public static ITypeFilter getInterfaceFilter() {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        try {
          int flags = candidate.getFlags();
          return Flags.isInterface(flags) && !Flags.isDeprecated(flags);
        }
        catch (JavaModelException e) {
          ScoutSdk.logWarning("could not evalutate flags of type '" + candidate.getFullyQualifiedName() + "'.", e);
          return false;
        }
      }
    };
  }

  private static boolean isClass(IType type) {
    try {
      int flags = type.getFlags();
      return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("could not evalutate flags of type '" + type.getFullyQualifiedName() + "'.", e);
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
