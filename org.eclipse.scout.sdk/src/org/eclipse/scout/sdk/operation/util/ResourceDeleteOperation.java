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
package org.eclipse.scout.sdk.operation.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ResourceDeleteOperation implements IOperation {

  private List<IResource> m_resourcesToDelete;

  public ResourceDeleteOperation() {
    m_resourcesToDelete = new ArrayList<IResource>();
  }

  public void setMembers(IResource[] resources) {
    m_resourcesToDelete = CollectionUtility.arrayList(resources);
  }

  public void addResource(IResource resource) {
    m_resourcesToDelete.add(resource);
  }

  public boolean removeMember(IResource resource) {
    return m_resourcesToDelete.remove(resource);
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("delete ");
    for (IResource resource : m_resourcesToDelete) {
      builder.append(resource.getName() + ", ");
    }
    builder.replace(builder.length() - 2, builder.length(), "");
    builder.append("...");
    return builder.toString();
  }

  @Override
  public void validate() {
    if (m_resourcesToDelete == null) {
      throw new IllegalArgumentException("null argument for resources not allowed.");
    }
    for (IResource resource : m_resourcesToDelete) {
      if (resource == null) {
        throw new IllegalArgumentException("null resource in the resource array.");
      }

      try {
        resource.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
      }
      catch (CoreException e) {
        // nop
      }

      if (!resource.exists()) {
        throw new IllegalArgumentException("resource '" + resource.getName() + "' does not exist.");
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IResource resource : m_resourcesToDelete) {
      resource.delete(IResource.FORCE, monitor);
    }
  }
}