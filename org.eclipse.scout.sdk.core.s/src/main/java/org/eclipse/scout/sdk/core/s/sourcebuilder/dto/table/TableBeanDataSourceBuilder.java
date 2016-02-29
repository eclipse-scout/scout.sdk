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
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.sourcebuilder.dto.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

/**
 * <h3>{@link TableBeanDataSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 28.08.2013
 */
public class TableBeanDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private DataAnnotationDescriptor m_dataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public TableBeanDataSourceBuilder(IType modelType, DataAnnotationDescriptor dataAnnotation, String typeName, IJavaEnvironment env) {
    super(modelType, typeName, env, false);
    m_dataAnnotation = dataAnnotation;
    setup();
  }

  @Override
  protected String computeSuperTypeSignature() {
    IType superDataType = getDataAnnotation().getSuperDataType();
    if (superDataType == null) {
      return Signature.createTypeSignature(IScoutRuntimeTypes.AbstractTablePageData);
    }
    return SignatureUtils.getTypeSignature(superDataType);
  }

  public DataAnnotationDescriptor getDataAnnotation() {
    return m_dataAnnotation;
  }
}
