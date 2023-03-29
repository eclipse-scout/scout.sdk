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

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.s.model.js.prop.IScoutJsPropertyValue;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsConstantValuePropertyValue;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.typescript.model.api.DataTypeFulfillsEvaluator;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class ConstantValueUnionScoutEnum implements IScoutJsEnum {

  private final ScoutJsModel m_scoutJsModel;
  private final IDataType m_unionDataType;
  private final Set<IConstantValue> m_constantValues;
  private final FinalValue<List<String>> m_constants = new FinalValue<>();

  protected ConstantValueUnionScoutEnum(ScoutJsModel scoutJsModel, IDataType unionDataType, Set<IConstantValue> constantValues) {
    m_scoutJsModel = scoutJsModel;
    m_unionDataType = Ensure.notNull(unionDataType);
    m_constantValues = Ensure.notNull(constantValues);
  }

  public static Optional<IScoutJsEnum> create(ScoutJsModel owner, IDataType unionDataType) {
    if (owner == null) {
      return Optional.empty();
    }
    var constantValues = Optional.ofNullable(unionDataType)
        .filter(dataType -> DataTypeFlavor.Union == dataType.flavor())
        .stream()
        .flatMap(IDataType::childTypes)
        .map(IDataType::constantValue)
        .flatMap(Optional::stream)
        .collect(toCollection(LinkedHashSet::new));
    if (constantValues.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new ConstantValueUnionScoutEnum(owner, unionDataType, constantValues));
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
  }

  @Override
  public String name() {
    return m_unionDataType.name();
  }

  @Override
  public IES6Class declaringClass() {
    return null;
  }

  @Override
  public String referenceName() {
    return null;
  }

  @Override
  public INodeElement topLevelReference() {
    return null;
  }

  protected Set<IConstantValue> constantValues() {
    return m_constantValues;
  }

  @Override
  public List<String> constants() {
    return m_constants.computeIfAbsentAndGet(this::parseConstants);
  }

  protected List<String> parseConstants() {
    return constantValues().stream()
        .map(IConstantValue::value)
        .flatMap(Optional::stream)
        .map(Object::toString)
        .collect(Collectors.toList());
  }

  @Override
  public Stream<? extends IScoutJsPropertyValue> createPropertyValues(ScoutJsProperty property) {
    return constantValues().stream()
        .map(v -> new ScoutJsConstantValuePropertyValue(v, property));
  }

  @Override
  public boolean fulfills(IDataType dataType) {
    return new DataTypeFulfillsEvaluator(dt -> dt == m_unionDataType).fulfills(dataType);
  }

  @Override
  public String toString() {
    return m_unionDataType.toString();
  }
}
