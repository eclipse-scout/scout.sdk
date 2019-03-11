/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.manipulation.JavaManipulation;
import org.eclipse.jdt.core.manipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

/**
 * <h3>{@link OrganizeImportOperation}</h3>
 *
 * @since 9.0.0
 */
public class OrganizeImportOperation implements IOperation {

  private ICompilationUnit m_icu;

  public OrganizeImportOperation() {
    this(null);
  }

  public OrganizeImportOperation(ICompilationUnit icu) {
    m_icu = icu;
  }

  @Override
  public String getOperationName() {
    return "Organize imports of compilation unit";
  }

  @Override
  public void validate() {
    if (getCompilationUnit() == null) {
      throw new IllegalArgumentException("compilation unit is null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (JavaManipulation.getPreferenceNodeId() == null) {
      return; // not configured. throws IllegalArgumentException. Do not organize imports
    }
    ICompilationUnit unit = getCompilationUnit();
    CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(unit.getJavaProject());
    OrganizeImportsOperation organizeImps = new OrganizeImportsOperation(unit, null, settings.importIgnoreLowercase, !unit.isWorkingCopy(), true, null);
    organizeImps.run(monitor);
  }

  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }

  public void setCompilationUnit(ICompilationUnit icu) {
    m_icu = icu;
  }
}
