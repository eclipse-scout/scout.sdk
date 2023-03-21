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

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.model.js.enums.ES6ClassEnumScoutEnum;
import org.eclipse.scout.sdk.core.s.model.js.enums.ES6ClassTypeAliasScoutEnum;
import org.eclipse.scout.sdk.core.s.model.js.enums.IScoutJsEnum;
import org.eclipse.scout.sdk.core.s.model.js.enums.ScoutJsEnumQuery;
import org.eclipse.scout.sdk.core.s.model.js.enums.VariableScoutEnum;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.s.model.js.objects.JavaScriptScoutObject;
import org.eclipse.scout.sdk.core.s.model.js.objects.ScoutJsObjectQuery;
import org.eclipse.scout.sdk.core.s.model.js.objects.TypeScriptScoutObject;
import org.eclipse.scout.sdk.core.typescript.IWebConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IExportFrom;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

public class ScoutJsModel {

  private static final Pattern NAMESPACE_PATTERN_WITHOUT_CLASS_REFERENCE = Pattern.compile("window\\.([\\w._]+)\\s*=\\s*Object\\.assign\\(window\\.");
  private static final Pattern NAMESPACE_PATTERN_WITH_CLASS_REFERENCE = Pattern.compile("ObjectFactory\\.get\\(\\)\\.registerNamespace\\('([\\w._]+)',");

  private final INodeModule m_nodeModule;
  private final IES6Class m_widgetClass;
  private final FinalValue<Optional<String>> m_namespace;
  private final FinalValue<Map<String, IScoutJsObject>> m_objects;
  private final FinalValue<Collection<IScoutJsEnum>> m_enums;
  private final FinalValue<Boolean> m_supportsTypeScript;
  private final FinalValue<Boolean> m_useClassReference;

  protected ScoutJsModel(INodeModule module, IES6Class widgetClass) {
    m_nodeModule = module;
    m_widgetClass = widgetClass;
    m_namespace = new FinalValue<>();
    m_objects = new FinalValue<>();
    m_enums = new FinalValue<>();
    m_supportsTypeScript = new FinalValue<>();
    m_useClassReference = new FinalValue<>();
  }

  public ScoutJsObjectQuery findScoutObjects() {
    return new ScoutJsObjectQuery(this);
  }

  public Map<String, IScoutJsObject> exportedScoutObjects() {
    return m_objects.computeIfAbsentAndGet(this::parseScoutObjects);
  }

  protected Map<String, IScoutJsObject> parseScoutObjects() {
    var widgetClass = widgetClass();
    Stream<IScoutJsObject> objects;
    if (supportsTypeScript()) {
      objects = nodeModule()
          .exports().stream()
          .map(IExportFrom::referencedElement)
          .filter(IES6Class.class::isInstance)
          .map(IES6Class.class::cast)
          .filter(c -> !c.isTypeAlias() && !c.isEnum())
          .map(element -> TypeScriptScoutObject.create(this, element).orElseThrow());
    }
    else {
      objects = nodeModule()
          .exports().stream()
          .filter(export -> !export.name().endsWith("Adapter"))
          .filter(export -> !export.name().endsWith("Model"))
          .map(IExportFrom::referencedElement)
          .filter(IES6Class.class::isInstance)
          .map(IES6Class.class::cast)
          .flatMap(element -> JavaScriptScoutObject.create(this, element, widgetClass).stream());
    }
    var myObjects = objects.collect(toMap(IScoutJsObject::name, identity(), (a, b) -> {
      SdkLog.warning("Duplicate classes with name '{}' in module '{}'.", a.name(), nodeModule().name());
      return b;
    }, LinkedHashMap::new));
    return unmodifiableMap(myObjects);
  }

  public ScoutJsEnumQuery findScoutEnums() {
    return new ScoutJsEnumQuery(this);
  }

  public Collection<IScoutJsEnum> exportedScoutEnums() {
    return m_enums.computeIfAbsentAndGet(this::parseScoutEnums);
  }

