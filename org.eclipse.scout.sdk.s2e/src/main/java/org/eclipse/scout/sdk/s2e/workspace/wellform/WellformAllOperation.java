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
package org.eclipse.scout.sdk.s2e.workspace.wellform;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.workspace.IWorkspaceBlockingOperation;

/**
 * <h3>{@link WellformAllOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class WellformAllOperation implements IWorkspaceBlockingOperation {

  @Override
  public String getOperationName() {
    return "Wellform all Scout classes";
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    int numTicks = 1000;
    int searchStepTicks = 10;
    monitor.beginTask("Wellform Scout classes...", numTicks);
    monitor.setTaskName("Searching for classes...");
    Set<IType> types = new HashSet<>();
    String[] roots = new String[]{IRuntimeClasses.ICodeType, IRuntimeClasses.IDesktop, IRuntimeClasses.IDesktopExtension, IRuntimeClasses.IForm, IRuntimeClasses.IWizard, IRuntimeClasses.IPage, IRuntimeClasses.IOutline};
    for (String root : roots) {
      Set<IType> rootTypes = JdtUtils.resolveJdtTypes(root);
      for (IType t : rootTypes) {
        ITypeHierarchy codeTypeHierarchy = t.newTypeHierarchy(null);
        for (IType candidate : codeTypeHierarchy.getAllClasses()) {
          if (JdtUtils.exists(candidate) && !candidate.isInterface() && !candidate.isBinary() && !candidate.isAnonymous() && candidate.getDeclaringType() == null) {
            types.add(candidate);
          }
          if (monitor.isCanceled()) {
            return;
          }
        }
      }
      monitor.worked(searchStepTicks);
    }

    monitor.setTaskName("Wellform classes...");
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(types, true);
    op.validate();
    op.run(new SubProgressMonitor(monitor, numTicks - (searchStepTicks * roots.length)), workingCopyManager);

    monitor.done();
  }
}
