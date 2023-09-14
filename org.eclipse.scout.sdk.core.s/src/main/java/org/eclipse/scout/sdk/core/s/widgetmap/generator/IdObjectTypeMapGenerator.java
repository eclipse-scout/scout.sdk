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
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaUtils;
import org.eclipse.scout.sdk.core.s.widgetmap.IdObjectType;
import org.eclipse.scout.sdk.core.s.widgetmap.IdObjectTypeMap;
import org.eclipse.scout.sdk.core.s.widgetmap.IdObjectTypeMapReference;
import org.eclipse.scout.sdk.core.s.widgetmap.Type;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
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
        .withElementName(Type.ensureValidName(map.name()));

    var intersection = CompositeTypeGenerator.create()
        .withFlavor(DataTypeFlavor.Intersection);

    if (!map.elements().isEmpty()) {
      var objectType = TypeGenerator.create();
      map.elements().values().stream().map(this::createIdObjectTypeField)
          .forEach(objectType::withField);
      intersection.withType(objectType);
    }
    map.idObjectTypeMapReferences().stream()
        .map(IdObjectTypeMapReference::reference)
        .forEach(ref -> intersection.withType(b -> b.ref(ref)));
    withAliasedType(intersection);
  }

  protected IFieldGenerator<?> createIdObjectTypeField(IdObjectType idObjectType) {
    var objectType = idObjectType.objectType();
    var objectLiteral = m_map.model();
    var es6Class = objectType.es6Class();
    var dataType = objectType
        .newClassName()
        .map(name -> objectLiteral.spi().createDataType(name).api())
        .orElseGet(() -> {
          var typeParameterCount = es6Class.typeParameters()
              .map(ITypeParameter::defaultConstraint)
              .filter(Optional::isEmpty)
              .count();
          if (typeParameterCount < 1) {
            return es6Class;
          }
          var typeArgs = Stream.generate(() -> TypeScriptTypes._any)
              .limit(typeParameterCount)
              .map(any -> objectLiteral.spi().createDataType(any).api())
              .toList();
          return es6Class.containingModule().nodeElementFactory().createClassWithTypeArguments(es6Class, typeArgs);
        });

    var id = JavaUtils.toStringLiteral(idObjectType.id(), "'", true).toString();
    return FieldGenerator.create()
        .withElementName(id)
        .withDataType(dataType);
  }

  /**
   * @return The {@link IdObjectTypeMap} that will be generated.
   */
  public Optional<IdObjectTypeMap> map() {
    return Optional.ofNullable(m_map);
  }

  /**
   * Sets the {@link IdObjectTypeMap} that will be generated as part of this {@link TypeAliasGenerator}.
   * 
   * @param map
   *          The new {@link IdObjectTypeMap}.
   * @return This generator.
   */
  public IdObjectTypeMapGenerator withMap(IdObjectTypeMap map) {
    m_map = map;
    return this;
  }
}
