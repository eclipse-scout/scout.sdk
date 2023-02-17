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
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.util.Strings.removePrefix;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.IWebConstants;
import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IExportFrom;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

// FIXME model:
//  * add factory/inventory (like IdeaNodeModules)
//  * caching (remove changed entries)
//  * only consider INodeModule with dependency on @eclipse-scout/core
public class ScoutJsModel {

  public static final String SCOUT_NAMESPACE = "scout";
  public static final String OBJECT_TYPE_PROPERTY_NAME = "objectType";
  public static final String ID_PROPERTY_NAME = "id";
  public static final String WIDGET_CLASS_NAME = "Widget";
  public static final IDataType DATA_TYPE_STRING = IDataType.createSimple(TypeScriptTypes._string);
  public static final IDataType DATA_TYPE_WIDGET = IDataType.createSimple(WIDGET_CLASS_NAME);

  private static final Pattern NAMESPACE_PATTERN_WITHOUT_CLASS_REFERENCE = Pattern.compile("window\\.([\\w._]+)\\s*=\\s*Object\\.assign\\(window\\.");
  private static final Pattern NAMESPACE_PATTERN_WITH_CLASS_REFERENCE = Pattern.compile("ObjectFactory\\.get\\(\\)\\.registerNamespace\\('([\\w._]+)',");

  private final INodeModule m_nodeModule;
  private final FinalValue<Optional<String>> m_namespace;
  private final FinalValue<Map<String, IScoutJsObject>> m_objects;
  private final FinalValue<Collection<ScoutJsModel>> m_dependencies;
  private final FinalValue<Optional<IES6Class>> m_scoutWidgetClass;
  private boolean m_useClassReference;
  private final boolean m_supportsTypeScript;

  protected ScoutJsModel(INodeModule module) {
    m_nodeModule = module;
    m_namespace = new FinalValue<>();
    m_objects = new FinalValue<>();
    m_dependencies = new FinalValue<>();
    m_scoutWidgetClass = new FinalValue<>();
    m_useClassReference = true; // default to new layout

    // TypeScript is supported if the main file is written in TypeScript
    m_supportsTypeScript = module.packageJson()
        .main()
        .filter(main -> main.endsWith(IWebConstants.TS_FILE_SUFFIX))
        .isPresent();
  }

  public static Optional<ScoutJsModel> create(Path nodeModuleDir, Object context) {
    return NodeModulesProvider.create(nodeModuleDir, context)
        .map(NodeModuleSpi::api)
        .flatMap(ScoutJsModel::create);
  }

  public static Optional<ScoutJsModel> create(INodeModule module) {
    return Optional.ofNullable(module)
        .map(ScoutJsModel::new);
  }

  public IScoutJsObject scoutObject(String objectType) {
    if (objectType == null) {
      return null;
    }
    var namespace = namespace().orElse(null);

    if (namespace == null && !objectType.contains(".")) {
      var scoutObject = scoutObjectsInternal().get(objectType);
      if (scoutObject != null) {
        return scoutObject;
      }
    }

    if (namespace == null || !SCOUT_NAMESPACE.equals(namespace) && !objectType.startsWith(namespace + ".")) {
      return dependencies().stream()
          .map(scoutJsModel -> scoutJsModel.scoutObject(objectType))
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null);
    }

    return scoutObjectsInternal().get(removePrefix(objectType, namespace + "."));
  }

  public IScoutJsObject scoutObject(IES6Class es6Class) {
    if (es6Class == null) {
      return null;
    }
    if (es6Class.containingModule() != nodeModule()) {
      return dependencies().stream()
          .map(scoutJsModel -> scoutJsModel.scoutObject(es6Class))
          .findFirst()
          .orElse(null);
    }
    return scoutObjectsInternal().values().stream()
        .filter(scoutJsObject -> es6Class == scoutJsObject.declaringClass())
        .findFirst()
        .orElse(null);
  }

  public Stream<IScoutJsObject> scoutObjects() {
    return scoutObjects(true);
  }

  public Stream<IScoutJsObject> scoutObjects(boolean includeDependencies) {
    var stream = scoutObjectsInternal().values().stream();
    if (includeDependencies) {
      stream = Stream.concat(stream, dependencies().stream().flatMap(d -> d.scoutObjects(true)));
    }
    return stream;
  }

  protected Map<String, IScoutJsObject> scoutObjectsInternal() {
    return Collections.unmodifiableMap(m_objects.computeIfAbsentAndGet(this::parseScoutObjects));
  }

  protected Map<String, IScoutJsObject> parseScoutObjects() {
    if (supportsTypeScript()) {
      // parse all the classes with a model
      return nodeModule()
          .exports().stream()
          .map(IExportFrom::referencedElement)
          .filter(IES6Class.class::isInstance)
          .map(IES6Class.class::cast)
          .flatMap(element -> TypeScriptScoutObject.create(this, element).stream())
          .collect(toMap(IScoutJsObject::name, identity()));
    }

    return nodeModule()
        .exports().stream()
        .filter(export -> !export.name().endsWith("Adapter"))
        .filter(export -> !export.name().endsWith("Model"))
        .map(IExportFrom::referencedElement)
        .filter(IES6Class.class::isInstance)
        .map(IES6Class.class::cast)
        .flatMap(element -> JavaScriptScoutObject.create(this, element).stream())
        .collect(toMap(IScoutJsObject::name, identity()));
  }

  public Collection<ScoutJsModel> dependencies() {
    return Collections.unmodifiableCollection(m_dependencies.computeIfAbsentAndGet(this::parseDependencies));
  }

  protected Collection<ScoutJsModel> parseDependencies() {
    return nodeModule().packageJson().dependencies().stream()
        .map(ScoutJsModel::create)
        .flatMap(Optional::stream)
        .collect(toSet());
  }

  public Optional<IES6Class> scoutWidgetClass() {
    return m_scoutWidgetClass.computeIfAbsentAndGet(this::parseScoutWidgetClass);
  }

  protected Optional<IES6Class> parseScoutWidgetClass() {
    if (SCOUT_NAMESPACE.equals(namespace().orElse(null))) {
      var scoutJsObject = scoutObject(WIDGET_CLASS_NAME);
      return Optional.ofNullable(scoutJsObject)
          .map(IScoutJsObject::declaringClass);
    }
    return dependencies().stream()
        .map(ScoutJsModel::scoutWidgetClass)
        .flatMap(Optional::stream)
        .findFirst();
  }

  public boolean supportsClassReference() {
    namespace(); // ensure parsed
    return m_useClassReference;
  }

  public boolean supportsTypeScript() {
    return m_supportsTypeScript;
  }

  public Optional<String> namespace() {
    return m_namespace.computeIfAbsentAndGet(() -> nodeModule().packageJson()
        .mainContent()
        .flatMap(this::parseNamespace));
  }

  protected Optional<String> parseNamespace(CharSequence content) {
    return firstGroup(NAMESPACE_PATTERN_WITH_CLASS_REFERENCE.matcher(content).results())
        .or(() -> firstGroup(NAMESPACE_PATTERN_WITHOUT_CLASS_REFERENCE.matcher(content).results())
            .map(ns -> {
              // legacy mode found: class reference not supported
              m_useClassReference = false;
              return ns;
            }));
  }

  private static Optional<String> firstGroup(Stream<MatchResult> matches) {
    return matches.map(match -> match.group(1))
        .filter(Strings::hasText)
        .findAny();
  }

  public INodeModule nodeModule() {
    return m_nodeModule;
  }

  @Override
  public String toString() {
    return nodeModule().toString();
  }
}
