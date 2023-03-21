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

import org.eclipse.scout.sdk.core.s.model.js.IScoutJsElement;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class VariableScoutEnum implements IScoutJsEnum {

  private final ScoutJsModel m_scoutJsModel;
  private final IVariable m_variable;
  private final IObjectLiteral m_objectLiteral;
  private final FinalValue<IES6Class> m_declaringClass = new FinalValue<>();
  private final FinalValue<INodeElement> m_reference = new FinalValue<>();
  private final FinalValue<List<String>> m_constants = new FinalValue<>();

  protected VariableScoutEnum(ScoutJsModel scoutJsModel, IVariable variable, IObjectLiteral objectLiteral) {
    m_scoutJsModel = scoutJsModel;
    m_variable = Ensure.notNull(variable);
    m_objectLiteral = Ensure.notNull(objectLiteral);
  }

  public static Optional<IScoutJsEnum> create(ScoutJsModel owner, IVariable variable) {
    if (owner == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(variable)
        .map(IVariable::constantValue)
        .flatMap(IConstantValue::asObjectLiteral)
        .map(objectLiteral -> new VariableScoutEnum(owner, variable, objectLiteral));
  }

  protected IObjectLiteral objectLiteral() {
    return m_objectLiteral;
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
  }

  @Override
  public String name() {
    return m_variable.name();
  }

  @Override
  public IES6Class declaringClass() {
    return m_declaringClass.computeIfAbsentAndGet(() -> {
      if (m_variable instanceof IField field) {
        return field.declaringClass();
      }
      return null;
    });
  }

  @Override
  public String referenceName() {
    return IScoutJsElement.toQualifiedName(Optional.ofNullable(declaringClass())
        .map(IES6Class::name)
        .orElse(null), name());
  }

  @Override
  public INodeElement topLevelReference() {
    return m_reference.computeIfAbsentAndGet(() -> {
      if (m_variable instanceof IField field) {
        return field.declaringClass();
      }
      return m_variable;
    });
  }

  @Override
  public List<String> constants() {
    return m_constants.computeIfAbsentAndGet(this::parseConstants);
  }

  protected List<String> parseConstants() {
    return objectLiteral().properties().keySet().stream().toList();
  }

  @Override
  public boolean fulfills(IDataType dataType) {
    return Optional.ofNullable(dataType)
        .flatMap(IDataType::objectLiteral)
        .map(objectLiteral -> objectLiteral == objectLiteral())
        .orElse(false);
  }

  @Override
  public String toString() {
    return objectLiteral().toString();
  }
}
