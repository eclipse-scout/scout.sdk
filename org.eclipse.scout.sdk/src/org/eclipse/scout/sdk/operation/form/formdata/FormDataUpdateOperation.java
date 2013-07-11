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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.data.AbstractSingleDerivedTypeAutoUpdateOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * Update operations that creates {@link IRuntimeClasses#AbstractFormData} and
 * {@link IRuntimeClasses#AbstractFormFieldData} for {@link IRuntimeClasses#IForm} and
 * {@link IRuntimeClasses#IFormField}, respectively.
 * 
 * @since 3.10.0-M1
 */
public class FormDataUpdateOperation extends AbstractSingleDerivedTypeAutoUpdateOperation {

  private FormDataAnnotation m_formDataAnnotation;

  public FormDataUpdateOperation(IType type) {
    this(type, null);
  }

  public FormDataUpdateOperation(IType type, FormDataAnnotation annotation) {
    super(type);
    m_formDataAnnotation = annotation;
  }

  @Override
  public String getOperationName() {
    return "Update Form Data for '" + getModelType().getElementName() + "'.";
  }

  @Override
  protected boolean prepare() {
    if (m_formDataAnnotation == null) {
      try {
        m_formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(getModelType(), TypeUtility.getSuperTypeHierarchy(getModelType()));
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not find form data annotation for '" + getModelType().getElementName() + "'.", e);
      }
    }
    // form data
    if (m_formDataAnnotation == null ||
        !FormDataAnnotation.isSdkCommandCreate(m_formDataAnnotation) ||
        StringUtility.isNullOrEmpty(m_formDataAnnotation.getFormDataTypeSignature())) {
      return false;
    }
    return true;
  }

  public IType getFormDataType() {
    return getDerivedModelType();
  }

  @Override
  protected String getDerivedTypeSignature() {
    return m_formDataAnnotation.getFormDataTypeSignature();
  }

  @Override
  protected boolean checkExistingDerivedTypeSuperTypeHierarchy(IType type, ITypeHierarchy hierarchy) {
    return hierarchy.contains(TypeUtility.getType(RuntimeClasses.AbstractFormData))
        || hierarchy.contains(TypeUtility.getType(RuntimeClasses.AbstractFormFieldData));
  }

  @Override
  protected ITypeSourceBuilder createTypeSourceBuilder(IType formDataType) {
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getModelType());
    ITypeSourceBuilder sourceBuilder = FormDataUtility.getPrimaryTypeFormDataSourceBuilder(m_formDataAnnotation.getSuperTypeSignature(), getModelType(), hierarchy, formDataType.getJavaProject());
    sourceBuilder.setElementName(formDataType.getElementName());
    sourceBuilder.setSuperTypeSignature(FormDataUtility.getFormDataSuperTypeSignature(m_formDataAnnotation, getModelType(), hierarchy));
    return sourceBuilder;
  }
}
