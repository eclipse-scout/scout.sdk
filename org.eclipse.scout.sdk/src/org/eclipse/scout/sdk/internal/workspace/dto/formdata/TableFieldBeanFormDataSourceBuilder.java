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
package org.eclipse.scout.sdk.internal.workspace.dto.formdata;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.DtoUtility;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link TableFieldBeanFormDataSourceBuilder}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public class TableFieldBeanFormDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private FormDataAnnotation m_formDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public TableFieldBeanFormDataSourceBuilder(IType modelType, String elementName, FormDataAnnotation formDataAnnotation, IProgressMonitor monitor) {
    super(modelType, elementName, false, monitor);
    m_formDataAnnotation = formDataAnnotation;
    setup(monitor);
  }

  @Override
  protected void createContent(IProgressMonitor monitor) {
    super.createContent(monitor);
    collectProperties(monitor);
  }

  @Override
  protected String computeSuperTypeSignature() throws JavaModelException {
    String superTypeSignature = null;
    if (ScoutTypeUtility.existsReplaceAnnotation(getModelType())) {
      IType replacedType = getLocalTypeHierarchy().getSuperclass(getModelType());
      IType replacedFormFieldDataType = DtoUtility.getFormDataType(replacedType, getLocalTypeHierarchy());
      if (replacedFormFieldDataType != null) {
        superTypeSignature = SignatureCache.createTypeSignature(replacedFormFieldDataType.getFullyQualifiedName());
      }
      addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createReplaceAnnotationBuilder());
    }
    if (superTypeSignature == null) {
      superTypeSignature = DtoUtility.computeSuperTypeSignatureForFormData(getModelType(), getFormDataAnnotation(), getLocalTypeHierarchy());

    }
    return superTypeSignature;
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }
}