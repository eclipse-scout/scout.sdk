/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.s2e.IOperation;
import org.eclipse.scout.sdk.s2e.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link ImportsCreateOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 27.03.2014
 */
public class ImportsCreateOperation implements IOperation {

  private final Set<String> m_importsToCreate;
  private final ICompilationUnit m_icu;

  public ImportsCreateOperation(ICompilationUnit icu) {
    m_importsToCreate = new TreeSet<>();
    m_icu = icu;
  }

  public ImportsCreateOperation(ICompilationUnit icu, Set<String> importsToCreate) {
    this(icu);
    if (importsToCreate != null) {
      setImportsToCreate(importsToCreate);
    }
  }

  public ImportsCreateOperation(ICompilationUnit icu, IImportValidator validator) {
    this(icu);
    if (validator != null) {
      setImportsToCreate(validator.getImportsToCreate());
    }
  }

  @Override
  public String getOperationName() {
    return "Create imports";
  }

  @Override
  public void validate() {
    if (!JdtUtils.exists(getCompilationUnit())) {
      throw new IllegalArgumentException("Compilation unit must exist to create imports!");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (String s : m_importsToCreate) {
      getCompilationUnit().createImport(s, null, monitor);
    }
  }

  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }

  public boolean addImportToCreate(String imp) {
    return m_importsToCreate.add(imp);
  }

  public boolean removeImportToCreate(String imp) {
    return m_importsToCreate.remove(imp);
  }

  public void setImportsToCreate(Set<String> imports) {
    m_importsToCreate.clear();
    m_importsToCreate.addAll(imports);
  }
}