  protected Collection<IScoutJsEnum> parseScoutEnums() {
    Stream<IScoutJsEnum> enums;
    if (supportsTypeScript()) {
      enums = Stream.concat(
          nodeModule().exports().stream()
              .map(IExportFrom::referencedElement)
              .filter(IES6Class.class::isInstance)
              .map(IES6Class.class::cast)
              .filter(IES6Class::isTypeAlias)
              .flatMap(element -> ES6ClassTypeAliasScoutEnum.create(this, element).stream()),
          nodeModule().exports().stream()
              .map(IExportFrom::referencedElement)
              .filter(IES6Class.class::isInstance)
              .map(IES6Class.class::cast)
              .filter(IES6Class::isEnum)
              .flatMap(element -> ES6ClassEnumScoutEnum.create(this, element).stream()));
    }
    else {
      enums = Stream.concat(
          nodeModule().exports().stream()
              .map(IExportFrom::referencedElement)
              .filter(IES6Class.class::isInstance)
              .map(IES6Class.class::cast)
              .flatMap(element -> element.fields()
                  .withModifier(Modifier.STATIC)
                  .stream()
                  .filter(field -> field.dataType().flatMap(IDataType::objectLiteral).isPresent())
                  .flatMap(field -> VariableScoutEnum.create(this, field).stream())),
          nodeModule().exports().stream()
              .map(IExportFrom::referencedElement)
              .filter(IVariable.class::isInstance)
              .map(IVariable.class::cast)
              .flatMap(variable -> VariableScoutEnum.create(this, variable).stream()));
    }
    return enums.collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Without "this" model. only dependencies. Leaf dependencies (deepest, typically @eclipse-scout/core) come first in
   * the list
   */
  public Stream<INodeModule> scoutJsDependenciesRecursively() {
    var collector = new LinkedHashSet<NodeModuleSpi>(); // deepest dependency first
    var visited = new HashSet<NodeModuleSpi>();
    nodeModule()
        .packageJson().spi()
        .dependencies()
        .forEach(d -> collectScoutCoreModules(d, collector, visited));
    return collector.stream().map(NodeModuleSpi::api);
  }

  protected static boolean collectScoutCoreModules(NodeModuleSpi module, Set<NodeModuleSpi> collector, Set<NodeModuleSpi> visited) {
    if (visited.contains(module)) {
      return collector.contains(module);
    }
    visited.add(module);

    if (ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME.equals(module.packageJson().api().name())) {
      collector.add(module);
      return true;
    }

    var added = false;
    for (var child : module.packageJson().dependencies()) {
      var containsScoutCore = collectScoutCoreModules(child, collector, visited);
      if (containsScoutCore && !added) {
        collector.add(module);
        added = true;
      }
    }
    return added;
  }

  public boolean supportsTypeScript() {
    // TypeScript is supported if the main file is written in TypeScript
    return m_supportsTypeScript.computeIfAbsentAndGet(() -> nodeModule().packageJson()
        .main()
        .filter(main -> main.endsWith(IWebConstants.TS_FILE_SUFFIX))
        .isPresent());
  }

  public IES6Class widgetClass() {
    return m_widgetClass;
  }

  public boolean supportsClassReference() {
    return m_useClassReference.computeIfAbsentAndGet(() -> widgetClass() // only scout-core decides if class-references are supported
        .containingModule()
        .packageJson()
        .mainContent()
        .map(NAMESPACE_PATTERN_WITH_CLASS_REFERENCE::matcher)
        .filter(Matcher::find)
        .isPresent());
  }

  public Optional<String> namespace() {
    return m_namespace.computeIfAbsentAndGet(() -> nodeModule().packageJson()
        .mainContent()
        .flatMap(ScoutJsModel::parseNamespace));
  }

  protected static Optional<String> parseNamespace(CharSequence content) {
    return firstGroup(NAMESPACE_PATTERN_WITH_CLASS_REFERENCE.matcher(content).results())
        .or(() -> firstGroup(NAMESPACE_PATTERN_WITHOUT_CLASS_REFERENCE.matcher(content).results()));
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
