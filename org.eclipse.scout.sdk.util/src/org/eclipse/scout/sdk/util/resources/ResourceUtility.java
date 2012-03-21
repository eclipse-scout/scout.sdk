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
package org.eclipse.scout.sdk.util.resources;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * <h3>{@link ResourceUtility}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2012
 */
public final class ResourceUtility {

  private ResourceUtility() {
  }

  public static IResource[] getAllResources(IResourceFilter filter) throws CoreException {
    return getAllResources(ResourcesPlugin.getWorkspace().getRoot(), filter);
  }

  public static IResource[] getAllResources(IResource startResource, IResourceFilter filter) throws CoreException {
    List<IResource> collector = new LinkedList<IResource>();
    collectResourcesRec(startResource, collector, filter);
    return collector.toArray(new IResource[collector.size()]);
  }

  private static void collectResourcesRec(IResource resource, Collection<IResource> collector, IResourceFilter filter) throws CoreException {
    if (exists(resource)) {
      if (filter.accept(resource)) {
        collector.add(resource);
      }
      if (resource instanceof IContainer) {
        for (IResource child : ((IContainer) resource).members()) {
          collectResourcesRec(child, collector, filter);
        }
      }
    }
  }

  public static boolean exists(IResource resource) {
    return resource != null && resource.exists();
  }
}
