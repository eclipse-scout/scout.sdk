/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto.table;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.dto.AbstractTableBeanGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link PageDataGenerator}</h3>
 *
 * @since 3.10.0 2013-08-28
 */
public class PageDataGenerator<TYPE extends PageDataGenerator<TYPE>> extends AbstractTableBeanGenerator<TYPE> {

  private final DataAnnotationDescriptor m_dataAnnotation;

  public PageDataGenerator(IType modelType, DataAnnotationDescriptor dataAnnotation, IJavaEnvironment targetEnv) {
    super(modelType, targetEnv);
    m_dataAnnotation = Ensure.notNull(dataAnnotation);
  }

  @Override
  protected String computeSuperType() {
    return dataAnnotation().getSuperDataType()
        .map(IType::reference)
        .orElseGet(() -> dataAnnotation()
            .getDataType()
            .javaEnvironment()
            .requireApi(IScoutApi.class)
            .AbstractTablePageData()
            .fqn());
  }

  public DataAnnotationDescriptor dataAnnotation() {
    return m_dataAnnotation;
  }
}
