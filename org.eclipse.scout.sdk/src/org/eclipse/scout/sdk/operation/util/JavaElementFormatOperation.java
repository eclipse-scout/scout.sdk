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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.util.jdt.SourceRange;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class JavaElementFormatOperation extends SourceFormatOperation {

  private final ICompilationUnit m_icu;
  private final boolean m_organizeImports;

  /**
   * @param project
   * @param document
   * @param range
   * @throws JavaModelException
   */
  public JavaElementFormatOperation(IMember element, boolean organizeImports) throws JavaModelException {
    this(element.getCompilationUnit(), organizeImports);
  }

  public JavaElementFormatOperation(ICompilationUnit icu, boolean organizeImports) throws JavaModelException {
    super(icu.getJavaProject());
    m_organizeImports = organizeImports;
    m_icu = icu;
    setDocument(new Document(icu.getSource()));

    ISourceRange sourceRange = icu.getSourceRange();
    setRange(new SourceRange(sourceRange.getOffset(), sourceRange.getLength()));
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (monitor.isCanceled()) {
      return;
    }
    super.run(monitor, workingCopyManager);
    // set source back to icu
    workingCopyManager.register(m_icu, monitor);
    m_icu.getBuffer().setContents(getDocument().get());
    if (isOrganizeImports()) {
      // organize imports
      OrganizeImportOperation op = new OrganizeImportOperation(m_icu);
      op.validate();
      op.run(monitor, workingCopyManager);
    }
  }

  public boolean isOrganizeImports() {
    return m_organizeImports;
  }
}
