/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.internal.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;

public final class DtoS2eUtils {

  private DtoS2eUtils() {
  }

  public static CompilationUnitWriteOperation newDtoOp(org.eclipse.jdt.core.IType jdtType, IType modelType, IJavaEnvironmentProvider envProvider) throws CoreException {

    //FormData
    FormDataAnnotationDescriptor a1 = findDataAnnotationForFormData(modelType);
    if (a1 != null) {
      org.eclipse.jdt.core.IType derivedJdtType = getDerivedJdtType(modelType, a1.getFormDataType(), jdtType);
      if (derivedJdtType == null) {
        return null;
      }
      IJavaProject derivedProject = derivedJdtType.getJavaProject();
      IJavaEnvironment sharedEnv = envProvider.get(derivedProject);
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(modelType, a1, sharedEnv);

      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, derivedJdtType.getCompilationUnit().findRecommendedLineSeparator(), S2eUtils.propertyMap(derivedProject));
      return new CompilationUnitWriteOperation(derivedJdtType, newSource);
    }

    //PageData
    DataAnnotationDescriptor a2 = findDataAnnotationForPageData(modelType);
    if (a2 != null) {
      org.eclipse.jdt.core.IType derivedJdtType = getDerivedJdtType(modelType, a2.getDataType(), jdtType);
      if (derivedJdtType == null) {
        return null;
      }
      IJavaProject derivedProject = derivedJdtType.getJavaProject();

      IJavaEnvironment sharedEnv = envProvider.get(derivedProject);
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createPageDataBuilder(modelType, a2, sharedEnv);

      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, derivedJdtType.getCompilationUnit().findRecommendedLineSeparator(), S2eUtils.propertyMap(derivedProject));
      return new CompilationUnitWriteOperation(derivedJdtType, newSource);
    }

    //RowData
    DataAnnotationDescriptor a3 = findDataAnnotationForRowData(modelType);
    if (a3 != null) {
      org.eclipse.jdt.core.IType derivedJdtType = getDerivedJdtType(modelType, a3.getDataType(), jdtType);
      if (derivedJdtType == null) {
        return null;
      }
      IJavaProject derivedProject = derivedJdtType.getJavaProject();

      IJavaEnvironment sharedEnv = envProvider.get(derivedJdtType.getJavaProject());
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createTableRowDataBuilder(modelType, a3, sharedEnv);

      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, derivedJdtType.getCompilationUnit().findRecommendedLineSeparator(), S2eUtils.propertyMap(derivedProject));
      return new CompilationUnitWriteOperation(derivedJdtType, newSource);
    }

    return null;
  }

  private static org.eclipse.jdt.core.IType getDerivedJdtType(IType modelType, IType derivedType, org.eclipse.jdt.core.IType modelJdtType) throws JavaModelException {
    String message = "Wrong derived resource annotation value.";
    if (modelType.equals(derivedType)) {
      SdkLog.error("Model type declares itself as derived target. DTO generation not possible.", new Exception(message));
      return null;
    }
    org.eclipse.jdt.core.IType derivedJdtType = modelJdtType.getJavaProject().findType(derivedType.name().replace('$', '.'));
    if (derivedJdtType == null) {
      SdkLog.error("Derived resource type '" + derivedType.name() + "' not found.", new Exception(message));
      return null;
    }
    if (derivedJdtType.isBinary()) {
      SdkLog.error("Derived resource type '" + derivedJdtType.getFullyQualifiedName() + "' is binary.", new Exception(message));
      return null;
    }
    return derivedJdtType;
  }

  private static FormDataAnnotationDescriptor findDataAnnotationForFormData(IType modelType) {
    FormDataAnnotationDescriptor formDataAnnotation = DtoUtils.getFormDataAnnotationDescriptor(modelType);
    if (FormDataAnnotationDescriptor.isCreate(formDataAnnotation) && formDataAnnotation.getFormDataType() != null) {
      return formDataAnnotation;
    }
    return null;
  }

  private static DataAnnotationDescriptor findDataAnnotationForPageData(IType model) {
    if (model.isInstanceOf(IScoutRuntimeTypes.IPageWithTable)) {
      return DtoUtils.getDataAnnotationDescriptor(model);
    }
    return null;
  }

  private static DataAnnotationDescriptor findDataAnnotationForRowData(IType model) {
    // direct column or table extension
    if (model.isInstanceOf(IScoutRuntimeTypes.IColumn) || model.isInstanceOf(IScoutRuntimeTypes.ITableExtension)) {
      return DtoUtils.getDataAnnotationDescriptor(model);
    }
    // check for table extension in IPageWithTableExtension
    if (model.isInstanceOf(IScoutRuntimeTypes.IPageWithTableExtension)) {
      IType innerTableExtension = model.innerTypes().withInstanceOf(IScoutRuntimeTypes.ITableExtension).first();
      if (innerTableExtension != null) {
        return DtoUtils.getDataAnnotationDescriptor(model);
      }
    }
    return null;
  }

}
