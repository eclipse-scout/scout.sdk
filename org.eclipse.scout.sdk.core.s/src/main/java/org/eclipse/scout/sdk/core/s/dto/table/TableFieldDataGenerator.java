/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto.table;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.dto.AbstractTableBeanGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link TableFieldDataGenerator}</h3>
 *
 * @since 3.10.0 2013-08-27
 */
public class TableFieldDataGenerator<TYPE extends TableFieldDataGenerator<TYPE>> extends AbstractTableBeanGenerator<TYPE> {

  private final FormDataAnnotationDescriptor m_formDataAnnotation;

  public TableFieldDataGenerator(IType modelType, FormDataAnnotationDescriptor formDataAnnotation, IJavaEnvironment targetEnv) {
    super(modelType, targetEnv);
    m_formDataAnnotation = Ensure.notNull(formDataAnnotation);
  }

  @Override
  protected void setupBuilder() {
    super.setupBuilder();
    withPropertyDtos()
        .withAdditionalInterfaces(formDataAnnotation());
  }

  @Override
  protected String computeSuperType() {
    return computeSuperTypeForFormData(modelType(), formDataAnnotation());
  }

  public FormDataAnnotationDescriptor formDataAnnotation() {
    return m_formDataAnnotation;
  }
}
