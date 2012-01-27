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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
@SuppressWarnings("restriction")
public class OrganizeImportOperation implements IOperation {

  private final ICompilationUnit m_icu;

  public OrganizeImportOperation(ICompilationUnit icu) {
    m_icu = icu;

  }

  @Override
  public String getOperationName() {
    return "Organize imports...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getCompilationUnit() == null) {
      throw new IllegalArgumentException("no compilation unit set.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    workingCopyManager.register(getCompilationUnit(), monitor);

    final ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setResolveBindings(true);
    parser.setStatementsRecovery(false);
    parser.setBindingsRecovery(false);
    parser.setSource(getCompilationUnit());
    CompilationUnit ast = (CompilationUnit) parser.createAST(null);
    OrganizeImportsOperation organizeImps = new OrganizeImportsOperation(getCompilationUnit(), ast, false, false, false, null);
    organizeImps.run(monitor);
  }

  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }
}
