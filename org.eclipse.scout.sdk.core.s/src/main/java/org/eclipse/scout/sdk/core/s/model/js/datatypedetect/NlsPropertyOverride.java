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
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertySubType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

public class NlsPropertyOverride extends AbstractStringArrayMethodCallOverride {

  public static final Pattern REGEX_NLS_KEYS = Pattern.compile(PROPERTY_TYPE_METHOD_REGEX_PREFIX + ScoutJsCoreConstants.FUNCTION_NAME_RESOLVE_TEXT_KEYS + PROPERTY_TYPE_METHOD_REGEX_SUFFIX);
  public static final Pattern REGEX_NLS_PROPERTY = Pattern.compile("\\." + ScoutJsCoreConstants.FUNCTION_NAME_RESOLVE_TEXT_PROPERTY + "\\(this\\s*,\\s*'([^']+)'");

  private final IDataType m_stringType;

  public NlsPropertyOverride(IScoutJsObject owner, IDataType stringType) {
    super(parseNames(owner), ScoutJsPropertySubType.TEXT_KEY);
    m_stringType = stringType;
  }

  protected static Stream<String> parseNames(IScoutJsObject owner) {
    return owner._inits().stream().flatMap(init -> Stream.concat(
        parseMethodCallWithStringArguments(init, REGEX_NLS_KEYS),
        parseMethodCallWithStringArguments(init, REGEX_NLS_PROPERTY)));
  }

  @Override
  protected IDataType getOverrideType() {
    return m_stringType;
  }
}
