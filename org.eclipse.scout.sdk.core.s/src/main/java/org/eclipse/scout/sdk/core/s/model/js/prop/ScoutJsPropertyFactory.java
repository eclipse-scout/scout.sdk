/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.prop;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.IPropertyDataTypeOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.PropertyDataTypeDetector;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Factory for {@link ScoutJsProperty} instances.
 */
public final class ScoutJsPropertyFactory {
  private ScoutJsPropertyFactory() {
  }

  /**
   * Creates {@link ScoutJsProperty} instances based on the {@link IField}s given.
   * 
   * @param properties
   *          The {@link IField} instances for which the corresponding properties should be created. These fields should
   *          correspond to the properties declared in the given {@link IScoutJsObject owner} directly (without any
   *          super or subclasses).
   * @param overrides
   *          {@link IPropertyDataTypeOverride} to use when computing the {@link ScoutJsPropertyType}.
   * @param owner
   *          The {@link IScoutJsObject} to which the created properties belong.
   * @return An unmodifiable {@link Map} holding the created {@link ScoutJsProperty properties} grouped by property
   *         name. It may contain fewer items than the {@link IField properties} passed to this method (some fields may
   *         be ignored).
   */
  public static Map<String, ScoutJsProperty> createProperties(Stream<IField> properties, List<IPropertyDataTypeOverride> overrides, IScoutJsObject owner) {
    var datatypeDetector = new PropertyDataTypeDetector(overrides);
    var excludedProperties = getExcludedProperties(owner.declaringClass());
    var fields = properties
        .filter(f -> !isPrivateOrJQueryLike(f));
    if (!excludedProperties.isEmpty()) {
      fields = fields.filter(f -> !excludedProperties.contains(f.name()));
    }
    var result = fields
        .map(f -> new ScoutJsProperty(owner, f, datatypeDetector))
        .collect(toMap(ScoutJsProperty::name, identity(), (a, b) -> {
          SdkLog.warning("Duplicate property '{}' in '{}'.", b.name(), owner);
          return b;
        }, LinkedHashMap::new));

    datatypeDetector.unused().forEach((name, type) -> {
      if (!excludedProperties.contains(name)) {
        result.compute(name, (key, lower) -> ScoutJsProperty.choose(createSynthetic(owner, key, type), lower));
      }
      SdkLog.warning("Property {}.{} is declared as {} property but is not declared as field.", owner.name(), name, type);
    });

    return unmodifiableMap(result);
  }

  private static ScoutJsProperty createSynthetic(IScoutJsObject owner, String propertyName, IDataType dataType) {
    var syntheticField = owner.scoutJsModel().nodeModule().nodeElementFactory()
        .createSyntheticField(propertyName, dataType, owner.declaringClass());
    return new ScoutJsProperty(owner, syntheticField);
  }

  private static Set<String> getExcludedProperties(INodeElement fieldDeclaringClass) {
    if (ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME.equals(fieldDeclaringClass.containingModule().name())) {
      return ScoutJsCoreConstants.getExcludedProperties(fieldDeclaringClass.name());
    }
    return emptySet();
  }

  private static boolean isPrivateOrJQueryLike(IField field) {
    var name = field.name();
    if (Strings.startsWith(name, '$') || Strings.startsWith(name, '_')) {
      return true;
    }
    var dataTypeName = field.dataType().map(IDataType::name).orElse(null);
    return Strings.startsWith(dataTypeName, ScoutJsCoreConstants.JQUERY);
  }
}
