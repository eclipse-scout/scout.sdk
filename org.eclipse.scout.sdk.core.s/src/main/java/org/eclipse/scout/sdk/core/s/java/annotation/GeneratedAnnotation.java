/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.annotation;

import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;

/**
 * <h3>{@link GeneratedAnnotation}</h3> Managed annotation for Generated
 *
 * @since 13.0
 */
public class GeneratedAnnotation extends AbstractManagedAnnotation {

  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(IScoutApi.class, IScoutApi::Generated);

  public String[] value() {
    return getValueFrom(IScoutApi.class, api -> api.Generated().valueElementName(), String[].class, null);
  }

  public String date() {
    return getValueFrom(IScoutApi.class, api -> api.Generated().dateElementName(), String.class, null);
  }

  public String comments() {
    return getValueFrom(IScoutApi.class, api -> api.Generated().commentsElementName(), String.class, null);
  }
}
