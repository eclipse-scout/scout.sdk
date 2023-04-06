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
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue.ConstantValueType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FunctionImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeModuleImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ObjectLiteralImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.PackageJsonImplementor;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeOwnerSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestingNodeModulesProviderSpi implements NodeModulesProviderSpi {
  private static final String TAG_NAME_FILE = "file";
  private static final String TAG_NAME_MODULE = "module";
  private static final String TAG_NAME_EXPORT = "export";
  private static final String TAG_NAME_INDEX = "index";
  private static final String TAG_NAME_REF = "ref";
  private static final String TAG_NAME_FIELD = "field";
  private static final String TAG_NAME_DATA_TYPE = "dataType";
  private static final String TAG_NAME_CLASS = "class";
  private static final String TAG_NAME_SUPER_CLASS = "superClass";
  private static final String TAG_NAME_FUNCTION = "function";
  private static final String TAG_NAME_OBJECT_LITERAL = "objectLiteral";
  private static final String TAG_NAME_PROPERTY = "property";
  private static final String TAG_NAME_CONSTANT_VALUE = "constantValue";
  private static final String TAG_NAME_TYPE = "type";
  private static final String TAG_NAME_VALUE = "value";

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
    var index = Xml.firstChildElement(moduleElement, TAG_NAME_INDEX)
        .map(Node::getTextContent)
        .orElse(null);
    var subModules = Xml.childElementsWithTagName(moduleElement, TAG_NAME_MODULE);
    var exports = Xml.childElementsWithTagName(moduleElement, TAG_NAME_EXPORT);
    return getOrCreateModule(name, version, index, subModules, exports);
  }

  public Optional<NodeModuleSpi> getOrCreateModule(String name, String version) {
    return getOrCreateModule(name, version, null, emptyList(), emptyList());
  }

  private Optional<NodeModuleSpi> getOrCreateModule(String name, String version, CharSequence index, Collection<Element> subModules, Collection<Element> exports) {
    var module = m_modules.get(name);
    if (module != null) {
      return module;
    }

    module = Optional.of(createModule(name, version, index, subModules, exports));
    m_modules.put(name, module);
    return module;
  }

  private NodeModuleSpi createModule(String name, String version, CharSequence index, Collection<Element> subModules, Collection<Element> exports) {
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

    var packageJsonApi = new PackageJsonImplementor(packageJsonSpi) {
      @Override
      public Optional<CharSequence> mainContent() {
        return Optional.ofNullable(index);
      }
    };
    when(packageJsonSpi.api()).thenReturn(packageJsonApi);

    var moduleSpi = mock(NodeModuleSpi.class);
    var moduleApi = new NodeModuleImplementor(moduleSpi, packageJsonSpi);
    when(moduleSpi.api()).thenReturn(moduleApi);
    when(moduleSpi.containingModule()).thenReturn(moduleSpi);
    when(moduleSpi.packageJson()).thenReturn(packageJsonSpi);
    when(packageJsonSpi.containingModule()).thenReturn(moduleSpi);

    var elements = exports.stream()
        .map(e -> createExport(e, moduleSpi))
        .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue, Ensure::failOnDuplicates, LinkedHashMap::new));
    when(moduleSpi.elements()).thenReturn(elements);

    var exportedElements = elements.entrySet().stream()
        .flatMap(e -> e.getValue().stream().map(exportName -> new SimpleEntry<>(exportName, e.getKey())))
        .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue, Ensure::failOnDuplicates, LinkedHashMap::new));
    when(moduleSpi.exports()).thenReturn(exportedElements);

    var classes = elements.keySet().stream()
        .filter(ES6ClassSpi.class::isInstance)
        .map(ES6ClassSpi.class::cast)
        .toList();
    when(moduleSpi.classes()).thenReturn(classes);

    return moduleSpi;
  }

  private static ES6ClassSpi createClass(Element classElement, NodeModuleSpi moduleSpi) {
    var name = classElement.getAttribute("name");
    var spi = mock(ES6ClassSpi.class);
    when(spi.name()).thenReturn(name);
    var result = new ES6ClassImplementor(spi);
    when(spi.api()).thenReturn(result);
    when(spi.withoutTypeArguments()).thenReturn(spi);
    when(spi.containingModule()).thenReturn(moduleSpi);

    var fields = Xml.childElementsWithTagName(classElement, TAG_NAME_FIELD).stream()
        .map(f -> createField(f, moduleSpi))
        .collect(toList());
    when(spi.fields()).thenReturn(fields);

    var superClassRef = Xml.firstChildElement(classElement, TAG_NAME_SUPER_CLASS)
        .flatMap(sc -> Xml.firstChildElement(sc, TAG_NAME_REF));
    when(spi.superClass()).thenAnswer(invocation -> superClassRef.map(ref -> resolveRefSpi(ref, moduleSpi, ES6ClassSpi.class)));

    createContainingFile(classElement, spi);

    return spi;
  }

  private static FieldSpi createField(Element fieldElement, NodeModuleSpi moduleSpi) {
    var name = fieldElement.getAttribute("name");
    var spi = mock(FieldSpi.class);
    when(spi.name()).thenReturn(name);
    var result = new FieldImplementor(spi);
    when(spi.api()).thenReturn(result);
    when(spi.containingModule()).thenReturn(moduleSpi);

    createDataType(fieldElement, spi, moduleSpi);
    createContainingFile(fieldElement, spi);

    return spi;
  }

  private static SimpleEntry<NodeElementSpi, List<String>> createExport(Element exportElement, NodeModuleSpi moduleSpi) {
    var exportName = exportElement.getAttribute("name");
    var referencedElement = Xml.firstChildElement(exportElement, TAG_NAME_CLASS)
        .<NodeElementSpi> map(c -> createClass(c, moduleSpi))
        .or(() -> Xml.firstChildElement(exportElement, TAG_NAME_FUNCTION)
            .map(f -> createFunction(f, moduleSpi)))
        .orElseThrow(); // currently only classes can be exported and must be present
    return new SimpleEntry<>(referencedElement, Collections.singletonList(exportName));
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private static void createDataType(Element dataTypeOwnerElement, DataTypeOwnerSpi spi, NodeModuleSpi moduleSpi) {
    var dataTypeElement = Xml.firstChildElement(dataTypeOwnerElement, TAG_NAME_DATA_TYPE);

    dataTypeElement
        .flatMap(sc -> Xml.firstChildElement(sc, TAG_NAME_REF))
        .ifPresent(ref -> when(spi.dataType()).thenAnswer(invocation -> resolveRefSpi(ref, moduleSpi, ES6ClassSpi.class)));
  }

  private static FunctionSpi createFunction(Element functionElement, NodeModuleSpi moduleSpi) {
    var name = functionElement.getAttribute("name");
    var spi = mock(FunctionSpi.class);
    when(spi.name()).thenReturn(name);
    var result = new FunctionImplementor(spi);
    when(spi.api()).thenReturn(result);
    when(spi.containingModule()).thenReturn(moduleSpi);

    var resultingObjectLiteral = Xml.firstChildElement(functionElement, TAG_NAME_OBJECT_LITERAL)
        .map(ol -> createObjectLiteral(ol, moduleSpi));
    when(spi.resultingObjectLiteral()).thenReturn(resultingObjectLiteral);

    createContainingFile(functionElement, spi);

    return spi;
  }

  private static ObjectLiteralSpi createObjectLiteral(Element objectLiteralElement, NodeModuleSpi moduleSpi) {
    var spi = mock(ObjectLiteralSpi.class);
    when(spi.name()).thenReturn("");
    var result = new ObjectLiteralImplementor(spi);
    when(spi.api()).thenReturn(result);
    when(spi.containingModule()).thenReturn(moduleSpi);

    var properties = Xml.childElementsWithTagName(objectLiteralElement, TAG_NAME_PROPERTY).stream()
        .flatMap(prop -> Xml.firstChildElement(prop, TAG_NAME_CONSTANT_VALUE)
            .map(cv -> TestingNodeModulesProviderSpi.createConstantValue(cv, moduleSpi))
            .map(cv -> new SimpleEntry<>(prop.getAttribute("name"), cv)).stream())
        .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> b, LinkedHashMap::new));
    when(spi.properties()).thenReturn(properties);

    createContainingFile(objectLiteralElement, spi);

    return spi;
  }

  private static void createContainingFile(Element nodeElement, NodeElementSpi spi) {
    var containingFile = Strings.notBlank(nodeElement.getAttribute(TAG_NAME_FILE))
        .map(Path::of);

    when(spi.containingFile()).thenReturn(containingFile);
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private static IConstantValue createConstantValue(Element constantValueElement, NodeModuleSpi moduleSpi) {
    var constantValue = mock(IConstantValue.class);

    var type = Xml.firstChildElement(constantValueElement, TAG_NAME_TYPE)
        .map(Node::getTextContent)
        .map(ConstantValueType::valueOf)
        .orElse(ConstantValueType.Unknown);
    when(constantValue.type()).thenReturn(type);

    var valueElement = Xml.firstChildElement(constantValueElement, TAG_NAME_VALUE).orElseThrow();

    var objectLiteralElement = Optional.of(type)
        .filter(ConstantValueType.ObjectLiteral::equals)
        .map(t -> Xml.firstChildElement(valueElement, TAG_NAME_OBJECT_LITERAL).orElseThrow());
    when(constantValue.asObjectLiteral()).thenAnswer(invocation -> objectLiteralElement
        .map(ol -> createObjectLiteral(ol, moduleSpi))
        .map(ObjectLiteralSpi::api));

    when(constantValue.asBoolean()).thenReturn(Optional.of(type)
        .filter(ConstantValueType.Boolean::equals)
        .map(t -> Boolean.valueOf(valueElement.getTextContent())));

    when(constantValue.asBigDecimal()).thenReturn(Optional.of(type)
        .filter(ConstantValueType.Numeric::equals)
        .map(t -> new BigDecimal(valueElement.getTextContent())));

    when(constantValue.asString()).thenReturn(Optional.of(type)
        .filter(ConstantValueType.String::equals)
        .map(t -> valueElement.getTextContent()));

    var es6ClassRef = Optional.of(type)
        .filter(ConstantValueType.ES6Class::equals)
        .map(t -> Xml.firstChildElement(valueElement, TAG_NAME_REF).orElseThrow());
    when(constantValue.asES6Class()).thenAnswer(invocation -> es6ClassRef
        .map(ref -> resolveRef(ref, moduleSpi.api(), IES6Class.class)));

    var constantValueElements = Optional.of(type)
        .filter(ConstantValueType.Array::equals)
        .map(t -> Xml.childElementsWithTagName(valueElement, TAG_NAME_CONSTANT_VALUE));
    when(constantValue.asArray()).thenAnswer(invocation -> constantValueElements
        .map(cvs -> cvs.stream()
            .map(cv -> createConstantValue(cv, moduleSpi))
            .toArray(IConstantValue[]::new)));

    return constantValue;
  }

  private static <T extends NodeElementSpi> T resolveRefSpi(Element refElement, NodeModuleSpi moduleSpi, Class<T> expectedType) {
    return Optional.of(resolveRef(refElement, moduleSpi.api(), INodeElement.class))
        .map(INodeElement::spi)
        .filter(expectedType::isInstance)
        .map(expectedType::cast)
        .orElseThrow();
  }

  private static <T extends INodeElement> T resolveRef(Element refElement, INodeModule module, Class<T> expectedType) {
    var refModuleName = refElement.getAttribute("module");
    var refModule = module.name().equals(refModuleName) ? module : module.packageJson().dependency(refModuleName).orElseThrow();
    return refModule.export(refElement.getAttribute("name"))
        .filter(expectedType::isInstance)
        .map(expectedType::cast)
        .orElseThrow();
  }
}
