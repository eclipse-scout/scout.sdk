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

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.DataAnnotation;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

/**
 * <h3>{@link TableBeanDataSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 28.08.2013
 */
public class TableBeanDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private DataAnnotation m_dataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public TableBeanDataSourceBuilder(IType modelType, DataAnnotation dataAnnotation, String typeName, IJavaEnvironment env) {
    super(modelType, typeName, env, false);
    m_dataAnnotation = dataAnnotation;
    setup();
  }

  @Override
  protected String computeSuperTypeSignature() {
    IType superDataType = getDataAnnotation().getSuperDataType();
    if (superDataType == null) {
      return Signature.createTypeSignature(IRuntimeClasses.AbstractTablePageData);
    }
    return SignatureUtils.getTypeSignature(superDataType);
  }

  public DataAnnotation getDataAnnotation() {
    return m_dataAnnotation;
  }
}
