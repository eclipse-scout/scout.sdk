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

import org.eclipse.scout.sdk.core.s.model.js.IScoutJsObject;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertySubType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

public class NlsPropertyOverride extends AbstractStringArrayMethodCallOverride {

  public static final String RESOLVE_TEXT_KEYS_METHOD_NAME = "resolveTextKeys";
  public static final Pattern REGEX_NLS_PROPERTY_TYPE = Pattern.compile(PROPERTY_TYPE_METHOD_REGEX_PREFIX + RESOLVE_TEXT_KEYS_METHOD_NAME + PROPERTY_TYPE_METHOD_REGEX_SUFFIX);

  private final IDataType m_stringType;

  public NlsPropertyOverride(IScoutJsObject owner, IDataType stringType) {
    super(owner._inits().stream().flatMap(init -> parseMethodCallWithStringArguments(init, REGEX_NLS_PROPERTY_TYPE)), ScoutJsPropertySubType.TEXT_KEY);
    m_stringType = stringType;
  }

  @Override
  protected IDataType getOverrideType() {
    return m_stringType;
  }
}
