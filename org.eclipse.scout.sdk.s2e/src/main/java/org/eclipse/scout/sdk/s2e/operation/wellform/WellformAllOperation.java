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
package org.eclipse.scout.sdk.s2e.operation.wellform;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link WellformAllOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class WellformAllOperation implements IOperation {

  @Override
  public String getOperationName() {
    return "Wellform all Scout classes";
  }

  @Override
  public void validate() {
    // no input: nothing to validate
  }

  @Override
  @SuppressWarnings("squid:S1067")
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    int numTicks = 100;
    int searchStepTicks = 1;
    SubMonitor progress = SubMonitor.convert(monitor, "Wellform Scout classes...", numTicks);
    progress.subTask("Searching for classes...");

    Set<IType> types = new HashSet<>();
    String[] roots = new String[]{IScoutRuntimeTypes.ICodeType, IScoutRuntimeTypes.IDesktop, IScoutRuntimeTypes.IDesktopExtension, IScoutRuntimeTypes.IForm, IScoutRuntimeTypes.IWizard, IScoutRuntimeTypes.IPage, IScoutRuntimeTypes.IOutline};
    for (String root : roots) {
      Set<IType> rootTypes = S2eUtils.resolveJdtTypes(root);
      for (IType t : rootTypes) {
        ITypeHierarchy codeTypeHierarchy = t.newTypeHierarchy(null);
        for (IType candidate : codeTypeHierarchy.getAllClasses()) {
          if (S2eUtils.exists(candidate) && !candidate.isInterface() && !candidate.isBinary() && !candidate.isAnonymous() && candidate.getDeclaringType() == null) {
            types.add(candidate);
          }
          if (monitor.isCanceled()) {
            return;
          }
        }
      }
      monitor.worked(searchStepTicks);
    }

    monitor.subTask("Wellform classes...");
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(types, true);
    op.validate();
    op.run(progress.newChild(numTicks - (searchStepTicks * roots.length)), workingCopyManager);

    monitor.done();
  }
}