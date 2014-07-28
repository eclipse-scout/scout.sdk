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
package org.eclipse.scout.sdk.operation.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class CompilationUnitSaveOperation implements IOperation {

  private final ICompilationUnit m_compilationUnit;

  public CompilationUnitSaveOperation(ICompilationUnit icu) {
    m_compilationUnit = icu;
  }

  @Override
  public String getOperationName() {
    return "save '" + m_compilationUnit.getElementName() + "'...";
  }

  @Override
  public void validate() {
    if (!TypeUtility.exists(getCompilationUnit())) {
      throw new IllegalArgumentException("Compilation unit must exist and connot be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getCompilationUnit().isWorkingCopy()) {
      getCompilationUnit().getBuffer().save(monitor, true);
    }
  }

  public ICompilationUnit getCompilationUnit() {
    return m_compilationUnit;
  }

}
