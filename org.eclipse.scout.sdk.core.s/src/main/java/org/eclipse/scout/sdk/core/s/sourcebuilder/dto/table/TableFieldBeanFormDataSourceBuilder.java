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
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto.table;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.sourcebuilder.dto.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;

/**
 * <h3>{@link TableFieldBeanFormDataSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-08-27
 */
public class TableFieldBeanFormDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private FormDataAnnotationDescriptor m_formDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public TableFieldBeanFormDataSourceBuilder(IType modelType, FormDataAnnotationDescriptor formDataAnnotation, String targetPackageName, String typeName, IJavaEnvironment env) {
    super(modelType, targetPackageName, typeName, env, false);
    m_formDataAnnotation = formDataAnnotation;
    setup();
  }

  @Override
  protected void createContent() {
    super.createContent();
    collectProperties();
  }

  @Override
  protected String computeSuperTypeSignature() {
    return DtoUtils.computeSuperTypeSignatureForFormData(getModelType(), getFormDataAnnotation());
  }

  public FormDataAnnotationDescriptor getFormDataAnnotation() {
    return m_formDataAnnotation;
  }
}
