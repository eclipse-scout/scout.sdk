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
package org.eclipse.scout.sdk.workspace.resource;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.scout.sdk.util.resources.IResourceFilter;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link ScoutResourceFilters}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2012
 */
public class ScoutResourceFilters extends ResourceFilters {

  public static IResourceFilter getProductFiles(IScoutProject project) {
    final HashSet<IProject> projects = new HashSet<IProject>();
    for (IScoutBundle b : project.getAllScoutBundles()) {
      projects.add(b.getProject());
    }
    final IResourceFilter projectFilter = new IResourceFilter() {
      @Override
      public boolean accept(IResource resource) {

        return projects.contains(resource.getProject());
      }
    };
    return getMultifilterAnd(getProductFilter(), projectFilter);
  }
}
