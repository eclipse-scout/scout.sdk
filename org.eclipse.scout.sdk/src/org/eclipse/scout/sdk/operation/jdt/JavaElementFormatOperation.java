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
package org.eclipse.scout.sdk.operation.jdt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.util.OrganizeImportOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class JavaElementFormatOperation extends SourceFormatOperation {

  private final ISourceReference m_sourceReference;
  private final ICompilationUnit m_compilationUnit;
  private final boolean m_organizeImports;

  public JavaElementFormatOperation(ICompilationUnit element, boolean organizeImports) throws JavaModelException {
    this(element, element, organizeImports);
  }

  public JavaElementFormatOperation(IMember element, boolean organizeImports) throws JavaModelException {
    this(element, element.getCompilationUnit(), organizeImports);
  }

  private JavaElementFormatOperation(ISourceReference sourceReference, ICompilationUnit icu, boolean organizeImports) throws JavaModelException {
    super(icu.getJavaProject());
    m_sourceReference = sourceReference;
    m_compilationUnit = icu;
    m_organizeImports = organizeImports;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("java project can not be null.");
    }
    if (!TypeUtility.exists(getCompilationUnit())) {
      throw new IllegalArgumentException("compilation unit can not be null.");
    }
    if (getSourceReference() == null) {
      throw new IllegalArgumentException("source reference can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (monitor.isCanceled()) {
      return;
    }
    ICompilationUnit icu = getCompilationUnit();
    if (!workingCopyManager.register(icu, monitor)) {
      workingCopyManager.reconcile(icu, monitor);
    }
    // range calculation
    setDocument(new Document(icu.getSource()));
    try {
      setRange(getSourceReference().getSourceRange());
    }
    catch (Exception e) {
      ScoutSdk.logWarning(e);
    }

    super.run(monitor, workingCopyManager);
    // set source back to icu
    icu.getBuffer().setContents(getDocument().get());
    if (isOrganizeImports()) {
      // organize imports
      OrganizeImportOperation op = new OrganizeImportOperation(icu);
      op.validate();
      op.run(monitor, workingCopyManager);
    }
  }

  public ICompilationUnit getCompilationUnit() {
    return m_compilationUnit;
  }

  public ISourceReference getSourceReference() {
    return m_sourceReference;
  }

  public boolean isOrganizeImports() {
    return m_organizeImports;
  }

}
