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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.TypeScriptTypes;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue.ConstantValueType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FunctionImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.NodeModuleImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ObjectLiteralImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.PackageJsonImplementor;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.TypeOfImplementor;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeOwnerSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.TypeOfSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestingNodeModulesProviderSpi implements NodeModulesProviderSpi {
  public static final String TAG_NAME_FILE = "file";
  public static final String TAG_NAME_MODULE = "module";
  public static final String TAG_NAME_EXPORT = "export";
  public static final String TAG_NAME_INDEX = "index";
  public static final String TAG_NAME_REF = "ref";
  public static final String TAG_NAME_FIELD = "field";
  public static final String TAG_NAME_MODIFIER = "modifier";
  public static final String TAG_NAME_DATA_TYPE = "dataType";
  public static final String TAG_NAME_CLASS = "class";
  public static final String TAG_NAME_SUPER_CLASS = "superClass";
  public static final String TAG_NAME_TYPE_ALIAS = "typeAlias";
  public static final String TAG_NAME_TYPE_ARGUMENT = "typeArgument";
  public static final String TAG_NAME_TYPE_OF = "typeOf";
  public static final String TAG_NAME_FUNCTION = "function";
  public static final String TAG_NAME_OBJECT_LITERAL = "objectLiteral";
  public static final String TAG_NAME_PROPERTY = "property";
  public static final String TAG_NAME_CONSTANT_VALUE = "constantValue";
  public static final String TAG_NAME_TYPE = "type";
  public static final String TAG_NAME_VALUE = "value";

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
    var moduleSpi = createNodeModule(name, version, index);
    var dependencies = subModules.stream()
        .map(this::getOrCreateModule)
        .flatMap(Optional::stream)
        .collect(toCollection(LinkedHashSet::new));
    var packageJsonSpi = moduleSpi.packageJson();
    when(packageJsonSpi.dependencies()).thenReturn(dependencies);
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

  public static NodeModuleSpi createNodeModule(String name, String version, CharSequence index) {
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
    when(moduleSpi.nodeElementFactory()).thenReturn(new TestingNodeElementFactorySpi(moduleSpi));
    when(packageJsonSpi.containingModule()).thenReturn(moduleSpi);

    return moduleSpi;
  }

  private static ES6ClassSpi createClass(Element classElement, NodeModuleSpi moduleSpi) {
    var name = classElement.getAttribute("name");
    var spi = mock(ES6ClassSpi.class);
    when(spi.name()).thenReturn(name);
    var result = new ES6ClassImplementor(spi);
    when(spi.api()).thenReturn(result);
    when(spi.flavor()).thenReturn(DataTypeFlavor.Single);
    when(spi.withoutTypeArguments()).thenReturn(spi);
    when(spi.containingModule()).thenReturn(moduleSpi);
    when(spi.createDataType(anyString())).thenAnswer(invocation -> {
      String dataTypeName = invocation.getArgument(0);
      var factory = (TestingNodeElementFactorySpi) moduleSpi.nodeElementFactory();
      return factory.getOrCreateDataTypeSpi(dataTypeName);
    });
    var isEnum = Boolean.parseBoolean(classElement.getAttribute("enum"));
    when(spi.isEnum()).thenReturn(isEnum);
    var typeAliasElement = Xml.firstChildElement(classElement, TAG_NAME_TYPE_ALIAS).orElse(null);
    when(spi.isTypeAlias()).thenReturn(typeAliasElement != null);
    when(spi.aliasedDataType()).thenAnswer(invocation -> Optional.ofNullable(resolveAliasedType(typeAliasElement, moduleSpi)));

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

    var modifiers = Xml.childElementsWithTagName(fieldElement, TAG_NAME_MODIFIER).stream()
        .map(TestingNodeModulesProviderSpi::createModifier)
        .collect(toSet());
    when(spi.hasModifier(any(Modifier.class))).thenAnswer(invocation -> modifiers.contains(invocation.<Modifier> getArgument(0)));

    var constantValue = Xml.firstChildElement(fieldElement, TAG_NAME_CONSTANT_VALUE)
        .map(cv -> createConstantValue(cv, moduleSpi, name))
        .orElse(null);
    when(spi.constantValue()).thenReturn(constantValue);
    if (constantValue != null) {
      when(spi.dataType()).thenAnswer(invocation -> constantValue.dataType().map(IDataType::spi).orElse(null));
    }
    else {
      createDataType(fieldElement, spi, moduleSpi);
    }
    createContainingFile(fieldElement, spi);

    return spi;
  }

  private static Modifier createModifier(Element modifierElement) {
    return Modifier.valueOf(modifierElement.getAttribute("name"));
  }

  private static SimpleEntry<NodeElementSpi, Set<String>> createExport(Element exportElement, NodeModuleSpi moduleSpi) {
    var exportName = exportElement.getAttribute("name");
    var referencedElement = Xml.firstChildElement(exportElement, TAG_NAME_CLASS)
        .<NodeElementSpi> map(c -> createClass(c, moduleSpi))
        .or(() -> Xml.firstChildElement(exportElement, TAG_NAME_FUNCTION)
            .map(f -> createFunction(f, moduleSpi)))
        .orElseThrow(); // currently only classes can be exported and must be present
    return new SimpleEntry<>(referencedElement, Collections.singleton(exportName));
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
    when(spi.createDataType(anyString())).thenAnswer(invocation -> {
      String name = invocation.getArgument(0);
      var factory = (TestingNodeElementFactorySpi) moduleSpi.nodeElementFactory();
      return factory.getOrCreateDataTypeSpi(name);
    });

    var properties = Xml.childElementsWithTagName(objectLiteralElement, TAG_NAME_PROPERTY).stream()
        .flatMap(prop -> Xml.firstChildElement(prop, TAG_NAME_CONSTANT_VALUE)
            .map(cv -> {
              var name = prop.getAttribute("name");
              return new SimpleEntry<>(name, createConstantValue(cv, moduleSpi, name));
            }).stream())
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
  private static IConstantValue createConstantValue(Element constantValueElement, NodeModuleSpi moduleSpi, String parentName) {
    var constantValue = mock(IConstantValue.class);

    var type = Xml.firstChildElement(constantValueElement, TAG_NAME_TYPE)
        .map(Node::getTextContent)
        .map(ConstantValueType::valueOf)
        .orElse(ConstantValueType.Unknown);
    when(constantValue.type()).thenReturn(type);

    when(constantValue.dataType()).thenAnswer(invocation -> Optional.ofNullable(switch (type) {
      case Boolean -> ((TestingNodeElementFactorySpi) moduleSpi.nodeElementFactory()).getOrCreateDataTypeSpi(TypeScriptTypes._boolean);
      case Numeric -> ((TestingNodeElementFactorySpi) moduleSpi.nodeElementFactory()).getOrCreateDataTypeSpi(TypeScriptTypes._number);
      case String -> ((TestingNodeElementFactorySpi) moduleSpi.nodeElementFactory()).getOrCreateDataTypeSpi(TypeScriptTypes._string);
      case ObjectLiteral -> moduleSpi.nodeElementFactory().createObjectLiteralDataType(parentName, constantValue.asObjectLiteral().map(IObjectLiteral::spi).orElse(null));
      default -> null;
    }).map(DataTypeSpi::api));

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
            .map(cv -> createConstantValue(cv, moduleSpi, parentName))
            .toArray(IConstantValue[]::new)));

    return constantValue;
  }

  private static <T> T resolveRefSpi(Element refElement, NodeModuleSpi moduleSpi, Class<T> expectedType) {
    return Optional.of(resolveRef(refElement, moduleSpi.api(), INodeElement.class))
        .map(INodeElement::spi)
        .filter(expectedType::isInstance)
        .map(expectedType::cast)
        .orElseThrow();
  }

  private static <T> T resolveRef(Element refElement, INodeModule module, Class<T> expectedType) {
    var refModuleName = refElement.getAttribute("module");
    var refModule = module.name().equals(refModuleName) ? module : module.packageJson().dependency(refModuleName).orElseThrow();
    var refFieldElement = Xml.firstChildElement(refElement, TAG_NAME_FIELD).orElse(null);
    var typeArguments = Xml.childElementsWithTagName(refElement, TAG_NAME_TYPE_ARGUMENT).stream()
        .map(e -> resolveTypeArgument(e, module.spi()))
        .filter(Objects::nonNull)
        .map(DataTypeSpi::api)
        .collect(toList());
    return refModule.export(refElement.getAttribute("name"))
        .map(export -> {
          if (export instanceof IES6Class es6Class && refFieldElement != null) {
            return es6Class.field(refFieldElement.getAttribute("name")).orElse(null);
          }
          if (export instanceof IES6Class es6Class && !typeArguments.isEmpty()) {
            return module.nodeElementFactory().createClassWithTypeArguments(es6Class, typeArguments);
          }
          return export;
        })
        .filter(expectedType::isInstance)
        .map(expectedType::cast)
        .orElseThrow();
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private static DataTypeSpi resolveAliasedType(Element typeAliasElement, NodeModuleSpi moduleSpi) {
    if (typeAliasElement == null) {
      return null;
    }

    var refElement = Xml.firstChildElement(typeAliasElement, TAG_NAME_REF).orElse(null);
    if (refElement != null) {
      return resolveRefSpi(refElement, moduleSpi, DataTypeSpi.class);
    }

    return null;
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private static DataTypeSpi resolveTypeArgument(Element typeArgumentElement, NodeModuleSpi moduleSpi) {
    if (typeArgumentElement == null) {
      return null;
    }

    var refElement = Xml.firstChildElement(typeArgumentElement, TAG_NAME_REF).orElse(null);
    if (refElement != null) {
      return resolveRefSpi(refElement, moduleSpi, DataTypeSpi.class);
    }

    var typeOfElement = Xml.firstChildElement(typeArgumentElement, TAG_NAME_TYPE_OF).orElse(null);
    if (typeOfElement != null) {
      return createTypeOf(typeOfElement, moduleSpi);
    }

    return null;
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private static TypeOfSpi createTypeOf(Element typeOfElement, NodeModuleSpi moduleSpi) {
    var spi = mock(TypeOfSpi.class);
    when(spi.name()).thenReturn("");
    var result = new TypeOfImplementor(spi);
    when(spi.api()).thenReturn(result);
    when(spi.containingModule()).thenReturn(moduleSpi);

    when(spi.dataTypeOwner()).thenAnswer(invocation -> resolveRefSpi(Xml.firstChildElement(typeOfElement, TAG_NAME_REF).orElseThrow(), moduleSpi, DataTypeOwnerSpi.class));

    return spi;
  }
}
