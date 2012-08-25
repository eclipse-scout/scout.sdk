/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.form.field;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>ButtonFieldNewOperation</h3> ...
 */
public class RadioButtonNewOperation extends ButtonFieldNewOperation {

  private final boolean m_radoButtonFieldSourceFormat;

  public RadioButtonNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public RadioButtonNewOperation(IType declaringType, boolean sourceFormat) {
    super(declaringType, false);
    m_radoButtonFieldSourceFormat = sourceFormat;
  }

  @Override
  public String getOperationName() {
    return "New radio button...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    if (m_radoButtonFieldSourceFormat) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedButton(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

}
