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

  public static final String TYPE_NAME = IScoutRuntimeTypes.FormData;

  public static enum SdkCommand {
    CREATE, USE, IGNORE, DEFAULT
  }

  public static enum DefaultSubtypeSdkCommand {
    CREATE, IGNORE, DEFAULT
  }

  public IType value() {
    return getValue("value", IType.class, null);
  }

  public SdkCommand sdkCommand() {
    IField enumValueField = getValue("sdkCommand", IField.class, null);
    if (enumValueField != null && StringUtils.isNotBlank(enumValueField.elementName())) {
      SdkCommand cmd = SdkCommand.valueOf(enumValueField.elementName());
      if (cmd != null) {
        return cmd;
      }
    }
    return SdkCommand.DEFAULT;
  }

  public DefaultSubtypeSdkCommand defaultSubtypeSdkCommand() {
    IField enumValueField = getValue("defaultSubtypeSdkCommand", IField.class, null);
    if (enumValueField != null && StringUtils.isNotBlank(enumValueField.elementName())) {
      DefaultSubtypeSdkCommand cmd = DefaultSubtypeSdkCommand.valueOf(enumValueField.elementName());
      if (cmd != null) {
        return cmd;
      }
    }
    return DefaultSubtypeSdkCommand.DEFAULT;
  }

  public int genericOrdinal() {
    return getValue("genericOrdinal", int.class, null);
  }

  public IType[] interfaces() {
    return getValue("interfaces", IType[].class, null);
  }

  public boolean isValueDefault() {
    return isDefault("value");
  }

  public boolean isSdkCommandDefault() {
    return isDefault("sdkCommand");
  }

  public boolean isDefaultSubtypeSdkCommandDefault() {
    return isDefault("defaultSubtypeSdkCommand");
  }

  public boolean isGenericOrdinalDefault() {
    return isDefault("genericOrdinal");
  }

  public boolean isInterfacesDefault() {
    return isDefault("interfaces");
  }
}
