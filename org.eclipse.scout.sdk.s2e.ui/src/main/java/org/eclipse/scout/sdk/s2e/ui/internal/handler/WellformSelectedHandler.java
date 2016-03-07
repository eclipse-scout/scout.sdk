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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link WellformSelectedHandler}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class WellformSelectedHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    Set<IResource> resourcesFromSelection = S2eUiUtils.getResourcesOfSelection(selection);
    if (resourcesFromSelection.isEmpty()) {
      logNoSelection();
      return null;
    }

    Set<IType> types = toTypes(resourcesFromSelection);
    if (types.isEmpty()) {
      logNoSelection();
      return null;
    }

    Set<IResource> resources = new HashSet<>(types.size());
    for (IType t : types) {
      resources.add(t.getResource());
    }

    new ResourceBlockingOperationJob(new WellformScoutTypeOperation(types, true), resources.toArray(new IResource[resources.size()])).schedule();
    return null;
  }

  private static void logNoSelection() {
    SdkLog.warning("Cannot wellform classes in the selected scope because no classes are selected.");
  }

  private static Set<IType> toTypes(Set<IResource> resources) {
    Set<IType> result = new HashSet<>(resources.size());
    for (IResource r : resources) {
      if (r != null && r.isAccessible()) {
        IJavaElement element = JavaCore.create(r);
        if (S2eUtils.exists(element)) {
          if (element.getElementType() == IJavaElement.TYPE) {
            result.add((IType) element);
          }
          else if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit icu = (ICompilationUnit) element;
            try {
              for (IType t : icu.getTypes()) {
                result.add(t);
              }
            }
            catch (CoreException e) {
              SdkLog.warning("Unable to wellform types in compilation unit {}. Skipping.", icu.getElementName(), e);
            }
          }
        }
      }
    }
    return result;
  }
}
