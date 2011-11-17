package org.eclipse.scout.sdk.workspace.type;

import java.util.HashSet;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ScoutTypeFilters extends TypeFilters {
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
}
