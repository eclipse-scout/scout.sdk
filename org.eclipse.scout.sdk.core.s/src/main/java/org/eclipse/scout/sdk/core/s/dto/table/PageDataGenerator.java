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

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.dto.AbstractTableBeanGenerator;
import org.eclipse.scout.sdk.core.s.java.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
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
