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

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.IPropertyDataTypeOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.KnownStringPropertiesOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.NlsPropertyOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.PreserveOnPropertyChangeOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.PropertyDataTypeDetector;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.WidgetPropertyOverride;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

public class JavaScriptScoutObject implements IScoutJsObject {

  private static final Pattern REGEX_CONSTANT = Pattern.compile("[A-Z_]+");

  private final ScoutJsModel m_scoutJsModel;
  private final IES6Class m_class;
  private final FinalValue<List<IFunction>> m_init;
  private final FinalValue<List<IFunction>> m_constructor;
  private final FinalValue<Map<String, ScoutJsProperty>> m_properties;

  protected JavaScriptScoutObject(ScoutJsModel scoutJsModel, IES6Class clazz) {
    m_scoutJsModel = scoutJsModel;
    m_class = clazz;
    m_constructor = new FinalValue<>();
    m_init = new FinalValue<>();
    m_properties = new FinalValue<>();
  }

  public static Optional<IScoutJsObject> create(ScoutJsModel owner, IES6Class clazz, IDataType widgetDataType) {
    return Optional.ofNullable(widgetDataType)
        .flatMap(widgetType -> Optional.ofNullable(clazz)
            .map(c -> new JavaScriptScoutObject(owner, c)));
  }

  protected static boolean isPrivateOrJQueryLike(IField field) {
    var name = field.name();
    if (Strings.startsWith(name, '$') || Strings.startsWith(name, '_')) {
      return true;
    }
    var dataTypeName = field.dataType().map(IDataType::name).orElse(null);
    return Strings.startsWith(dataTypeName, ScoutJsCoreConstants.JQUERY);
  }

  @Override
  public Map<String, ScoutJsProperty> properties() {
    return m_properties.computeIfAbsentAndGet(this::parseProperties);
  }

  protected Map<String, ScoutJsProperty> parseProperties() {
    var fields = declaringClass()
        .fields()
        .withoutModifier(Modifier.STATIC)
        .stream()
        .filter(f -> !REGEX_CONSTANT.matcher(f.name()).matches());
    return createProperties(fields, createOverrides(), this);
  }

  protected List<IPropertyDataTypeOverride> createOverrides() {
    var widgetType = scoutJsModel().widgetClass();
    var stringType = Ensure.notNull(declaringClass().createDataType(TypeScriptTypes._string));
    return Arrays.asList( // order is important!
        new PreserveOnPropertyChangeOverride(this, stringType),
        new WidgetPropertyOverride(this, widgetType),
        new NlsPropertyOverride(this, stringType),
        new KnownStringPropertiesOverride(this, stringType));
  }

  protected static Map<String, ScoutJsProperty> createProperties(Stream<IField> properties, List<IPropertyDataTypeOverride> overrides, IScoutJsObject owner) {
    var datatypeDetector = new PropertyDataTypeDetector(overrides);
    var excludedProperties = getExcludedProperties(owner.declaringClass());
    var fields = properties
        .filter(f -> !isPrivateOrJQueryLike(f));
    if (!excludedProperties.isEmpty()) {
      fields = fields.filter(f -> !excludedProperties.contains(f.name()));
    }
    var result = fields
        .map(f -> new ScoutJsProperty(owner, f, datatypeDetector))
        .collect(toMap(ScoutJsProperty::name, identity(), Ensure::failOnDuplicates, LinkedHashMap::new));

    datatypeDetector.unused().forEach((name, type) -> {
      if (!excludedProperties.contains(name)) {
        result.compute(name, (key, lower) -> {
          var syntheticField = owner.scoutJsModel().nodeModule().nodeElementFactory().createSyntheticField(key, type);
          return ScoutJsProperty.choose(new ScoutJsProperty(owner, syntheticField, datatypeDetector), lower);
        });
      }
      SdkLog.warning("Property {}.{} is declared as {} property but is not declared as field.", owner.name(), name, type);
    });

    return unmodifiableMap(result);
  }

  private static Set<String> getExcludedProperties(INodeElement fieldDeclaringClass) {
    if (ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME.equals(fieldDeclaringClass.containingModule().name())) {
      return ScoutJsCoreConstants.getExcludedProperties(fieldDeclaringClass.name());
    }
    return emptySet();
  }

  @Override
  public List<IFunction> _inits() {
    return m_init.computeIfAbsentAndGet(() -> declaringClass().functions().withName(ScoutJsCoreConstants.FUNCTION_NAME_INIT).stream().toList());
  }

  public List<IFunction> constructors() {
    return m_constructor.computeIfAbsentAndGet(() -> declaringClass().functions().withName("constructor").stream().toList());
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
  }

  @Override
  public IES6Class declaringClass() {
    return m_class;
  }

  @Override
  public String name() {
    return declaringClass().name();
  }

  @Override
  public String toString() {
    return declaringClass().toString();
  }
}
