/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.wellform;

import static java.util.Collections.addAll;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link WellformSelectedHandler}</h3>
 *
 * @since 5.1.0
 */
public class WellformSelectedHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) {
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

    ISchedulingRule rule = new MultiRule(resources.toArray(new ISchedulingRule[0]));
    runInEclipseEnvironment(new WellformScoutTypeOperation(types, true), rule);
    return null;
  }

  private static void logNoSelection() {
    SdkLog.warning("Cannot wellform classes in the selected scope because no classes are selected.");
  }

  private static Set<IType> toTypes(Collection<IResource> resources) {
    Set<IType> result = new HashSet<>(resources.size());
    for (IResource r : resources) {
      if (r != null && r.isAccessible()) {
        IJavaElement element = JavaCore.create(r);
        if (JdtUtils.exists(element)) {
          if (element.getElementType() == IJavaElement.TYPE) {
            result.add((IType) element);
          }
          else if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit icu = (ICompilationUnit) element;
            try {
              addAll(result, icu.getTypes());
            }
            catch (JavaModelException e) {
              SdkLog.warning("Unable to wellform types in compilation unit {}. Skipping.", icu.getElementName(), e);
            }
          }
        }
      }
    }
    return result;
  }
}
