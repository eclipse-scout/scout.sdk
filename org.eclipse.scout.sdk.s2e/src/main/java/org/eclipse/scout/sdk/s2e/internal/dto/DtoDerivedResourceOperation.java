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

import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.s2e.trigger.AbstractDerivedResourceOperation;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;

/**
 *
 */
public class DtoDerivedResourceOperation extends AbstractDerivedResourceOperation {

  public DtoDerivedResourceOperation(org.eclipse.jdt.core.IType jdtType, IJavaEnvironmentProvider envProvider) throws CoreException {
    super(jdtType, envProvider);
  }

  @Override
  public String getOperationName() {
    return "Update DTO for '" + getModelType().getName() + "'.";
  }

  @Override
  protected void runImpl(IProgressMonitor monitor) throws CoreException {
    CompilationUnitWriteOperation op = DtoS2eUtils.newDtoOp(getJdtType(), getModelType(), getJavaEnvironmentProvider(), monitor);
    if (op == null) {
      return;
    }

    JdtUtils.writeTypes(Collections.singletonList(op), monitor);
  }

}
