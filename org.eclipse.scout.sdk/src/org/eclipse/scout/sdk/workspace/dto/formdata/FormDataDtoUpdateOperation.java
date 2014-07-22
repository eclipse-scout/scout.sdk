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
package org.eclipse.scout.sdk.workspace.dto.formdata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.internal.workspace.dto.DtoUtility;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.AbstractDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link FormDataDtoUpdateOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 16.08.2013
 */
public class FormDataDtoUpdateOperation extends AbstractDtoAutoUpdateOperation {

  private final FormDataAnnotation m_formDataAnnotation;

  public FormDataDtoUpdateOperation(IType modelType) throws JavaModelException {
    this(modelType, ScoutTypeUtility.findFormDataAnnotation(modelType, TypeUtility.getSupertypeHierarchy(modelType)));
  }

  /**
   * @param modelType
   */
  public FormDataDtoUpdateOperation(IType modelType, FormDataAnnotation formDataAnnotation) {
    super(modelType);
    m_formDataAnnotation = formDataAnnotation;
  }

  @Override
  public void validate() {
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

    // collect all source builders for the whole form data.
    ITypeSourceBuilder formDataSourceBuilder = DtoUtility.createFormDataSourceBuilder(getModelType(), formDataAnnotation, monitor);
    if (monitor.isCanceled() || formDataSourceBuilder == null) {
      return null;
    }

    IType formDataType = ensureDerivedType();
    if (!TypeUtility.exists(formDataType)) {
      return null;
    }
    if (monitor.isCanceled()) {
      return null;
    }
    ICompilationUnit formDataIcu = formDataType.getCompilationUnit();

    CompilationUnitSourceBuilder cuSourceBuilder = new CompilationUnitSourceBuilder(formDataIcu.getElementName(), formDataIcu.getParent().getElementName());
    cuSourceBuilder.addTypeSourceBuilder(formDataSourceBuilder);
    cuSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());

    ImportValidator validator = new ImportValidator(TypeUtility.getPackage(formDataIcu).getElementName());

    // loop through all types recursively to ensure all simple names that will be created are "consumed" in the import validator
    consumeAllTypeNamesRec(formDataSourceBuilder, validator);
    if (monitor.isCanceled()) {
      return null;
    }

    // create source code
    StringBuilder sourceBuilder = new StringBuilder();
    cuSourceBuilder.createSource(sourceBuilder, ResourceUtility.getLineSeparator(formDataIcu), formDataIcu.getJavaProject(), validator);
    if (monitor.isCanceled()) {
      return null;
    }

    return sourceBuilder.toString();
  }
}
