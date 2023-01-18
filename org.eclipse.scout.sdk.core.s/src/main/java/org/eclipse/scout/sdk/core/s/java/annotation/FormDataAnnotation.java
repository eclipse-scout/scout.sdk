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
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;

/**
 * <h3>{@link FormDataAnnotation}</h3> Represents one single @FormData annotation occurrence
 *
 * @since 5.2.0
 */
public class FormDataAnnotation extends AbstractManagedAnnotation {

  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(IScoutApi.class, IScoutApi::FormData);

  public IType value() {
    return getValueFrom(IScoutApi.class, api -> api.FormData().valueElementName(), IType.class, null);
  }

  public SdkCommand sdkCommand() {
    return getValueAsEnumFrom(IScoutApi.class, api -> api.FormData().sdkCommandElementName(), SdkCommand.class);
  }

  public DefaultSubtypeSdkCommand defaultSubtypeSdkCommand() {
    return getValueAsEnumFrom(IScoutApi.class, api -> api.FormData().defaultSubtypeSdkCommandElementName(), DefaultSubtypeSdkCommand.class);
  }

  public int genericOrdinal() {
    return getValueFrom(IScoutApi.class, api -> api.FormData().genericOrdinalElementName(), int.class, null);
  }

  public IType[] interfaces() {
    return getValueFrom(IScoutApi.class, api -> api.FormData().interfacesElementName(), IType[].class, null);
  }

  public boolean isValueDefault() {
    return isDefault(IScoutApi.class, api -> api.FormData().valueElementName());
  }

  public boolean isSdkCommandDefault() {
    return isDefault(IScoutApi.class, api -> api.FormData().sdkCommandElementName());
  }

  public boolean isDefaultSubtypeSdkCommandDefault() {
    return isDefault(IScoutApi.class, api -> api.FormData().defaultSubtypeSdkCommandElementName());
  }

  public boolean isGenericOrdinalDefault() {
    return isDefault(IScoutApi.class, api -> api.FormData().genericOrdinalElementName());
  }

  public boolean isInterfacesDefault() {
    return isDefault(IScoutApi.class, api -> api.FormData().interfacesElementName());
  }

  public enum SdkCommand {
    CREATE, USE, IGNORE, DEFAULT
  }

  public enum DefaultSubtypeSdkCommand {
    CREATE, IGNORE, DEFAULT
  }
}
