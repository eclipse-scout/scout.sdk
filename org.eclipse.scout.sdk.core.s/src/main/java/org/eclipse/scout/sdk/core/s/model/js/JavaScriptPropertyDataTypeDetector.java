/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

public class JavaScriptPropertyDataTypeDetector {

  private final List<IPropertyDataTypeOverride> m_overrides;

  public JavaScriptPropertyDataTypeDetector(JavaScriptScoutObject owner) {
    m_overrides = Arrays.asList(new PreserveOnPropertyChangeOverride(owner), new WidgetPropertyOverride(owner), new NlsPropertyOverride(owner)); // order is important!
  }

  public ScoutJsPropertyType detect(IField field) {
    var fieldName = field.name();
    return m_overrides.stream()
        .flatMap(override -> override.overrideType(fieldName).stream())
        .findFirst()
        .orElseGet(() -> new ScoutJsPropertyType(field.dataType().orElse(null)));
  }

  public Map<String, ScoutJsPropertyType> unused() {
    return m_overrides.stream()
        .flatMap(o -> o.unused().entrySet().stream())
        .collect(toUnmodifiableMap(Entry::getKey, Entry::getValue, (a, b) -> a));
  }

  public interface IPropertyDataTypeOverride {
    Optional<ScoutJsPropertyType> overrideType(String propertyName);

    Map<String, ScoutJsPropertyType> unused();
  }

  protected abstract static class AbstractPropertyOverride implements IPropertyDataTypeOverride {

    protected static final String PROPERTY_TYPE_METHOD_REGEX_PREFIX = "this\\.";
    protected static final String PROPERTY_TYPE_METHOD_REGEX_SUFFIX = "\\(\\[?([^])]+)";
    protected static final Pattern REGEX_COMMA = Pattern.compile(",");

    private final Map<String /* propertyName */, Boolean /* Used */> m_overrides;

    protected AbstractPropertyOverride(Stream<String> names) {
      m_overrides = names.collect(toMap(identity(), s -> Boolean.FALSE, (a, b) -> Boolean.FALSE, HashMap::new));
    }

    @Override
    public Optional<ScoutJsPropertyType> overrideType(String propertyName) {
      return Optional.ofNullable(m_overrides.computeIfPresent(propertyName, (k, v) -> Boolean.TRUE))
          .map(f -> getType());
    }

    protected abstract ScoutJsPropertyType getType();

    @Override
    public Map<String, ScoutJsPropertyType> unused() {
      return m_overrides.entrySet().stream()
          .filter(e -> !e.getValue())
          .collect(toUnmodifiableMap(Entry::getKey, e -> getType()));
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
          .flatMap(AbstractPropertyOverride::splitPropertyNames);
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

  protected static class PreserveOnPropertyChangeOverride extends AbstractPropertyOverride {

    protected static final Pattern REGEX_PRESERVE_PROPERTY_TYPE = Pattern.compile(PROPERTY_TYPE_METHOD_REGEX_PREFIX + "_addPreserveOnPropertyChangeProperties" + PROPERTY_TYPE_METHOD_REGEX_SUFFIX);

    protected PreserveOnPropertyChangeOverride(JavaScriptScoutObject owner) {
      super(owner.constructors().stream().flatMap(constr -> parseMethodCallWithStringArguments(constr, REGEX_PRESERVE_PROPERTY_TYPE)));
    }

    @Override
    protected ScoutJsPropertyType getType() {
      return new ScoutJsPropertyType(ScoutJsModel.DATA_TYPE_STRING);
    }
  }

  protected static class WidgetPropertyOverride extends AbstractPropertyOverride {

    protected static final Pattern REGEX_WIDGET_PROPERTY_TYPE = Pattern.compile(PROPERTY_TYPE_METHOD_REGEX_PREFIX + "_addWidgetProperties" + PROPERTY_TYPE_METHOD_REGEX_SUFFIX);

    protected WidgetPropertyOverride(JavaScriptScoutObject owner) {
      super(owner.constructors().stream().flatMap(constr -> parseMethodCallWithStringArguments(constr, REGEX_WIDGET_PROPERTY_TYPE)));
    }

    @Override
    protected ScoutJsPropertyType getType() {
      return new ScoutJsPropertyType(ScoutJsModel.DATA_TYPE_WIDGET);
    }
  }

  protected static class NlsPropertyOverride extends AbstractPropertyOverride {

    protected static final Pattern REGEX_NLS_PROPERTY_TYPE = Pattern.compile(PROPERTY_TYPE_METHOD_REGEX_PREFIX + "resolveTextKeys" + PROPERTY_TYPE_METHOD_REGEX_SUFFIX);

    protected NlsPropertyOverride(JavaScriptScoutObject owner) {
      super(owner._inits().stream().flatMap(init -> parseMethodCallWithStringArguments(init, REGEX_NLS_PROPERTY_TYPE)));
    }

    @Override
    protected ScoutJsPropertyType getType() {
      return new ScoutJsPropertyType(ScoutJsModel.DATA_TYPE_STRING, ScoutJsPropertySubType.TEXT_KEY);
    }
  }
}
