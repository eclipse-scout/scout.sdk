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
package org.eclipse.scout.sdk.ui.services;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.scout.sdk.operation.util.IOrganizeImportService;

/**
 * <h3>{@link OrganizeImportService}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 23.05.2013
 */
@SuppressWarnings("restriction")
public class OrganizeImportService implements IOrganizeImportService {

  @Override
  public void organize(ICompilationUnit cu, IProgressMonitor monitor) throws CoreException {
    final ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setResolveBindings(true);
    parser.setStatementsRecovery(true);
    parser.setBindingsRecovery(true);
    parser.setSource(cu);
    CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

    CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(cu.getJavaProject());
    OrganizeImportsOperation organizeImps = new OrganizeImportsOperation(cu, astRoot, settings.importIgnoreLowercase, !cu.isWorkingCopy(), true, null);
    organizeImps.run(monitor);
  }

}
