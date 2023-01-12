/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.fixture.managed;

import static org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier.of;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;

/**
 * <h3>{@link AnnotationWithArrayValues}</h3>
 *
 * @since 6.1.0
 */
public class AnnotationWithArrayValues extends AbstractManagedAnnotation {
  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(of(org.eclipse.scout.sdk.core.fixture.AnnotationWithArrayValues.class.getName()));

  public AnnotationWithSingleValues[] annos() {
    return getValue("annos", AnnotationWithSingleValues[].class, null);
  }
}
