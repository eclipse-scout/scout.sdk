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

import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.function.Predicate.not;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue.ConstantValueType;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.Strings;

public final class IdObjectTypeMapUtils {

  private IdObjectTypeMapUtils() {
  }

  public static Set<ObjectType> createObjectTypesForPage(String pageOrModelName, IObjectLiteral pageModel) {
    if (pageOrModelName == null || pageModel == null) {
      return emptySet();
    }

    return Stream.concat(createDetailFormForPage(pageOrModelName, pageModel).stream(), createDetailTableForPage(pageOrModelName, pageModel).stream())
        .collect(Collectors.toCollection(LinkedHashSet::new));
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
}
