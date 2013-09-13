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
package org.eclipse.scout.sdk.internal.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitUpdateOperation;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.AbstractDtoAutoUpdateOperation;

/**
 * <h3>{@link FormDataDtoUpdateOperation}</h3>
 * 
 * @author aho
 * @since 3.10.0 16.08.2013
 */
public class FormDataDtoUpdateOperation extends AbstractDtoAutoUpdateOperation {

  private final FormDataAnnotation m_formDataAnnotation;

  /**
   * @param modelType
   */
  public FormDataDtoUpdateOperation(IType modelType, FormDataAnnotation formDataAnnotation) {
    super(modelType);
    m_formDataAnnotation = formDataAnnotation;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getFormDataAnnotation() == null) {
      throw new IllegalArgumentException("FormDataAnnotation can not be null.");
    }
    super.validate();
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }

  @Override
  protected String getDerivedTypeSignature() throws CoreException {
    FormDataAnnotation formDataAnnotation = getFormDataAnnotation();
    if (formDataAnnotation != null) {
      return formDataAnnotation.getFormDataTypeSignature();
    }
    return null;
  }

  @Override
  protected String createDerivedTypeSource(IProgressMonitor monitor) throws CoreException {
    FormDataAnnotation formDataAnnotation = getFormDataAnnotation();

    ITypeSourceBuilder formDataNewOp = FormDataUtility.createFormDataSourceBuilder(getModelType(), formDataAnnotation);

    IType formDataType = ensureDerivedType();
    if (!TypeUtility.exists(formDataType)) {
      return null;
    }
    ICompilationUnit formDataIcu = formDataType.getCompilationUnit();

    CompilationUnitUpdateOperation icuUpdateOp = new CompilationUnitUpdateOperation(formDataIcu);
    icuUpdateOp.addTypeSourceBuilder(formDataNewOp);

    SimpleImportValidator validator = new SimpleImportValidator(TypeUtility.getPackage(formDataIcu).getElementName());
    StringBuilder sourceBuilder = new StringBuilder();
    icuUpdateOp.getSourceBuilder().createSource(sourceBuilder, ResourceUtility.getLineSeparator(formDataIcu), formDataIcu.getJavaProject(), validator);
    String source = sourceBuilder.toString();
    return source;
  }

}
