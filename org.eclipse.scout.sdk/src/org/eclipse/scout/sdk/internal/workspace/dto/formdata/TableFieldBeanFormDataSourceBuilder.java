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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataUtility;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link TableFieldBeanFormDataSourceBuilder}</h3>
 * 
 * @author aho
 * @since 3.10.0 27.08.2013
 */
public class TableFieldBeanFormDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private FormDataAnnotation m_formDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public TableFieldBeanFormDataSourceBuilder(IType modelType, String elementName, FormDataAnnotation formDataAnnotation) {
    super(modelType, elementName, false);
    m_formDataAnnotation = formDataAnnotation;
    setup();
  }

  @Override
  protected void createContent() {
    super.createContent();
    collectProperties();
  }

  @Override
  protected String computeSuperTypeSignature() throws JavaModelException {
    String superTypeSignature = null;
    if (ScoutTypeUtility.existsReplaceAnnotation(getModelType())) {
      IType replacedType = getLocalTypeHierarchy().getSuperclass(getModelType());
      IType replacedFormFieldDataType = ScoutTypeUtility.getFormDataType(replacedType, getLocalTypeHierarchy());
      if (replacedFormFieldDataType != null) {
        superTypeSignature = SignatureCache.createTypeSignature(replacedFormFieldDataType.getFullyQualifiedName());
      }
      addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createReplaceAnnotationBuilder());
    }
    if (superTypeSignature == null) {
      superTypeSignature = FormDataUtility.computeSuperTypeSignatureForFormData(getModelType(), getFormDataAnnotation(), getLocalTypeHierarchy());

    }
    return superTypeSignature;
  }

  @Override
  protected String getTableRowDataSuperClassSignature(IType table, ITypeHierarchy fieldHierarchy) throws CoreException {
    IType parentTable = fieldHierarchy.getSuperclass(table);
    if (TypeUtility.exists(parentTable)) {
      IType declaringType = parentTable.getDeclaringType();
      if (TypeUtility.exists(declaringType)) {
        IType formDataType = ScoutTypeUtility.getFormDataType(declaringType, fieldHierarchy);
        IType parentTableBeanData = getTableRowDataType(formDataType);
        if (TypeUtility.exists(parentTableBeanData)) {
          return SignatureCache.createTypeSignature(parentTableBeanData.getFullyQualifiedName());
        }
      }
    }
    return SignatureCache.createTypeSignature(RuntimeClasses.AbstractTableRowData);
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }

}
