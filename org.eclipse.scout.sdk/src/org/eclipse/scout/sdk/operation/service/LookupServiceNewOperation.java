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
package org.eclipse.scout.sdk.operation.service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class LookupServiceNewOperation extends ServiceNewOperation {

  final IType iService = TypeUtility.getType(RuntimeClasses.IService);
  final IType abstractSqlLookupService = TypeUtility.getType(RuntimeClasses.AbstractSqlLookupService);

  @Override
  public String getOperationName() {
    return "New Lookup service";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IType serviceImplementation = getCreatedServiceImplementation();
    ITypeHierarchy superTypeHierarchy = serviceImplementation.newSupertypeHierarchy(monitor);

    if (superTypeHierarchy.contains(abstractSqlLookupService)) {
      StringBuilder methodBuilder = new StringBuilder();
      methodBuilder.append("@Override\n");
      methodBuilder.append("protected String getConfiguredSqlSelect(){\n");
      methodBuilder.append(SdkProperties.TAB + "return \"\"; " + ScoutUtility.getCommentBlock("write select statement here.") + "\n}");
      serviceImplementation.createMethod(methodBuilder.toString(), null, true, monitor);
    }
    else {
      IImportValidator validator = new CompilationUnitImportValidator(serviceImplementation.getCompilationUnit());
      String lookupRowRef = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.LookupRow));
      String lookupCallRef = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.LookupCall));
      String processingExceptionRef = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));

      StringBuilder methodSource = new StringBuilder();
      methodSource.append("@Override\n");
      methodSource.append("public ");
      methodSource.append(lookupRowRef + "[] ");
      methodSource.append("getDataByAll(");
      methodSource.append(lookupCallRef + " call) ");
      methodSource.append("throws " + processingExceptionRef + " {\n");
      methodSource.append(SdkProperties.TAB + ScoutUtility.getCommentBlock("Auto-generated method stub\n"));
      methodSource.append(SdkProperties.TAB + "return null;\n");
      methodSource.append("}\n");
      serviceImplementation.createMethod(methodSource.toString(), null, true, monitor);

      methodSource = new StringBuilder();
      methodSource.append("@Override\n");
      methodSource.append("public " + lookupRowRef + "[] getDataByKey(" + lookupCallRef + " call) throws " + processingExceptionRef + "{\n");
      methodSource.append(SdkProperties.TAB + ScoutUtility.getCommentBlock("Auto-generated method stub\n"));
      methodSource.append(SdkProperties.TAB + "return null;\n");
      methodSource.append("}\n");
      serviceImplementation.createMethod(methodSource.toString(), null, true, monitor);

      methodSource = new StringBuilder();
      methodSource.append("@Override\n");
      methodSource.append("public " + lookupRowRef + "[] getDataByRec(" + lookupCallRef + " call) throws " + processingExceptionRef + "{\n");
      methodSource.append(SdkProperties.TAB + ScoutUtility.getCommentBlock("Auto-generated method stub\n"));
      methodSource.append(SdkProperties.TAB + "return null;\n");
      methodSource.append("}\n");
      serviceImplementation.createMethod(methodSource.toString(), null, true, monitor);

      methodSource = new StringBuilder();
      methodSource.append("@Override\n");
      methodSource.append("public " + lookupRowRef + "[] getDataByText(" + lookupCallRef + " call) throws " + processingExceptionRef + "{\n");
      methodSource.append(SdkProperties.TAB + ScoutUtility.getCommentBlock("Auto-generated method stub\n"));
      methodSource.append(SdkProperties.TAB + "return null;\n");
      methodSource.append("}\n");
      serviceImplementation.createMethod(methodSource.toString(), null, true, monitor);

      for (String imp : validator.getImportsToCreate()) {
        serviceImplementation.getCompilationUnit().createImport(imp, null, monitor);
      }
    }
  }
}
