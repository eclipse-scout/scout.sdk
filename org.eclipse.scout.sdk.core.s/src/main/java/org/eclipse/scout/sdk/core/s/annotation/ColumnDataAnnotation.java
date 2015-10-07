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
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link ColumnDataAnnotation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ColumnDataAnnotation extends AbstractManagedAnnotation {

  public static final String TYPE_NAME = IScoutRuntimeTypes.ColumnData;
  public static final SdkColumnCommand DEFAULT_VALUE = SdkColumnCommand.CREATE;

  public static enum SdkColumnCommand {
    CREATE, IGNORE
  }

  public SdkColumnCommand value() {
    IField enumValueField = getValue("value", IField.class, null);
    if (enumValueField != null && StringUtils.isNotBlank(enumValueField.elementName())) {
      SdkColumnCommand cmd = SdkColumnCommand.valueOf(enumValueField.elementName());
      if (cmd != null) {
        return cmd;
      }
    }
    return DEFAULT_VALUE;
  }

  public boolean isValueDefault() {
    return isDefault("value");
  }

  public static SdkColumnCommand valueOf(IAnnotatable owner) {
    ColumnDataAnnotation columnCommandAnnotation = owner.annotations().withManagedWrapper(ColumnDataAnnotation.class).first();
    if (columnCommandAnnotation == null) {
      return null;
    }
    return columnCommandAnnotation.value();
  }
}
