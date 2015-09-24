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
package org.eclipse.scout.sdk.s2e.internal.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.SdkConsole;
import org.eclipse.scout.sdk.s2e.trigger.AbstractDerivedResourceBatchOperation;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;

/**
 *
 */
public class DtoDerivedResourceBatchOperation extends AbstractDerivedResourceBatchOperation {

  public DtoDerivedResourceBatchOperation(Collection<org.eclipse.jdt.core.IType> jdtTypes, IJavaEnvironmentProvider envProvider) {
    super(jdtTypes, envProvider);
  }

  @Override
  public String getOperationName() {
    return "Update all DTO";
  }

  @Override
  protected void runImpl(IProgressMonitor monitor) throws CoreException {
    IJavaEnvironmentProvider envProvider = getJavaEnvironmentProvider();

    ArrayList<CompilationUnitWriteOperation> allOps = new ArrayList<>();
    for (org.eclipse.jdt.core.IType jdtType : getJdtTypes()) {
      if (monitor.isCanceled()) {
        return;
      }
      try {
        monitor.subTask(jdtType.getFullyQualifiedName());
        IType modelType = envProvider.jdtTypeToScoutType(jdtType);
        CompilationUnitWriteOperation op = DtoS2eUtils.newDtoOp(jdtType, modelType, getJavaEnvironmentProvider(), monitor);
        if (op != null) {
          allOps.add(op);
        }
      }
      catch (Throwable t) {
        SdkConsole.println("ERROR: " + getClass().getSimpleName() + ": " + jdtType.getFullyQualifiedName(), t);
      }
    }

    JdtUtils.writeTypes(allOps, monitor);
  }

}
