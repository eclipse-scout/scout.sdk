/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.enums;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.s.model.js.prop.IScoutJsPropertyValue;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.typescript.model.api.DataTypeFulfillsEvaluator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeOf;
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ES6ClassTypeAliasScoutEnum implements IScoutJsEnum {

  private final ScoutJsModel m_scoutJsModel;
  private final IES6Class m_class;
  private final IScoutJsEnum m_wrappedEnum;

  protected ES6ClassTypeAliasScoutEnum(ScoutJsModel scoutJsModel, IES6Class clazz, IScoutJsEnum wrappedEnum) {
    m_scoutJsModel = scoutJsModel;
    m_class = Ensure.notNull(clazz);
    m_wrappedEnum = Ensure.notNull(wrappedEnum);
  }

  public static Optional<IScoutJsEnum> create(ScoutJsModel owner, IES6Class clazz) {
    if (owner == null || clazz == null || !clazz.isTypeAlias()) {
      return Optional.empty();
    }
    var aliasedType = Optional.of(clazz)
        .flatMap(IES6Class::aliasedDataType);

    if (aliasedType.map(dataType -> ScoutJsCoreConstants.CLASS_NAME_ENUM_OBJECT.equals(dataType.name()))
        .orElse(false)) {
      return aliasedType.stream()
          .flatMap(IDataType::typeArguments)
          .findFirst()
          .filter(ITypeOf.class::isInstance)
          .map(ITypeOf.class::cast)
          .flatMap(ITypeOf::dataTypeOwner)
          .filter(IVariable.class::isInstance)
          .map(IVariable.class::cast)
          .flatMap(variable -> VariableScoutEnum.create(owner, variable))
          .map(scoutJsEnum -> new ES6ClassTypeAliasScoutEnum(owner, clazz, scoutJsEnum));
    }

    return aliasedType
        .filter(dataType -> DataTypeFlavor.Union == dataType.flavor())
        .flatMap(unionDataType -> ConstantValueUnionScoutEnum.create(owner, unionDataType))
        .map(scoutJsEnum -> new ES6ClassTypeAliasScoutEnum(owner, clazz, scoutJsEnum));
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
  }

  protected IScoutJsEnum wrappedEnum() {
    return m_wrappedEnum;
  }

  @Override
  public String name() {
    return declaringClass().name();
  }

  @Override
  public IES6Class declaringClass() {
    return m_class;
  }

  @Override
  public String referenceName() {
    return wrappedEnum().referenceName();
  }

  @Override
  public INodeElement topLevelReference() {
    return wrappedEnum().topLevelReference();
  }

  @Override
  public List<String> constants() {
    return wrappedEnum().constants();
  }

  @Override
  public Stream<? extends IScoutJsPropertyValue> createPropertyValues(ScoutJsProperty property) {
    return wrappedEnum().createPropertyValues(property);
  }

  @Override
  public boolean fulfills(IDataType dataType) {
    return new DataTypeFulfillsEvaluator(dt -> dt == declaringClass() || wrappedEnum().fulfills(dt))
        .fulfills(dataType);
  }

  @Override
  public String toString() {
    return declaringClass().toString();
  }
}
