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

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.enums.ConstantValueUnionScoutEnum;
import org.eclipse.scout.sdk.core.s.model.js.enums.IScoutJsEnum;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class ScoutJsPropertyType {

  private final IDataType m_dataType;
  private final ScoutJsProperty m_declaringProperty;
  private final ScoutJsPropertySubType m_subType;
  private final FinalValue<Set<IES6Class>> m_classes;
  private final FinalValue<Optional<IScoutJsEnum>> m_enum;

  public ScoutJsPropertyType(IDataType dataType, ScoutJsPropertySubType subType, ScoutJsProperty declaringProperty) {
    m_dataType = dataType; // dataType may be null in case the property is based on a Field and the field has no datatype (cannot be detected. e.g. in JavaScript: this.myField = null)
    m_subType = Ensure.notNull(subType);
    m_declaringProperty = Ensure.notNull(declaringProperty);
    m_classes = new FinalValue<>();
    m_enum = new FinalValue<>();
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

  public boolean isEnumLike() {
    return scoutJsEnum().isPresent();
  }

  public Optional<IScoutJsEnum> scoutJsEnum() {
    return m_enum.computeIfAbsentAndGet(() -> dataType().flatMap(dataType -> declaringProperty().scoutJsObject().scoutJsModel()
        .findScoutEnums()
        .withIncludeDependencies(true)
        .withFulfillsDataType(dataType)
        .first()
        .or(() -> ConstantValueUnionScoutEnum.create(declaringProperty().scoutJsObject().scoutJsModel(), dataType))));
  }

  public boolean isArray() {
    return m_dataType != null && m_dataType.flavor() == DataTypeFlavor.Array;
  }

  public boolean hasClasses() {
    return !getClasses().isEmpty() && !isEnumLike();
  }

  public Stream<IES6Class> classes() {
    return getClasses().stream();
  }

  protected Set<IES6Class> getClasses() {
    return m_classes.computeIfAbsentAndGet(() -> unwrappedDataType() // handles arrays, union and intersection types (non-recursive)
        .map(this::convertToClass)
        .filter(Objects::nonNull)
        .collect(toCollection(LinkedHashSet::new)));
  }

  protected Stream<IDataType> unwrappedDataType() {
    return dataType()
        .map(IDataType::spi)
        .map(a -> a.componentDataTypes().isEmpty() ? Stream.of(a) : a.componentDataTypes().stream())
        .orElseGet(Stream::empty)
        .map(DataTypeSpi::api);
  }

  protected IES6Class convertToClass(IDataType dataType) {
    if (!(dataType instanceof IES6Class clazz)) {
      return null;
    }
    if (!ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME.equals(clazz.containingModule().name())) {
      return clazz;
    }
    var className = clazz.name();
    if (ScoutJsCoreConstants.CLASS_NAMES_MODEL_TYPES.contains(className)) {
      return clazz
          .typeArguments()
          .findFirst()
          .filter(IES6Class.class::isInstance)
          .map(IES6Class.class::cast)
          .orElse(clazz);
    }
    var scoutJsCoreModel = declaringProperty().scoutJsObject().scoutJsModel();
    if (ScoutJsCoreConstants.CLASS_NAME_STATUS_OR_MODEL.equals(className)) {
      var status = scoutJsCoreModel.exportedScoutObjects().get(ScoutJsCoreConstants.CLASS_NAME_STATUS);
      return Optional.ofNullable(status)
          .map(IScoutJsObject::declaringClass)
          .orElse(clazz);
    }
    if (ScoutJsCoreConstants.CLASS_NAME_LOOKUP_CALL_OR_MODEL.equals(className)) {
      var lookupCall = scoutJsCoreModel.exportedScoutObjects().get(ScoutJsCoreConstants.CLASS_NAME_LOOKUP_CALL);
      return Optional.ofNullable(lookupCall)
          .map(IScoutJsObject::declaringClass)
          .orElse(clazz);
    }
    return clazz;
  }

  public boolean isBoolean() {
    return TypeScriptTypes._boolean.equals(dataTypeName());
  }

  protected String dataTypeName() {
    return dataType().map(IDataType::name).orElse(null);
  }
}
