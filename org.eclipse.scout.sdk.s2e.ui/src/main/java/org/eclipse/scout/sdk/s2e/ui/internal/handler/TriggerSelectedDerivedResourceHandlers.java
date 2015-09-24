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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link TriggerSelectedDerivedResourceHandlers}</h3>
 * <p>
 * Only trigger in the selected workspace projects
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class TriggerSelectedDerivedResourceHandlers extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    MessageBox messageBox = new MessageBox(HandlerUtil.getActiveShellChecked(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage("This will update all @Generated classes in the selected projects.\nDepending on the size of your workspace this can take several minutes.\nDo you really want to update all @Generated classes?");
    messageBox.setText("Do you really want to update all @Generated classes?");
    int answer = messageBox.open();
    if (answer == SWT.YES) {
      ScoutSdkCore.getDerivedResourceManager().triggerAll(createJavaSeachScope(event));
    }
    return null;
  }

  private static IJavaSearchScope createJavaSeachScope(ExecutionEvent event) {
    Set<IResource> resourceSet = new LinkedHashSet<>();
    ISelection selection0 = HandlerUtil.getCurrentSelection(event);
    if (!selection0.isEmpty() && selection0 instanceof IStructuredSelection) {
      for (Object selElem : ((IStructuredSelection) selection0).toArray()) {
        if (selElem instanceof IWorkingSet) {
          IWorkingSet workingSet = (IWorkingSet) selElem;
          if (workingSet.isEmpty() && workingSet.isAggregateWorkingSet()) {
            continue;
          }
          for (IAdaptable workingSetElement : workingSet.getElements()) {
            Object o = workingSetElement.getAdapter(IResource.class);
            if (o instanceof IResource) {
              IResource resource = (IResource) o;
              if (resource.isAccessible()) {
                resourceSet.add(resource);
              }
            }
          }
        }
        else if (selElem instanceof IAdaptable) {
          Object o = ((IAdaptable) selElem).getAdapter(IResource.class);
          if (o instanceof IResource) {
            IResource resource = (IResource) o;
            if (resource.isAccessible()) {
              resourceSet.add(resource);
            }
          }
        }
      }
    }
    Set<IJavaElement> jset = new LinkedHashSet<>();
    for (IResource r : resourceSet) {
      IJavaElement e = JavaCore.create(r);
      if (e.exists()) {
        jset.add(e);
      }
    }
    return SearchEngine.createJavaSearchScope(jset.toArray(new IJavaElement[0]));
  }

}
