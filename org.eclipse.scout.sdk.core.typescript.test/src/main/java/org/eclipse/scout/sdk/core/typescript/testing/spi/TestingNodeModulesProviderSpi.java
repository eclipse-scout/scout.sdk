/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.testing.spi;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ExportFromImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeModuleImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.PackageJsonImplementor;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ExportFromSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Element;

public class TestingNodeModulesProviderSpi implements NodeModulesProviderSpi {
  public static final String TAG_NAME_MODULE = "module";
  public static final String TAG_NAME_EXPORT = "export";
  public static final String TAG_NAME_CLASS = "class";

  private final Map<String, Optional<NodeModuleSpi>> m_modules = new HashMap<>();

  @Override
  public Optional<NodeModuleSpi> create(Path xmlFile) {
    try {
      var rootElement = Xml.get(xmlFile).getDocumentElement();
      Ensure.isTrue(TAG_NAME_MODULE.equals(rootElement.getLocalName()), "Wrong root tag. Expected: '{}'.", TAG_NAME_MODULE);
      return getOrCreateModule(rootElement);
    }
    catch (IOException e) {
      throw new SdkException("Error reading XML file '{}'.", xmlFile, e);
    }
  }

  @Override
  public Set<NodeModuleSpi> remove(Path changedPath) {
    var keysToRemove = m_modules
        .keySet().stream()
        .filter(changedPath::startsWith)
        .collect(toSet());
    return keysToRemove.stream()
        .map(m_modules::remove)
        .filter(Objects::nonNull)
        .flatMap(Optional::stream)
        .collect(toSet());
  }

  @Override
  public void clear() {
    m_modules.clear();
  }

  private Optional<NodeModuleSpi> getOrCreateModule(Element moduleElement) {
    var name = Ensure.notBlank(moduleElement.getAttribute("name"));
    var version = Strings.notBlank(moduleElement.getAttribute("version")).orElse("0.0.1");
    var subModules = Xml.childElementsWithTagName(moduleElement, TAG_NAME_MODULE);
    var exports = Xml.childElementsWithTagName(moduleElement, TAG_NAME_EXPORT);
    return getOrCreateModule(name, version, subModules, exports);
  }

  public Optional<NodeModuleSpi> getOrCreateModule(String name, String version) {
    return getOrCreateModule(name, version, emptyList(), emptyList());
  }

  private Optional<NodeModuleSpi> getOrCreateModule(String name, String version, Collection<Element> subModules, Collection<Element> exports) {
    var module = m_modules.get(name);
    if (module != null) {
      return module;
    }

    module = Optional.of(createModule(name, version, subModules, exports));
    m_modules.put(name, module);
    return module;
  }

  private NodeModuleSpi createModule(String name, String version, Collection<Element> subModules, Collection<Element> exports) {
    var packageJsonSpi = mock(PackageJsonSpi.class);
    var packageJsonContent = """
        {
          "name": "%s",
          "version": "%s"
        }
        """.formatted(name, version);
    when(packageJsonSpi.getString(eq("name"))).thenReturn(name);
    when(packageJsonSpi.getString(eq("version"))).thenReturn(version);
    when(packageJsonSpi.content()).thenReturn(new ByteArrayInputStream(packageJsonContent.getBytes(StandardCharsets.UTF_8)));
    var dependencies = subModules.stream()
        .map(this::getOrCreateModule)
        .flatMap(Optional::stream)
        .collect(toCollection(LinkedHashSet::new));
    when(packageJsonSpi.dependencies()).thenReturn(dependencies);

    var packageJsonApi = new PackageJsonImplementor(packageJsonSpi);
    when(packageJsonSpi.api()).thenReturn(packageJsonApi);

    var moduleSpi = mock(NodeModuleSpi.class);
    var moduleApi = new NodeModuleImplementor(moduleSpi, packageJsonSpi);
    when(moduleSpi.api()).thenReturn(moduleApi);
    when(moduleSpi.containingModule()).thenReturn(moduleSpi);
    when(moduleSpi.packageJson()).thenReturn(packageJsonSpi);
    when(packageJsonSpi.containingModule()).thenReturn(moduleSpi);

    var allExports = exports.stream()
        .map(e -> createExportFrom(e, moduleSpi))
        .collect(toMap(ExportFromSpi::name, identity(), Ensure::failOnDuplicates, LinkedHashMap::new));
    when(moduleSpi.exports()).thenReturn(allExports);

    return moduleSpi;
  }

  private static ES6ClassSpi createClass(Element classElement, NodeModuleSpi moduleSpi) {
    var name = classElement.getAttribute("name");
    var spi = mock(ES6ClassSpi.class);
    when(spi.name()).thenReturn(name);
    var result = new ES6ClassImplementor(spi);
    when(spi.api()).thenReturn(result);
    when(spi.containingModule()).thenReturn(moduleSpi);
    return spi;
  }

  private static ExportFromSpi createExportFrom(Element exportElement, NodeModuleSpi moduleSpi) {
    var name = exportElement.getAttribute("name");
    var exportSpi = mock(ExportFromSpi.class);
    when(exportSpi.name()).thenReturn(name);
    var result = new ExportFromImplementor(exportSpi);
    when(exportSpi.api()).thenReturn(result);
    when(exportSpi.containingModule()).thenReturn(moduleSpi);

    var referencedElement = Xml.firstChildElement(exportElement, TAG_NAME_CLASS)
        .map(c -> createClass(c, moduleSpi))
        .orElseThrow(); // currently only classes can be exported and must be present

    when(exportSpi.referencedElement()).thenReturn(referencedElement);
    return exportSpi;
  }
}
