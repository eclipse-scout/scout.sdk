/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.annotation;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link FormDataAnnotation}</h3> Represents one single @FormData annotation occurrence
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FormDataAnnotation extends AbstractManagedAnnotation {

  public static final String VALUE_ELEMENT_NAME = "value";
  public static final String INTERFACES_ELEMENT_NAME = "interfaces";
  public static final String GENERIC_ORDINAL_ELEMENT_NAME = "genericOrdinal";
  public static final String DEFAULT_SUBTYPE_SDK_COMMAND_ELEMENT_NAME = "defaultSubtypeSdkCommand";
  public static final String SDK_COMMAND_ELEMENT_NAME = "sdkCommand";

  public static final String TYPE_NAME = IScoutRuntimeTypes.FormData;

  public enum SdkCommand {
    CREATE, USE, IGNORE, DEFAULT
  }

  public enum DefaultSubtypeSdkCommand {
    CREATE, IGNORE, DEFAULT
  }

  public IType value() {
    return getValue(VALUE_ELEMENT_NAME, IType.class, null);
  }

  public SdkCommand sdkCommand() {
    IField enumValueField = getValue(SDK_COMMAND_ELEMENT_NAME, IField.class, null);
    if (enumValueField != null && StringUtils.isNotBlank(enumValueField.elementName())) {
      SdkCommand cmd = SdkCommand.valueOf(enumValueField.elementName());
      if (cmd != null) {
        return cmd;
      }
    }
    return SdkCommand.DEFAULT;
  }

  public DefaultSubtypeSdkCommand defaultSubtypeSdkCommand() {
    IField enumValueField = getValue(DEFAULT_SUBTYPE_SDK_COMMAND_ELEMENT_NAME, IField.class, null);
    if (enumValueField != null && StringUtils.isNotBlank(enumValueField.elementName())) {
      DefaultSubtypeSdkCommand cmd = DefaultSubtypeSdkCommand.valueOf(enumValueField.elementName());
      if (cmd != null) {
        return cmd;
      }
    }
    return DefaultSubtypeSdkCommand.DEFAULT;
  }

  public int genericOrdinal() {
    return getValue(GENERIC_ORDINAL_ELEMENT_NAME, int.class, null);
  }

  public IType[] interfaces() {
    return getValue(INTERFACES_ELEMENT_NAME, IType[].class, null);
  }

  public boolean isValueDefault() {
    return isDefault(VALUE_ELEMENT_NAME);
  }

  public boolean isSdkCommandDefault() {
    return isDefault(SDK_COMMAND_ELEMENT_NAME);
  }

  public boolean isDefaultSubtypeSdkCommandDefault() {
    return isDefault(DEFAULT_SUBTYPE_SDK_COMMAND_ELEMENT_NAME);
  }

  public boolean isGenericOrdinalDefault() {
    return isDefault(GENERIC_ORDINAL_ELEMENT_NAME);
  }

  public boolean isInterfacesDefault() {
    return isDefault(INTERFACES_ELEMENT_NAME);
  }
}
