/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

@SuppressWarnings("restriction")
public class OverrideUnimplementedMethodsOperation implements IOperation {

  private IType m_type;

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(m_type)) {
      throw new IllegalArgumentException("type must exsist");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    RefactoringASTParser parser = new RefactoringASTParser(AST.JLS4);
    CompilationUnit cu = parser.parse(m_type.getCompilationUnit(), true);
    ITypeBinding typeBinding = ASTNodes.getTypeBinding(cu, m_type);
    AddUnimplementedMethodsOperation op = new AddUnimplementedMethodsOperation(cu, typeBinding, null, -1, true, true, true);
    m_type.getJavaProject().getProject().getWorkspace().run(op, new NullProgressMonitor());
  }

  @Override
  public String getOperationName() {
    return OverrideUnimplementedMethodsOperation.class.getName();
  }

  public IType getType() {
    return m_type;
  }

  public void setType(IType type) {
    m_type = type;
  }
}
