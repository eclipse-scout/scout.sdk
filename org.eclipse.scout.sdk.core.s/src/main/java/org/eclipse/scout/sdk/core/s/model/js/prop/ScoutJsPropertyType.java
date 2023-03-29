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
import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.enums.ConstantValueUnionScoutEnum;
import org.eclipse.scout.sdk.core.s.model.js.enums.IScoutJsEnum;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

public class ScoutJsPropertyType {

  private final IDataType m_dataType; // dataType may be null in case the property is based on a Field and the field has no datatype (cannot be detected. e.g. in JavaScript: this.myField = null)
  private final ScoutJsProperty m_declaringProperty;
  private final ScoutJsPropertySubType m_subType;
  private final FinalValue<Set<IScoutJsEnum>> m_enums;
  private final FinalValue<Set<IScoutJsObject>> m_objects;
  private final FinalValue<Set<IES6Class>> m_classes;

  public ScoutJsPropertyType(IDataType dataType, ScoutJsPropertySubType subType, ScoutJsProperty declaringProperty) {
    m_dataType = dataType;
    m_subType = Ensure.notNull(subType);
    m_declaringProperty = Ensure.notNull(declaringProperty);
    m_enums = new FinalValue<>();
    m_objects = new FinalValue<>();
    m_classes = new FinalValue<>();
  }

  public ScoutJsPropertyType(IDataType dataType, ScoutJsProperty declaringProperty) {
    this(dataType, ScoutJsPropertySubType.NOTHING, declaringProperty);
  }

  @Override
  public String toString() {
    var toStringBuilder = new StringBuilder(dataType().map(IDataType::name).orElse("unknown"));
    if (subType() != ScoutJsPropertySubType.NOTHING) {
      toStringBuilder.append(" (sub-type=").append(subType()).append(")");
    }
    return toStringBuilder.toString();
  }

  public ScoutJsPropertySubType subType() {
    return m_subType;
  }

  public Optional<IDataType> dataType() {
    return Optional.ofNullable(m_dataType);
  }

  public ScoutJsProperty declaringProperty() {
    return m_declaringProperty;
  }

  public boolean hasClasses() {
    return !classes().isEmpty() && !isEnumLike();
  }

  public Stream<IScoutJsObject> scoutJsObjects() {
    return getScoutJsObjects().stream();
  }

  protected Set<IScoutJsObject> getScoutJsObjects() {
    return m_objects.computeIfAbsentAndGet(() -> declaringProperty().scoutJsObject().scoutJsModel()
        .findScoutObjects()
        .withIncludeDependencies(true)
        .withObjectClasses(classes())
        .stream()
        .collect(toCollection(LinkedHashSet::new)));
  }

  public Set<IES6Class> classes() {
    return m_classes.computeIfAbsentAndGet(() -> {
      var dt = dataType().orElse(null);
      if (dt == null) {
        return emptySet();
      }

      Set<IES6Class> classes = new LinkedHashSet<>();

      var classCollector = new ScoutJsCoreDataTypesUnwrapVisitor((d, l, i) -> {
        if (d instanceof IES6Class c && !c.isTypeAlias() && !c.isEnum()) {
          classes.add(c);
        }
        return TreeVisitResult.CONTINUE;
      });
      dt.visit(classCollector);
      return classes;
    });
  }

  public boolean isEnumLike() {
    return !getScoutJsEnums().isEmpty();
  }

  public Stream<IScoutJsEnum> scoutJsEnums() {
    return getScoutJsEnums().stream();
  }

  protected Set<IScoutJsEnum> getScoutJsEnums() {
    return m_enums.computeIfAbsentAndGet(() -> dataType()
        .filter(d -> d.flavor() != DataTypeFlavor.Array)
        .stream()
        .flatMap(dataType -> Stream
            .concat(
                declaringProperty().scoutJsObject().scoutJsModel()
                    .findScoutEnums()
                    .withIncludeDependencies(true)
                    .withFulfillsDataType(dataType)
                    .stream(),
                ConstantValueUnionScoutEnum.create(declaringProperty().scoutJsObject().scoutJsModel(), dataType).stream()))
        .collect(toCollection(LinkedHashSet::new)));
  }

  public boolean isArray() {
    return m_dataType != null && m_dataType.flavor() == DataTypeFlavor.Array;
  }

  public boolean isBoolean() {
    return TypeScriptTypes._boolean.equals(dataTypeName());
  }

  protected String dataTypeName() {
    return dataType().map(IDataType::name).orElse(null);
  }
}
