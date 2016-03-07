/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.handler;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.classid.MissingClassIdsNewOperation;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link CreateMissingClassIdsSelectedHandler}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CreateMissingClassIdsSelectedHandler extends AbstractHandler {
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    final Set<IResource> resourcesFromSelection = S2eUiUtils.getResourcesOfSelection(selection);
    if (resourcesFromSelection.isEmpty()) {
      logNoSelection();
      return null;
    }

    MissingClassIdsNewOperation operation = new MissingClassIdsNewOperation() {
      @Override
      protected ITypeHierarchy createHierarchy(IType iTypeWithClassId, SubMonitor monitor) throws CoreException {
        IRegion region = JavaCore.newRegion();
        boolean added = false;
        for (IResource r : resourcesFromSelection) {
          if (r != null && r.isAccessible()) {
            IJavaElement element = JavaCore.create(r);
            if (S2eUtils.exists(element)) {
              region.add(element);
              added = true;
            }
          }
        }
        if (!added) {
          logNoSelection(); // no classes found.
        }
        ITypeHierarchy result = JavaCore.newTypeHierarchy(region, null, monitor);
        return result;
      }
    };

    new ResourceBlockingOperationJob(operation, resourcesFromSelection.toArray(new IResource[resourcesFromSelection.size()])).schedule();
    return null;
  }

  private static void logNoSelection() {
    SdkLog.warning("Cannot create missing @ClassIds in the selected scope because no resources are selected.");
  }
}
