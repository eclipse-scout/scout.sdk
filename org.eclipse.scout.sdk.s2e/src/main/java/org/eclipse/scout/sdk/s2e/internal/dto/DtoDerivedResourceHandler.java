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
import org.eclipse.scout.sdk.core.model.api.MissingTypeException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.trigger.AbstractDerivedResourceSingleHandler;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;

/**
 *
 */
public class DtoDerivedResourceHandler extends AbstractDerivedResourceSingleHandler {

  public DtoDerivedResourceHandler(org.eclipse.jdt.core.IType jdtType, IJavaEnvironmentProvider envProvider) {
    super(jdtType, envProvider);
  }

  @Override
  public String getName() {
    return "Update DTO for '" + getModelFullyQualifiedName() + "'.";
  }

  @Override
  protected void runImpl(IProgressMonitor monitor) throws CoreException {
    monitor.beginTask(getName(), 1);
    try {
      CompilationUnitWriteOperation op = DtoS2eUtils.newDtoOp(getJdtType(), getModelType(), getJavaEnvironmentProvider(), monitor);
      if (op == null) {
        return;
      }

      S2eUtils.writeTypes(Collections.singletonList(op), monitor, false);
    }
    catch (MissingTypeException e) {
      SdkLog.info("Unable to update DTO for '" + getModelFullyQualifiedName() + "' because there are compile errors in the compilation unit.", e);
    }
    finally {
      monitor.done();
    }
  }
}
