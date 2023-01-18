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

import java.util.Optional;

import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;

/**
 * <h3>{@link ClassIdAnnotation}</h3>
 *
 * @since 5.2.0
 */
public class ClassIdAnnotation extends AbstractManagedAnnotation {

  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(IScoutApi.class, IScoutApi::ClassId);

  public static Optional<String> valueOf(IAnnotatable owner) {
    return owner.annotations()
        .withManagedWrapper(ClassIdAnnotation.class)
        .first()
        .map(ClassIdAnnotation::value);
  }

  public String value() {
    return getValueFrom(IScoutApi.class, api -> api.ClassId().valueElementName(), String.class, null);
  }
}
