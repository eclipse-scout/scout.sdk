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
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.TypeFilters;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;

public final class DtoS2eUtils {

  private DtoS2eUtils() {
  }

  public static CompilationUnitWriteOperation newDtoOp(org.eclipse.jdt.core.IType jdtType, IType modelType, IJavaEnvironmentProvider envProvider, IProgressMonitor monitor) throws CoreException {
    //FormData
    FormDataAnnotation a1 = findDataAnnotationForFormData(modelType);
    if (a1 != null) {
      if (modelType.equals(a1.getFormDataType())) {
        S2ESdkActivator.logError("FormData annotation points to itself. DTO generation not possible.", new Exception("Wrong @FormData annotation value."));
        return null;
      }
      org.eclipse.jdt.core.IType derivedJdtType = jdtType.getJavaProject().findType(a1.getFormDataType().getName());
      IJavaEnvironment sharedEnv = envProvider.get(derivedJdtType.getJavaProject());
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(modelType, a1, sharedEnv);

      IJavaProject p = jdtType.getJavaProject();
      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, JdtUtils.lineSeparator(p), JdtUtils.propertyMap(p));
      return new CompilationUnitWriteOperation(jdtType, newSource);
    }

    //PageData
    DataAnnotation a2 = findDataAnnotationForPageData(modelType);
    if (a2 != null) {
      if (modelType.equals(a2.getDataType())) {
        S2ESdkActivator.logError("Data annotation points to itself. DTO generation not possible.", new Exception("Wrong @Data annotation value"));
        return null;
      }
      org.eclipse.jdt.core.IType derivedJdtType = jdtType.getJavaProject().findType(a2.getDataType().getName());
      IJavaEnvironment sharedEnv = envProvider.get(derivedJdtType.getJavaProject());
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createPageDataBuilder(modelType, a2, sharedEnv);

      IJavaProject p = jdtType.getJavaProject();
      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, JdtUtils.lineSeparator(p), JdtUtils.propertyMap(p));
      return new CompilationUnitWriteOperation(jdtType, newSource);
    }

    //RowData
    DataAnnotation a3 = findDataAnnotationForRowData(modelType);
    if (a3 != null) {
      if (modelType.equals(a3.getDataType())) {
        S2ESdkActivator.logError("Data annotation points to itself. DTO generation not possible.", new Exception("Wrong @FormData annotation value."));
        return null;
      }
      org.eclipse.jdt.core.IType derivedJdtType = jdtType.getJavaProject().findType(a3.getDataType().getName());
      IJavaEnvironment sharedEnv = envProvider.get(derivedJdtType.getJavaProject());
      ICompilationUnitSourceBuilder cuSrc = DtoUtils.createTableRowDataBuilder(modelType, a3, sharedEnv);

      IJavaProject p = jdtType.getJavaProject();
      String newSource = DtoUtils.createJavaCode(cuSrc, sharedEnv, JdtUtils.lineSeparator(p), JdtUtils.propertyMap(p));
      return new CompilationUnitWriteOperation(jdtType, newSource);
    }

    return null;
  }

  private static FormDataAnnotation findDataAnnotationForFormData(IType modelType) {
    FormDataAnnotation formDataAnnotation = DtoUtils.findFormDataAnnotation(modelType);
    if (FormDataAnnotation.isCreate(formDataAnnotation) && formDataAnnotation.getFormDataType() != null) {
      return formDataAnnotation;
    }
    return null;
  }

  private static DataAnnotation findDataAnnotationForPageData(IType model) {
    if (CoreUtils.isInstanceOf(model, IRuntimeClasses.IPageWithTable)) {
      return DtoUtils.findDataAnnotation(model);
    }
    return null;
  }

  private static DataAnnotation findDataAnnotationForRowData(IType model) {
    // direct column or table extension
    if (CoreUtils.isInstanceOf(model, IRuntimeClasses.IColumn) || CoreUtils.isInstanceOf(model, IRuntimeClasses.ITableExtension)) {
      return DtoUtils.findDataAnnotation(model);
    }
    // check for table extension in IPageWithTableExtension
    if (CoreUtils.isInstanceOf(model, IRuntimeClasses.IPageWithTableExtension)) {
      IType innerTableExtension = CoreUtils.getInnerType(model, TypeFilters.subtypeOf(IRuntimeClasses.ITableExtension));
      if (innerTableExtension != null) {
        return DtoUtils.findDataAnnotation(model);
      }
    }
    return null;
  }

}
