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
package org.eclipse.scout.sdk.core.s.dto.form;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.dto.AbstractDtoGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link FormDataGenerator}</h3>
 *
 * @since 3.10.0 2013-08-27
 */
public class FormDataGenerator<TYPE extends FormDataGenerator<TYPE>> extends AbstractDtoGenerator<TYPE> {

  private final FormDataAnnotationDescriptor m_formDataAnnotation;

  public FormDataGenerator(IType modelType, FormDataAnnotationDescriptor formDataAnnotation, IJavaEnvironment targetEnv) {
    super(modelType, targetEnv);
    m_formDataAnnotation = Ensure.notNull(formDataAnnotation);
  }

  @Override
  protected void setupBuilder() {
    super.setupBuilder();
    withPropertyDtos()
        .withAdditionalInterfaces(formDataAnnotation())
        .withReplaceIfNecessary();
  }

  @Override
  protected String computeSuperType() {
    return computeSuperTypeForFormData(modelType(), formDataAnnotation());
  }

  public FormDataAnnotationDescriptor formDataAnnotation() {
    return m_formDataAnnotation;
  }
}
