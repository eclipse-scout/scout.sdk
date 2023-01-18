/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.annotation;

import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.model.api.AbstractManagedAnnotation;

/**
 * <h3>{@link GeneratedAnnotation}</h3> Managed annotation for Generated
 *
 * @since 5.2.0
 */
public class GeneratedAnnotation extends AbstractManagedAnnotation {

  public static final String VALUE_ELEMENT_NAME = "value";
  public static final String DATE_ELEMENT_NAME = "date";
  public static final String COMMENTS_ELEMENT_NAME = "comments";

  public static final String FQN = "javax.annotation.Generated";
  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(ITypeNameSupplier.of(FQN));

  public String[] value() {
    return getValue(VALUE_ELEMENT_NAME, String[].class, null);
  }

  public String date() {
    return getValue(DATE_ELEMENT_NAME, String.class, null);
  }

  public String comments() {
    return getValue(COMMENTS_ELEMENT_NAME, String.class, null);
  }
}
