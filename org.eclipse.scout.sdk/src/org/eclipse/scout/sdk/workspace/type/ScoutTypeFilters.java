package org.eclipse.scout.sdk.workspace.type;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ScoutTypeFilters extends TypeFilters {

  /**
   * Creates and returns a filter that accepts all types that are in the given scout bundles.
   * 
   * @param bundles
   *          The scout bundles in which the types must be.
   * @return the newly created filter.
   */
  public static ITypeFilter getInScoutBundles(final IScoutBundle... bundles) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return isInScoutBundles(type, bundles);
      }
    };
  }

  /**
   * Creates and returns a filter that accepts all types that are in the given scout bundles.<br>
   * Furthermore types that are abstract, an interface or deprecated are not accepted!
   * 
   * @param bundles
   *          The scout bundles in which the types must be.
   * @return the newly created filter.
   * @see TypeFilters#isClass(IType)
   * @see TypeFilters#getClassFilter()
   */
  public static ITypeFilter getTypesInScoutBundles(final IScoutBundle... bundles) {
    return new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        return isClass(type) && isInScoutBundles(type, bundles);
      }
    };
  }

  private static boolean isInScoutBundles(IType type, IScoutBundle... bundles) {
    if (bundles != null) {
      for (IScoutBundle b : bundles) {
        if (b.contains(type)) {
          return true;
        }
      }
    }
    return false;
  }
}
