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
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;

/**
 * <h3>{@link DataAnnotation}</h3> Describes a Data or PageData annotation.
 *
 * @since 5.2.0
 */
public class DataAnnotation extends AbstractManagedAnnotation {

  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(IScoutApi.class, IScoutApi::Data);

  public static Optional<IType> valueOf(IAnnotatable owner) {
    return owner.annotations()
        .withManagedWrapper(DataAnnotation.class)
        .first()
        .map(DataAnnotation::value);
  }

  public IType value() {
    return getValueFrom(IScoutApi.class, api -> api.Data().valueElementName(), IType.class, null);
  }
}
