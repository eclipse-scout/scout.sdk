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

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class CalendarServiceNewOperation extends ServiceNewOperation {

  final IType iService = TypeUtility.getType(RuntimeClasses.IService);
  final IType abstractSqlLookupService = TypeUtility.getType(RuntimeClasses.AbstractSqlLookupService);

  @Override
  public String getOperationName() {
    return "new Lookup service";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IType serviceInterface = getCreatedServiceInterface();
    if (TypeUtility.exists(serviceInterface)) {
      CompilationUnitImportValidator importValidator = new CompilationUnitImportValidator(serviceInterface.getCompilationUnit());
      // getItems
      StringBuilder methodBuilder = new StringBuilder();
      String calendarItemSimpleName = importValidator.getTypeName(Signature.createTypeSignature(RuntimeClasses.ICalendarItem, true));
      methodBuilder.append(calendarItemSimpleName);
      methodBuilder.append("[] getItems(");
      methodBuilder.append(importValidator.getTypeName(Signature.createTypeSignature(Date.class.getName(), true)));
      methodBuilder.append(" minDate, ");
      methodBuilder.append(importValidator.getTypeName(Signature.createTypeSignature(Date.class.getName(), true)));
      methodBuilder.append(" maxDate) throws ");
      String processingExceptionSimpleName = importValidator.getTypeName(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));
      methodBuilder.append(processingExceptionSimpleName);
      methodBuilder.append(";");
      serviceInterface.createMethod(methodBuilder.toString(), null, true, monitor);
      // storeItems
      methodBuilder = new StringBuilder();
      methodBuilder.append("void storeItems(").append(calendarItemSimpleName).append("[] items, boolean delta) throws ").append(processingExceptionSimpleName).append(";");
      serviceInterface.createMethod(methodBuilder.toString(), null, true, monitor);
      // create imports
      importValidator.createImports(monitor);
      // format
      new JavaElementFormatOperation(serviceInterface, true).run(monitor, workingCopyManager);
    }
    IType serviceImplementation = getCreatedServiceImplementation();
    if (TypeUtility.exists(serviceImplementation)) {
      CompilationUnitImportValidator importValidator = new CompilationUnitImportValidator(serviceImplementation.getCompilationUnit());
      StringBuilder methodBuilder = new StringBuilder();
      String overrideSimpleName = importValidator.getTypeName(Signature.createTypeSignature(Override.class.getName(), true));
      methodBuilder.append("@").append(overrideSimpleName).append("\n");
      methodBuilder.append("public ");
      String calendarItemSimpleName = importValidator.getTypeName(Signature.createTypeSignature(RuntimeClasses.ICalendarItem, true));
      methodBuilder.append(calendarItemSimpleName);
      methodBuilder.append("[] getItems(");
      methodBuilder.append(importValidator.getTypeName(Signature.createTypeSignature(Date.class.getName(), true)));
      methodBuilder.append(" minDate, ");
      methodBuilder.append(importValidator.getTypeName(Signature.createTypeSignature(Date.class.getName(), true)));
      methodBuilder.append(" maxDate) throws ");
      String processingExceptionSimpleName = importValidator.getTypeName(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));
      methodBuilder.append(processingExceptionSimpleName);
      methodBuilder.append(" {\n");
      methodBuilder.append(ScoutUtility.getCommentBlock("business logic here.")).append("\n");
      methodBuilder.append("return new ").append(calendarItemSimpleName).append("[0];\n}\n");
      serviceImplementation.createMethod(methodBuilder.toString(), null, true, monitor);
      // storeItems
      methodBuilder = new StringBuilder();
      methodBuilder.append("@").append(overrideSimpleName).append("\n");
      methodBuilder.append("public void storeItems(").append(calendarItemSimpleName).append("[] items, boolean delta) throws ").append(processingExceptionSimpleName).append("{\n");
      methodBuilder.append(ScoutUtility.getCommentBlock("business logic here.")).append("\n");
      methodBuilder.append("}\n");
      serviceImplementation.createMethod(methodBuilder.toString(), null, true, monitor);
      // create imports
      importValidator.createImports(monitor);
      // format
      new JavaElementFormatOperation(serviceImplementation, true).run(monitor, workingCopyManager);
    }

  }

}
