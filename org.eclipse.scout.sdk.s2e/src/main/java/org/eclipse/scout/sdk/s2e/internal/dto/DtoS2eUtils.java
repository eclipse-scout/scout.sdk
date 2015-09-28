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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;

public final class DtoS2eUtils {

  private DtoS2eUtils() {
  }

  public static CompilationUnitWriteOperation newDtoOp(org.eclipse.jdt.core.IType jdtType, IType modelType, IJavaEnvironmentProvider envProvider, IProgressMonitor monitor) throws CoreException {
    //FormData
    FormDataAnnotationDescriptor a1 = findDataAnnotationForFormData(modelType);
    if (a1 != null) {
      if (modelType.equals(a1.getFormDataType())) {
        SdkLog.error("FormData annotation points to itself. DTO generation not possible.", new Exception("Wrong @FormData annotation value."));
        return null;
      }
      org.eclipse.jdt.core.IType derivedJdtType = jdtType.getJavaProject().findType(a1.getFormDataType().name());
      IJavaEnvironment sharedEnv = envProvider.get(derivedJdtType.getJavaProject());
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(modelType, a1, sharedEnv);

      IJavaProject p = derivedJdtType.getJavaProject();
      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, JdtUtils.lineSeparator(p), JdtUtils.propertyMap(p));
      return new CompilationUnitWriteOperation(derivedJdtType, newSource);
    }

    //PageData
    DataAnnotationDescriptor a2 = findDataAnnotationForPageData(modelType);
    if (a2 != null) {
      if (modelType.equals(a2.getDataType())) {
        SdkLog.error("Data annotation points to itself. DTO generation not possible.", new Exception("Wrong @Data annotation value"));
        return null;
      }
      org.eclipse.jdt.core.IType derivedJdtType = jdtType.getJavaProject().findType(a2.getDataType().name());
      IJavaEnvironment sharedEnv = envProvider.get(derivedJdtType.getJavaProject());
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createPageDataBuilder(modelType, a2, sharedEnv);

      IJavaProject p = derivedJdtType.getJavaProject();
      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, JdtUtils.lineSeparator(p), JdtUtils.propertyMap(p));
      return new CompilationUnitWriteOperation(derivedJdtType, newSource);
    }

    //RowData
    DataAnnotationDescriptor a3 = findDataAnnotationForRowData(modelType);
    if (a3 != null) {
      if (modelType.equals(a3.getDataType())) {
        SdkLog.error("Data annotation points to itself. DTO generation not possible.", new Exception("Wrong @FormData annotation value."));
        return null;
      }
      org.eclipse.jdt.core.IType derivedJdtType = jdtType.getJavaProject().findType(a3.getDataType().name());
      IJavaEnvironment sharedEnv = envProvider.get(derivedJdtType.getJavaProject());
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createTableRowDataBuilder(modelType, a3, sharedEnv);

      IJavaProject p = derivedJdtType.getJavaProject();
      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, JdtUtils.lineSeparator(p), JdtUtils.propertyMap(p));
      return new CompilationUnitWriteOperation(derivedJdtType, newSource);
    }

    return null;
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
