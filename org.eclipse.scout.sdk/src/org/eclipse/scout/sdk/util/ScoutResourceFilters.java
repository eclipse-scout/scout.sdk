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
package org.eclipse.scout.sdk.util;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.scout.sdk.util.resources.IResourceFilter;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link ScoutResourceFilters}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2012
 */
public class ScoutResourceFilters extends ResourceFilters {

  /**
   * Gets a filter matching all product files in the given scout bundle or all bundles below the given bundle.
   *
   * @param bundle
   * @return
   */
  public static IResourceFilter getProductFileFilter(IScoutBundle bundle) {
    final HashSet<IProject> projects = new HashSet<>();
    for (IScoutBundle b : bundle.getChildBundles(ScoutBundleFilters.getAllBundlesFilter(), true)) {
      projects.add(b.getProject());
    }
    final IResourceFilter projectFilter = new IResourceFilter() {
      @Override
      public boolean accept(IResourceProxy resource) {
        return projects.contains(resource.requestResource().getProject());
      }
    };
    return getMultifilterAnd(getProductFileFilter(), projectFilter);
  }
}
