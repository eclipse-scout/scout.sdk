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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.IWebConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IExportFrom;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

public class ScoutJsModel {

  public static final String SCOUT_NAMESPACE = "scout";
  private static final Pattern NAMESPACE_PATTERN_WITHOUT_CLASS_REFERENCE = Pattern.compile("window\\.([\\w._]+)\\s*=\\s*Object\\.assign\\(window\\.");
  private static final Pattern NAMESPACE_PATTERN_WITH_CLASS_REFERENCE = Pattern.compile("ObjectFactory\\.get\\(\\)\\.registerNamespace\\('([\\w._]+)',");

  private final INodeModule m_nodeModule;
  private final FinalValue<Optional<String>> m_namespace;
  private final FinalValue<Map<String, IScoutJsObject>> m_objects;
  private boolean m_useClassReference;
  private final boolean m_supportsTypeScript;

  protected ScoutJsModel(INodeModule module) {
    m_nodeModule = module;
    m_namespace = new FinalValue<>();
    m_objects = new FinalValue<>();
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

  public Map<String, IScoutJsObject> scoutObjects() {
    return Collections.unmodifiableMap(m_objects.computeIfAbsentAndGet(this::parseScoutObjects));
  }

  protected Map<String, IScoutJsObject> parseScoutObjects() {
    if (supportsTypeScript()) {
      // parse all the classes with a model
      return nodeModule()
          .exports().stream()
          .map(IExportFrom::referencedElement)
          .filter(element -> element instanceof IES6Class)
          .map(element -> (IES6Class) element)
          .flatMap(element -> TypeScriptScoutObject.create(element).stream())
          .collect(toMap(IScoutJsObject::name, identity()));
    }

    return nodeModule()
        .exports().stream()
        .filter(export -> !export.name().endsWith("Adapter"))
        .filter(export -> !export.name().endsWith("Model"))
        .map(IExportFrom::referencedElement)
        .filter(element -> element instanceof IES6Class)
        .map(element -> (IES6Class) element)
        .flatMap(element -> JavaScriptScoutObject.create(element).stream())
        .collect(toMap(IScoutJsObject::name, identity()));
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
