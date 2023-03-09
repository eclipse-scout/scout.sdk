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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertySubType;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

public abstract class AbstractStringArrayMethodCallOverride implements IPropertyDataTypeOverride {

  protected static final String PROPERTY_TYPE_METHOD_REGEX_PREFIX = "this\\.";
  protected static final String PROPERTY_TYPE_METHOD_REGEX_SUFFIX = "\\(\\[?([^])]+)";
  protected static final Pattern REGEX_COMMA = Pattern.compile(",");

  private final Map<String /* propertyName */, Boolean /* used */> m_overrides;
  private final ScoutJsPropertySubType m_subType;

  protected AbstractStringArrayMethodCallOverride(Stream<String> names) {
    this(names, ScoutJsPropertySubType.NOTHING);
  }

  protected AbstractStringArrayMethodCallOverride(Stream<String> names, ScoutJsPropertySubType subType) {
    m_overrides = names.collect(toMap(identity(), s -> Boolean.FALSE, (a, b) -> Boolean.FALSE, HashMap::new));
    m_subType = subType;
  }

  @Override
  public Optional<ScoutJsPropertyType> overrideType(IField field) {
    return Optional.ofNullable(m_overrides.computeIfPresent(field.name(), (k, v) -> Boolean.TRUE))
        .map(v -> createPropertyType(field.dataType().map(IDataType::arrayDimension).orElse(0)));
  }

  protected ScoutJsPropertyType createPropertyType(int arrayDimension) {
    var detectedOverride = getOverrideType();
    var dataType = detectedOverride.createArrayType(arrayDimension);
    return new ScoutJsPropertyType(dataType, m_subType);
  }

  protected abstract IDataType getOverrideType();

  @Override
  public Map<String, IDataType> unused() {
    return m_overrides.entrySet().stream()
        .filter(e -> !e.getValue())
        .collect(toUnmodifiableMap(Entry::getKey, e -> getOverrideType()));
  }

  protected static Stream<String> parseMethodCallWithStringArguments(INodeElement function, Pattern propertyTypePattern) {
    return function.source()
        .map(SourceRange::asCharSequence)
        .map(source -> parseMethodCallWithStringArguments(source, propertyTypePattern))
        .orElse(Stream.empty());
  }

  protected static Stream<String> parseMethodCallWithStringArguments(CharSequence source, Pattern propertyTypePattern) {
    return propertyTypePattern.matcher(source).results()
        .map(result -> result.group(1))
        .flatMap(AbstractStringArrayMethodCallOverride::splitPropertyNames);
  }

  protected static Stream<String> splitPropertyNames(CharSequence propertyNames) {
    return REGEX_COMMA.splitAsStream(propertyNames)
        .map(Strings::trim)
        .map(Strings::withoutQuotes)
        .map(Strings::trim)
        .filter(s -> !s.isEmpty())
        .map(Object::toString);
  }
}
