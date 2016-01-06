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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.trigger.AbstractDerivedResourceBatchHandler;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;

/**
 *
 */
public class DtoDerivedResourceBatchHandler extends AbstractDerivedResourceBatchHandler {

  public DtoDerivedResourceBatchHandler(Collection<org.eclipse.jdt.core.IType> jdtTypes, IJavaEnvironmentProvider envProvider) {
    super(jdtTypes, envProvider);
  }

  @Override
  public String getName() {
    return "Update all DTOs";
  }

  @Override
  protected void runImpl(IProgressMonitor monitor) throws CoreException {
    IJavaEnvironmentProvider envProvider = getJavaEnvironmentProvider();

    monitor.beginTask(getName(), getJdtTypes().size());
    try {
      for (org.eclipse.jdt.core.IType jdtType : getJdtTypes()) {
        if (monitor.isCanceled()) {
          return;
        }
        try {
          monitor.subTask("process " + jdtType.getFullyQualifiedName());
          IType modelType = envProvider.jdtTypeToScoutType(jdtType);
          CompilationUnitWriteOperation op = DtoS2eUtils.newDtoOp(jdtType, modelType, getJavaEnvironmentProvider());
          if (op != null) {
            S2eUtils.writeTypes(Collections.singletonList(op), monitor, false);
          }
        }
        catch (Exception t) {
          SdkLog.error("{}: {}", getClass().getSimpleName(), jdtType.getFullyQualifiedName(), t);
        }
        monitor.worked(1);
      }
    }
    finally {
      monitor.done();
    }
  }
}
