package org.eclipse.scout.sdk.workspace.type;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ScoutTypeFilters extends TypeFilters {
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
}
