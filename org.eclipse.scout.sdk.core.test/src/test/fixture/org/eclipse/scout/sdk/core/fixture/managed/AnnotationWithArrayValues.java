/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture.managed;

import static org.eclipse.scout.sdk.core.apidef.IClassNameSupplier.raw;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;

/**
 * <h3>{@link AnnotationWithArrayValues}</h3>
 *
 * @since 6.1.0
 */
public class AnnotationWithArrayValues extends AbstractManagedAnnotation {
  protected static final ApiFunction<?, IClassNameSupplier> TYPE_NAME = new ApiFunction<>(raw(org.eclipse.scout.sdk.core.fixture.AnnotationWithArrayValues.class.getName()));

  public AnnotationWithSingleValues[] annos() {
    return getValue("annos", AnnotationWithSingleValues[].class, null);
  }
}
