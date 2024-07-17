/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.objects;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.IPropertyDataTypeOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.KnownStringPropertiesOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.NlsPropertyOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.PreserveOnPropertyChangeOverride;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.WidgetPropertyOverride;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyFactory;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class JavaScriptScoutObject implements IScoutJsObject {

  private static final Pattern REGEX_CONSTANT = Pattern.compile("[A-Z_0-9]+");

  private final ScoutJsModel m_scoutJsModel;
  private final IES6Class m_class;
  private final FinalValue<List<IFunction>> m_init;
  private final FinalValue<List<IFunction>> m_constructor;
  private final FinalValue<Map<String, ScoutJsProperty>> m_properties;

  protected JavaScriptScoutObject(ScoutJsModel scoutJsModel, IES6Class clazz) {
    m_scoutJsModel = scoutJsModel;
    m_class = clazz;
    m_constructor = new FinalValue<>();
    m_init = new FinalValue<>();
    m_properties = new FinalValue<>();
  }

  public static Optional<IScoutJsObject> create(ScoutJsModel owner, IES6Class clazz, IDataType widgetDataType) {
    return Optional.ofNullable(widgetDataType)
        .flatMap(widgetType -> Optional.ofNullable(clazz)
            .map(c -> new JavaScriptScoutObject(owner, c)));
  }

  @Override
  public Map<String, ScoutJsProperty> properties() {
    return m_properties.computeIfAbsentAndGet(this::parseProperties);
  }

  protected Map<String, ScoutJsProperty> parseProperties() {
    var fields = declaringClass()
        .fields()
        .withoutModifier(Modifier.STATIC)
        .stream()
        .filter(f -> !REGEX_CONSTANT.matcher(f.name()).matches());
    return ScoutJsPropertyFactory.createProperties(fields, createOverrides(), this);
  }

  protected List<IPropertyDataTypeOverride> createOverrides() {
    var widgetType = scoutJsModel().widgetClass();
    var stringType = Ensure.notNull(declaringClass().spi().createDataType(TypeScriptTypes._string).api());
    return Arrays.asList( // order is important!
        new PreserveOnPropertyChangeOverride(this, stringType),
        new WidgetPropertyOverride(this, widgetType),
        new NlsPropertyOverride(this, stringType),
        new KnownStringPropertiesOverride(this, stringType));
  }

  @Override
  public List<IFunction> _inits() {
    return m_init.computeIfAbsentAndGet(() -> declaringClass().functions().withName(ScoutJsCoreConstants.FUNCTION_NAME_INIT).stream().toList());
  }

  /**
   * @return An unmodifiable {@link List} holding all constructors of this {@link IScoutJsObject}.
   */
  public List<IFunction> constructors() {
    return m_constructor.computeIfAbsentAndGet(() -> declaringClass().functions().withName("constructor").stream().toList());
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
  public String name() {
    return declaringClass().name();
  }

  @Override
  public String toString() {
    return declaringClass().toString();
  }
}
