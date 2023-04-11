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

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.widgetmap.generator.IdObjectTypeMapGenerator;
import org.eclipse.scout.sdk.core.s.widgetmap.generator.ObjectTypeGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue.ConstantValueType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
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
        .flatMap(ol -> ol.property(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE)
            .flatMap(IConstantValue::asES6Class)
            .flatMap(es6Class -> ObjectType.create(es6Class, calculateUsedNames(pageModel)))
            .map(objectType -> {
              var es6ClassName = objectType.es6Class().name();
              var newClassName = getId(ol).orElse(es6ClassName);
              if (!acceptName.test(newClassName) || es6ClassName.equals(newClassName)) {
                newClassName = pageName + newClassName;
              }
              return objectType.withNewClassNameAndMaps(newClassName, ol);
            }));
  }

  /* **************************************************************************
   * USED NAMES
   * *************************************************************************/

  public static Collection<String> calculateUsedNames(INodeElement nodeElement) {
    if (nodeElement == null) {
      return new HashSet<>();
    }
    var containingFile = nodeElement.containingFile().orElse(null);
    return nodeElement.containingModule().elements().stream()
        .filter(element -> element.containingFile()
            .filter(otherPath -> !otherPath.equals(containingFile))
            .isPresent())
        .filter(INodeElement::isExportedFromModule)
        .map(INodeElement::moduleExportNames)
        .flatMap(Collection::stream)
        .collect(Collectors.toCollection(HashSet::new));
  }

  /* **************************************************************************
   * PROPERTIES
   * *************************************************************************/

  public static Optional<String> getId(IObjectLiteral model) {
    return Optional.ofNullable(model)
        .flatMap(ol -> ol.property(ScoutJsCoreConstants.PROPERTY_NAME_ID))
        .flatMap(IConstantValue::asString)
        .flatMap(Strings::notBlank);
  }

  public static Optional<IES6Class> getObjectType(IObjectLiteral model) {
    return Optional.ofNullable(model)
        .flatMap(ol -> ol.property(ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE))
        .flatMap(IConstantValue::asES6Class);
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
