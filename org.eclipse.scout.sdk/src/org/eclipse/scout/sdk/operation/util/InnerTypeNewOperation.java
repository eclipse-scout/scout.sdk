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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>InnerTypeNewOperation</h3> To generate a new inner type of the declaring type.
 */
public class InnerTypeNewOperation extends AbstractScoutTypeNewOperation {
  private final IType m_declaringType;
  private boolean m_formatSource;
  private IType m_createdType;
  private IJavaElement m_sibling;

  public InnerTypeNewOperation(String name, IType declaringType) {
    this(name, declaringType, false);
  }

  public InnerTypeNewOperation(String name, IType declaringType, boolean formatSource) {
    super(name);
    m_declaringType = declaringType;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "New Inner Type '" + getTypeName() + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (getDeclaringtype() == null) {
      throw new IllegalArgumentException("declaring type missing!");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    workingCopyManager.register(getDeclaringtype().getCompilationUnit(), monitor);
    CompilationUnitImportValidator validator = new CompilationUnitImportValidator(getDeclaringtype().getCompilationUnit());
    m_createdType = createImplementation(validator, monitor);
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedType(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  protected IType createImplementation(IImportValidator validator, IProgressMonitor p) throws CoreException {
    ICompilationUnit icu = m_declaringType.getCompilationUnit();
    String content = createSource(validator);
    String javaLangRegex = "^java\\.lang\\.[^.]*$";

    for (String imp : validator.getImportsToCreate()) {
      if (!imp.matches(javaLangRegex)) {
        icu.createImport(imp, null, p);
      }
    }
    IType t = m_declaringType.createType(content, getSibling(), true, p);

    return t;
  }

  public IType getDeclaringtype() {
    return m_declaringType;
  }

  @Override
  public IType getCreatedType() {
    return m_createdType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  /**
   * the type where the new created type will be inserted after in the java file.
   * 
   * @param sibling
   */
  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

}
