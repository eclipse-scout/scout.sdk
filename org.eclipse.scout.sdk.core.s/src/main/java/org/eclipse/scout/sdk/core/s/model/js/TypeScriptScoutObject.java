/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.IPropertyDataTypeOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.NlsPropertyOverride;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class TypeScriptScoutObject implements IScoutJsObject {

  private final IES6Class m_class;
  private final ScoutJsModel m_scoutJsModel;
  private final FinalValue<Optional<IES6Class>> m_model;
  private final FinalValue<Map<String, ScoutJsProperty>> m_properties;
  private final FinalValue<List<IFunction>> m_init;

  protected TypeScriptScoutObject(ScoutJsModel scoutJsModel, IES6Class clazz) {
    m_scoutJsModel = scoutJsModel;
    m_class = clazz;
    m_model = new FinalValue<>();
    m_init = new FinalValue<>();
    m_properties = new FinalValue<>();
  }

  public static Optional<IScoutJsObject> create(ScoutJsModel scoutJsModel, IES6Class clazz) {
    if (scoutJsModel == null || clazz == null) {
      return Optional.empty();
    }
    return Optional.of(new TypeScriptScoutObject(scoutJsModel, clazz));
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
  }

  @Override
  public IES6Class declaringClass() {
    return m_class;
  }

  @Override
  public Map<String, ScoutJsProperty> properties() {
    return m_properties.computeIfAbsentAndGet(() -> {
      var fields = model().orElse(declaringClass())
          .fields()
          .withoutModifier(Modifier.STATIC)
          .stream()
          .filter(f -> !f.name().equals(ScoutJsCoreConstants.PROPERTY_NAME_EVENT_MAP))
          .filter(f -> !f.name().equals(ScoutJsCoreConstants.PROPERTY_NAME_SELF))
          .filter(f -> !f.name().equals(ScoutJsCoreConstants.PROPERTY_NAME_WIDGET_MAP));
      return JavaScriptScoutObject.createProperties(fields, createOverrides(), this);
    });
  }

  protected List<IPropertyDataTypeOverride> createOverrides() {
    var stringType = Ensure.notNull(declaringClass().createDataType(TypeScriptTypes._string));
    return Collections.singletonList(new NlsPropertyOverride(this, stringType));
  }

  @Override
  public List<IFunction> _inits() {
    return m_init.computeIfAbsentAndGet(() -> declaringClass().functions().withName(ScoutJsCoreConstants.FUNCTION_NAME_INIT).stream().toList());
  }

  public Optional<IES6Class> model() {
    return m_model.computeIfAbsentAndGet(() -> findClassField(ScoutJsCoreConstants.PROPERTY_NAME_MODEL));
  }

  protected Optional<IES6Class> findClassField(String name) {
    return declaringClass().field(name)
        .flatMap(IField::dataType)
        .filter(IES6Class.class::isInstance)
        .map(IES6Class.class::cast);
  }

  @Override
  public String name() {
    return declaringClass().name();
  }

  @Override
  public String toString() {
    return declaringClass().toString();
  }
}
