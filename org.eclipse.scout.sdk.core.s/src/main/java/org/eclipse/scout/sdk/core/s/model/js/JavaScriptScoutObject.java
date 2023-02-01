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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

public class JavaScriptScoutObject implements IScoutJsObject {

  private static final Pattern REGEX_CONSTANT = Pattern.compile("[A-Z_]+");

  private final IES6Class m_class;
  private final FinalValue<List<IFunction>> m_init;
  private final FinalValue<List<IFunction>> m_constructor;
  private final FinalValue<Map<String, ScoutJsProperty>> m_properties;

  protected JavaScriptScoutObject(IES6Class clazz) {
    m_class = clazz;
    m_constructor = new FinalValue<>();
    m_init = new FinalValue<>();
    m_properties = new FinalValue<>();
  }

  public static Optional<IScoutJsObject> create(IES6Class clazz) {
    return Optional.ofNullable(clazz)
        .filter(c -> !isPrivateOrJQueryLikeName(c.name()))
        .map(JavaScriptScoutObject::new);
  }

  protected static boolean isPrivateOrJQueryLikeName(CharSequence name) {
    return Strings.startsWith(name, '$') || Strings.startsWith(name, '_');
  }

  @Override
  public Map<String, ScoutJsProperty> properties() {
    return m_properties.computeIfAbsentAndGet(this::parseProperties);
  }

  protected Map<String, ScoutJsProperty> parseProperties() {
    var datatypeDetector = new JavaScriptPropertyDataTypeDetector(this);
    var result = declaringClass().fields().stream()
        .filter(f -> !isPrivateOrJQueryLikeName(f.name()))
        .filter(f -> !REGEX_CONSTANT.matcher(f.name()).matches())
        .map(f -> new ScoutJsProperty(f, datatypeDetector))
        .collect(toMap(p -> p.field().name(), identity()));

    datatypeDetector.unused().forEach((name, type) -> SdkLog.debug("Property {}.{} is declared as {} property but is not initialized in the constructor.", name(), name, type));
    return result;
  }

  public List<IFunction> _inits() {
    return m_init.computeIfAbsentAndGet(() -> declaringClass().functions().withName("_init").stream().toList());
  }

  public List<IFunction> constructors() {
    return m_constructor.computeIfAbsentAndGet(() -> declaringClass().functions().withName("constructor").stream().toList());
  }

  @Override
  public IES6Class declaringClass() {
    return m_class;
  }

  @Override
  public String name() {
    return declaringClass().name();
  }
}
