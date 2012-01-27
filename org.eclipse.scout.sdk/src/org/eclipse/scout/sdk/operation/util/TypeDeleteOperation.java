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
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.IDeleteOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>ScoutTypeDeleteOperation</h3> Deleted a scout type. Might be used for primary and inner types.
 */
public class TypeDeleteOperation implements IDeleteOperation {

  private IType m_type;

  public TypeDeleteOperation(IType type) {
    m_type = type;
  }

  @Override
  public String getOperationName() {
    return "Delete " + m_type.getElementName();
  }

  public IType getType() {
    return m_type;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getType() == null) {
      throw new IllegalArgumentException("type is null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ICompilationUnit icu = getType().getCompilationUnit();
    if (getType().getDeclaringType() != null) {
      workingCopyManager.register(icu, monitor);
      // handle inner type
      // delete import in compilation unit
      String fullyQuallifiedImport = getType().getFullyQualifiedName();
      IImportDeclaration importDec = getType().getCompilationUnit().getImport(fullyQuallifiedImport);
      if (importDec != null && importDec.exists()) {
        importDec.delete(true, monitor);
      }
      getType().delete(true, monitor);
    }
    else {
      workingCopyManager.unregister(icu, monitor);
      icu.delete(true, monitor);
    }
  }
}
