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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.jdt.SourceRange;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 *
 */
public class JavaElementFormatOperation extends SourceFormatOperation {

  private final IMember m_javaMember;
  private final boolean m_organizeImports;

  /**
   * @param project
   * @param document
   * @param range
   * @throws JavaModelException
   */
  public JavaElementFormatOperation(IMember element, boolean organizeImports) throws JavaModelException {
    super(element.getJavaProject());
    m_organizeImports = organizeImports;
    m_javaMember = element;
    setDocument(new Document(m_javaMember.getCompilationUnit().getSource()));
    SourceRange range = new SourceRange(element.getSourceRange().getOffset(), element.getSourceRange().getLength());
    setRange(range);
  }

  @Override
  public String getOperationName() {
    return "Format source of '" + getJavaMember().getElementName() + "'...";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    if (monitor.isCanceled()) {
      return;
    }
    super.run(monitor, workingCopyManager);
    // set source back to icu
    ICompilationUnit icu = getJavaMember().getCompilationUnit();
    workingCopyManager.register(icu, monitor);
    icu.getBuffer().setContents(getDocument().get());
    if (isOrganizeImports()) {
      // organize imports
      OrganizeImportOperation op = new OrganizeImportOperation(icu);
      op.validate();
      op.run(monitor, workingCopyManager);
    }
  }

  public IMember getJavaMember() {
    return m_javaMember;
  }

  public boolean isOrganizeImports() {
    return m_organizeImports;
  }
}
