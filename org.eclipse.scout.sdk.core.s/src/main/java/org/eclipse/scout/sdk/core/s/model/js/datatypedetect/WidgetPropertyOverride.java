/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.datatypedetect;

import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.s.model.js.JavaScriptScoutObject;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.util.Ensure;

public class WidgetPropertyOverride extends AbstractStringArrayMethodCallOverride {

  public static final String ADD_WIDGET_PROPERTIES_METHOD_NAME = "_addWidgetProperties";
  public static final Pattern REGEX_WIDGET_PROPERTY_TYPE = Pattern.compile(PROPERTY_TYPE_METHOD_REGEX_PREFIX + ADD_WIDGET_PROPERTIES_METHOD_NAME + PROPERTY_TYPE_METHOD_REGEX_SUFFIX);

  private final IDataType m_widgetType;

  public WidgetPropertyOverride(JavaScriptScoutObject owner, IDataType widgetDataType) {
    super(owner.constructors().stream().flatMap(constr -> parseMethodCallWithStringArguments(constr, REGEX_WIDGET_PROPERTY_TYPE)));
    m_widgetType = Ensure.notNull(widgetDataType);
  }

  @Override
  protected IDataType getOverrideType() {
    return m_widgetType;
  }
}
