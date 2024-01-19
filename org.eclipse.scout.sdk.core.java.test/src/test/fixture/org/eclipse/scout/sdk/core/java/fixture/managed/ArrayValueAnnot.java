/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.fixture.managed;

import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.model.api.AbstractManagedAnnotation;

/**
 * <h3>{@link ArrayValueAnnot}</h3> managed wrapper for {@link ArrayValueAnnot}
 *
 * @since 5.1.0
 */
public class ArrayValueAnnot extends AbstractManagedAnnotation {
  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(ITypeNameSupplier.of(org.eclipse.scout.sdk.core.java.fixture.ArrayValueAnnot.class.getName()));

  public String[] value() {
    return getValue("value", String[].class, null);
  }
}
