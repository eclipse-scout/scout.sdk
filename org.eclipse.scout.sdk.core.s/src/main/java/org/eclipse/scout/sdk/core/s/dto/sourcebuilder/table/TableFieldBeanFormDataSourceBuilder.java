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
package org.eclipse.scout.sdk.core.s.dto.sourcebuilder.table;

import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;

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
  public TableFieldBeanFormDataSourceBuilder(IType modelType, FormDataAnnotation formDataAnnotation, String typeName, ILookupEnvironment lookupEnv) {
    super(modelType, typeName, lookupEnv, false);
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
    return DtoUtils.computeSuperTypeSignatureForFormData(getModelType(), getFormDataAnnotation(), this);
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }
}
