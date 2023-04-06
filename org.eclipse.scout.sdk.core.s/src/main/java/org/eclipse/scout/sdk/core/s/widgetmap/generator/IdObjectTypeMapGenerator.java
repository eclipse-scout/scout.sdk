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

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.widgetmap.IdObjectType;
import org.eclipse.scout.sdk.core.s.widgetmap.IdObjectTypeMap;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.builder.TypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.type.CompositeTypeGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.type.TypeAliasGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;

public class IdObjectTypeMapGenerator extends TypeAliasGenerator<IdObjectTypeMapGenerator> {

  private IdObjectTypeMap m_map;

  @Override
  protected void setup() {
    var map = map().orElseThrow(() -> Ensure.newFail("IdObjectTypeMap missing."));

    withModifier(Modifier.EXPORT)
        .withElementName(map.name());

    var objectType = TypeGenerator.create();
    map.elements().values().stream().map(IdObjectTypeMapGenerator::createIdObjectTypeField)
        .forEach(objectType::withField);

    var intersection = CompositeTypeGenerator.create()
        .withFlavor(DataTypeFlavor.Intersection)
        .withType(objectType);
    map.idObjectTypeMapReferences()
        .forEach(ref -> intersection.withType(b -> TypeScriptSourceBuilder.create(b)
            .append(ref.name())));
    withAliasedType(intersection);
  }

  protected static IFieldGenerator<?> createIdObjectTypeField(IdObjectType idObjectType) {
    var id = "'" + idObjectType.id() + "'";

    var objectType = idObjectType.objectType();
    var dataType = objectType.newClassName().orElseGet(() -> {
      var es6Class = objectType.es6Class();
      var name = es6Class.name();
      var typeParameterCount = es6Class.typeParameters()
          .map(ITypeParameter::defaultConstraint)
          .filter(Optional::isEmpty)
          .count();
      if (typeParameterCount > 0) {
        name += Stream.generate(() -> TypeScriptTypes._any)
            .limit(typeParameterCount)
            .collect(Collectors.joining(", ", "<", ">"));
      }
      return name;
    });

    return FieldGenerator.create()
        .withElementName(id)
        .withDataType(dataType);
  }

  public Optional<IdObjectTypeMap> map() {
    return Optional.ofNullable(m_map);
  }

  public IdObjectTypeMapGenerator withMap(IdObjectTypeMap map) {
    m_map = map;
    return this;
  }
}
