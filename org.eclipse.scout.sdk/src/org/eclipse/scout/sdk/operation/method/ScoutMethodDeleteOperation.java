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
package org.eclipse.scout.sdk.operation.method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.OrganizeImportOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>BCMethodDeleteOperation</h3> ...
 */
public class ScoutMethodDeleteOperation implements IOperation {

  private final IMethod m_method;

  public ScoutMethodDeleteOperation(IMethod method) {
    m_method = method;
  }

  public String getOperationName() {
    return Texts.get("Process_deleteX", getMethod().getElementName());
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getMethod() == null) {
      throw new IllegalArgumentException("method is null");
    }
    if (!TypeUtility.exists(getMethod())) {
      throw new IllegalArgumentException("type to implement the method does not exist.");
    }
    if (getMethod().isReadOnly()) {
      throw new IllegalArgumentException("read only method can not be deleted.");
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ICompilationUnit compilationUnit = getMethod().getCompilationUnit();
    workingCopyManager.register(compilationUnit, monitor);
    getMethod().delete(true, monitor);
    OrganizeImportOperation op = new OrganizeImportOperation(compilationUnit);
    TuningUtility.startTimer();
    op.run(monitor, workingCopyManager);
    TuningUtility.stopTimer("wellform '" + compilationUnit.getElementName() + "'");
  }

  public IMethod getMethod() {
    return m_method;
  }
}
