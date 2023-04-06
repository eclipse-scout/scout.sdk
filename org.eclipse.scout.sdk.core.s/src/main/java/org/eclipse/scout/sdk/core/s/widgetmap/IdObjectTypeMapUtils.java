/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import static java.util.Optional.empty;
import static java.util.function.Predicate.not;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.widgetmap.generator.IdObjectTypeMapGenerator;
import org.eclipse.scout.sdk.core.s.widgetmap.generator.ObjectTypeGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue.ConstantValueType;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.Strings;

public final class IdObjectTypeMapUtils {

  private IdObjectTypeMapUtils() {
  }

  /* **************************************************************************
   * PAGE
   * *************************************************************************/

  public static Stream<ObjectType> createObjectTypesForPage(String pageOrModelName, IObjectLiteral pageModel) {
    if (pageOrModelName == null || pageModel == null) {
      return Stream.empty();
    }

    return Stream.concat(createDetailFormForPage(pageOrModelName, pageModel).stream(), createDetailTableForPage(pageOrModelName, pageModel).stream());
  }

  public static Optional<ObjectType> createDetailFormForPage(String pageOrModelName, IObjectLiteral pageModel) {
    return createObjectTypeForPage(pageOrModelName, pageModel, ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_FORM, not(ScoutJsCoreConstants.CLASS_NAME_FORM::equals));
  }

  public static Optional<ObjectType> createDetailTableForPage(String pageOrModelName, IObjectLiteral pageModel) {
    return createObjectTypeForPage(pageOrModelName, pageModel, ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_TABLE, not(ScoutJsCoreConstants.CLASS_NAME_TABLE::equals));
  }

  private static Optional<ObjectType> createObjectTypeForPage(String pageOrModelName, IObjectLiteral pageModel, String propertyName, Predicate<String> acceptName) {
    if (pageOrModelName == null || pageModel == null || propertyName == null || acceptName == null) {
      return empty();
    }

    var pageName = Strings.removeSuffix(pageOrModelName, ScoutJsCoreConstants.CLASS_NAME_SUFFIX_MODEL);

    return pageModel.property(propertyName)
        .filter(cv -> cv.type() == ConstantValueType.ObjectLiteral)
        .flatMap(IConstantValue::asObjectLiteral)
        .flatMap(ol -> IdObjectType.create(ol)
            .map(idObjectType -> {
              var name = idObjectType.id();
              if (!acceptName.test(name) || name.equals(idObjectType.objectType().es6Class().name())) {
                name = pageName + name;
              }
              var objectType = idObjectType.objectType().withNewClassName(name);

              WidgetMap.create(name, ol, true).ifPresent(objectType::withWidgetMap);
              ColumnMap.create(name, ol).ifPresent(objectType::withColumnMap);

              return objectType;
            }));
  }

  /* **************************************************************************
   * GENERATORS
   * *************************************************************************/

  public static Optional<IdObjectTypeMapGenerator> createIdObjectTypeMapGenerator(IdObjectTypeMap map) {
    return Optional.ofNullable(map)
        .map(m -> new IdObjectTypeMapGenerator()
            .withMap(m));
  }

  public static Optional<ObjectTypeGenerator> createObjectTypeGenerator(ObjectType objectType) {
    return Optional.ofNullable(objectType)
        .filter(ot -> ot.newClassName().isPresent())
        .map(ot -> new ObjectTypeGenerator()
            .withObjectType(ot));
  }

  public static Optional<IdObjectTypeMapGenerator> createWidgetMapGenerator(String widgetOrModelName, IObjectLiteral widgetModel) {
    return WidgetMap.create(widgetOrModelName, widgetModel)
        .flatMap(IdObjectTypeMapUtils::createIdObjectTypeMapGenerator);
  }

  public static Stream<ObjectTypeGenerator> createObjectTypeGeneratorsForPage(String pageOrModelName, IObjectLiteral pageModel) {
    return createObjectTypesForPage(pageOrModelName, pageModel)
        .map(IdObjectTypeMapUtils::createObjectTypeGenerator)
        .flatMap(Optional::stream);
  }

  public static Optional<ObjectTypeGenerator> createDetailFormGeneratorForPage(String pageOrModelName, IObjectLiteral pageModel) {
    return createDetailFormForPage(pageOrModelName, pageModel)
        .flatMap(IdObjectTypeMapUtils::createObjectTypeGenerator);
  }

  public static Optional<ObjectTypeGenerator> createDetailTableGeneratorForPage(String pageOrModelName, IObjectLiteral pageModel) {
    return createDetailTableForPage(pageOrModelName, pageModel)
        .flatMap(IdObjectTypeMapUtils::createObjectTypeGenerator);
  }

  /* **************************************************************************
   * ADDITIONAL GENERATORS
   * *************************************************************************/

  public static Stream<INodeElementGenerator<?>> collectAdditionalGenerators(IdObjectTypeMapGenerator generator) {
    return Optional.ofNullable(generator)
        .flatMap(IdObjectTypeMapGenerator::map)
        .stream()
        .flatMap(IdObjectTypeMapUtils::collectAdditionalGenerators);
  }

  private static Stream<INodeElementGenerator<?>> collectAdditionalGenerators(IdObjectTypeMap map) {
    return map.elements().values().stream()
        .map(IdObjectType::objectType)
        .filter(objectType -> objectType.newClassName().isPresent())
        .flatMap(objectType -> Stream.concat(
            createObjectTypeGenerator(objectType).stream(),
            collectAdditionalGenerators(objectType)));
  }

  public static Stream<INodeElementGenerator<?>> collectAdditionalGenerators(ObjectTypeGenerator generator) {
    return Optional.ofNullable(generator)
        .flatMap(ObjectTypeGenerator::objectType)
        .stream()
        .flatMap(IdObjectTypeMapUtils::collectAdditionalGenerators);
  }

  private static Stream<INodeElementGenerator<?>> collectAdditionalGenerators(ObjectType objectType) {
    return Stream.concat(
        objectType.widgetMap().stream()
            .flatMap(m -> Stream.concat(
                createIdObjectTypeMapGenerator(m).stream(),
                collectAdditionalGenerators(m))),
        objectType.columnMap().stream()
            .flatMap(m -> Stream.concat(
                createIdObjectTypeMapGenerator(m).stream(),
                collectAdditionalGenerators(m))));
  }
}
