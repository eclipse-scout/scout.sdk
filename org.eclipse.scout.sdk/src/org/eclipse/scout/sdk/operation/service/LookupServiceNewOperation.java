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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class LookupServiceNewOperation extends ServiceNewOperation {

  final IType iService = TypeUtility.getType(RuntimeClasses.IService);
  final IType abstractSqlLookupService = TypeUtility.getType(RuntimeClasses.AbstractSqlLookupService);

  @Override
  public String getOperationName() {
    return "new Lookup service";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IType serviceImplementation = getCreatedServiceImplementation();
    ITypeHierarchy superTypeHierarchy = serviceImplementation.newSupertypeHierarchy(monitor);

    if (superTypeHierarchy.contains(abstractSqlLookupService)) {
      StringBuilder methodBuilder = new StringBuilder();
      methodBuilder.append("@Override\n");
      methodBuilder.append("public String getConfiguredSqlSelect(){\n");
      methodBuilder.append(SdkProperties.TAB + "return \"\"; " + ScoutUtility.getCommentBlock("write select statement here.") + "\n}");
      serviceImplementation.createMethod(methodBuilder.toString(), null, true, monitor);
    }
    else {
      IImportValidator validator = new CompilationUnitImportValidator(serviceImplementation.getCompilationUnit());
      String lookupRowRef = validator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.LookupRow, true));
      String lookupCallRef = validator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.LookupCall, true));
      String processingExceptionRef = validator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));

      StringBuffer methodSource = new StringBuffer();
      methodSource.append("public ");
      methodSource.append(lookupRowRef + "[] ");
      methodSource.append("getDataByAll(");
      methodSource.append(lookupCallRef + " call) ");
      methodSource.append("throws " + processingExceptionRef + " {\n");
      methodSource.append(SdkProperties.TAB + ScoutUtility.getCommentBlock("Auto-generated method stub\n"));
      methodSource.append(SdkProperties.TAB + "return null;\n");
      methodSource.append("}\n");
      serviceImplementation.createMethod(methodSource.toString(), null, true, monitor);

      methodSource = new StringBuffer();
      methodSource.append("public " + lookupRowRef + "[] getDataByKey(" + lookupCallRef + " call) throws " + processingExceptionRef + "{\n");
      methodSource.append(SdkProperties.TAB + ScoutUtility.getCommentBlock("Auto-generated method stub\n"));
      methodSource.append(SdkProperties.TAB + "return null;\n");
      methodSource.append("}\n");
      serviceImplementation.createMethod(methodSource.toString(), null, true, monitor);

      methodSource = new StringBuffer();
      methodSource.append("public " + lookupRowRef + "[] getDataByRec(" + lookupCallRef + " call) throws " + processingExceptionRef + "{\n");
      methodSource.append(SdkProperties.TAB + ScoutUtility.getCommentBlock("Auto-generated method stub\n"));
      methodSource.append(SdkProperties.TAB + "return null;\n");
      methodSource.append("}\n");
      serviceImplementation.createMethod(methodSource.toString(), null, true, monitor);

      methodSource = new StringBuffer();
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

  @Override
  public void setImplementationBundle(IScoutBundle implementationBundle) {
    super.setImplementationBundle(implementationBundle);
    if (implementationBundle != null) {
      setServicePackageName(implementationBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_LOOKUP));
    }
    else {
      setServicePackageName(null);
    }
  }

  @Override
  public void setInterfaceBundle(IScoutBundle interfaceBundle) {
    super.setInterfaceBundle(interfaceBundle);
    if (interfaceBundle != null) {
      setServiceInterfacePackageName(interfaceBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_LOOKUP));
    }
    else {
      setServiceInterfacePackageName(null);
    }
  }

}
