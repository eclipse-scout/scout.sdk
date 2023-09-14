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

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.typescript.model.api.DataTypeFulfillsEvaluator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * Enums based on TypeScript enum keyword like:
 * 
 * <pre>
 * enum LogLevel {
 *   INFO = 'info',
 *   WARN = 'warn',
 *   ERROR = 'error'
 * }
 * </pre>
 */
public class ES6ClassEnumScoutEnum implements IScoutJsEnum {

  private final ScoutJsModel m_scoutJsModel;
  private final IES6Class m_class;
  private final FinalValue<List<String>> m_constants;

  protected ES6ClassEnumScoutEnum(ScoutJsModel scoutJsModel, IES6Class clazz) {
    m_scoutJsModel = scoutJsModel;
    m_class = clazz;
    m_constants = new FinalValue<>();
  }

  public static Optional<IScoutJsEnum> create(ScoutJsModel owner, IES6Class clazz) {
    if (owner == null || clazz == null || !clazz.isEnum()) {
      return Optional.empty();
    }
    return Optional.of(new ES6ClassEnumScoutEnum(owner, clazz));
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
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
  public INodeElement topLevelReference() {
    return declaringClass();
  }

  @Override
  public List<String> constants() {
    return m_constants.computeIfAbsentAndGet(this::parseConstants);
  }

  protected List<String> parseConstants() {
    return declaringClass().fields().stream().map(IField::name).toList();
  }

  @Override
  public boolean fulfills(IDataType dataType) {
    return new DataTypeFulfillsEvaluator(dt -> dt == declaringClass()).fulfills(dataType);
  }

  @Override
  public String toString() {
    return declaringClass().toString();
  }
}
