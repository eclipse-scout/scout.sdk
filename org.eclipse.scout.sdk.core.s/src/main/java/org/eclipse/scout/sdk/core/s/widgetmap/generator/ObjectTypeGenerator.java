/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap.generator;

import static java.util.function.Predicate.not;

import java.util.Optional;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.widgetmap.IdObjectTypeMap;
import org.eclipse.scout.sdk.core.s.widgetmap.ObjectType;
import org.eclipse.scout.sdk.core.typescript.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

public class ObjectTypeGenerator extends TypeGenerator<ObjectTypeGenerator> {

  private ObjectType m_objectType;

  @Override
  protected void setup() {
    var objectType = objectType().orElseThrow(() -> Ensure.newFail("ObjectType missing."));
    var elementName = objectType.newClassName()
        .filter(not(Strings::isBlank))
        .orElseThrow(() -> Ensure.newFail("Name missing."));

    withModifier(Modifier.EXPORT)
        .asClass()
        .withElementName(elementName)
        .withSuperClass(objectType.es6Class().name());

    objectType.widgetMap()
        .map(ObjectTypeGenerator::createWidgetMapField)
        .ifPresent(this::withField);

    objectType.columnMap()
        .map(ObjectTypeGenerator::createColumnMapField)
        .ifPresent(this::withField);
  }

  protected static IFieldGenerator<?> createIdObjectTypeMapField(IdObjectTypeMap map, String fieldName) {
    return FieldGenerator.create()
        .withElementName(fieldName)
        .withModifier(Modifier.DECLARE)
        .withDataType(map.name());
  }

  protected static IFieldGenerator<?> createWidgetMapField(IdObjectTypeMap map) {
    return createIdObjectTypeMapField(map, ScoutJsCoreConstants.PROPERTY_NAME_WIDGET_MAP);
  }

  protected static IFieldGenerator<?> createColumnMapField(IdObjectTypeMap map) {
    return createIdObjectTypeMapField(map, ScoutJsCoreConstants.PROPERTY_NAME_COLUMN_MAP);
  }

  public Optional<ObjectType> objectType() {
    return Optional.ofNullable(m_objectType);
  }

  public ObjectTypeGenerator withObjectType(ObjectType objectType) {
    m_objectType = objectType;
    return this;
  }
}
