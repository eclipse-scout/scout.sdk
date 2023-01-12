/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.annotation;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;

/**
 * <h3>{@link ExtendsAnnotation}</h3>
 *
 * @since 6.1.0
 */
public class ExtendsAnnotation extends AbstractManagedAnnotation {
  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(IScoutApi.class, IScoutApi::Extends);

  public IType value() {
    return getValueFrom(IScoutApi.class, api -> api.Extends().valueElementName(), IType.class, null);
  }

  public IType[] pathToContainer() {
    return getValueFrom(IScoutApi.class, api -> api.Extends().pathToContainerElementName(), IType[].class, null);
  }
}
