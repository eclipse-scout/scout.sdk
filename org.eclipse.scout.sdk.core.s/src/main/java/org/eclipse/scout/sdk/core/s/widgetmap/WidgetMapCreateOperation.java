/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.typescript.IWebConstants;
import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptBuilderContext;
import org.eclipse.scout.sdk.core.typescript.builder.TypeScriptBuilderContext;
import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportCollector.ES6ImportDescriptor;
import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

public class WidgetMapCreateOperation {

  // in 
  private IObjectLiteral m_literal;
  private IES6Class m_mainWidget;
  private String m_modelName;
  private boolean m_isPage;

  // out
  private List<CharSequence> m_classSources;
  private Map<String /* widgetMap declaration field name */, CharSequence /* field declaration source */> m_declarationSources;
  private List<ES6ImportDescriptor> m_importsForModel;
  private List<ES6ImportDescriptor> m_importNamesForDeclarations;

  public void execute() {
    validateOperation();
    prepareOperation();
    executeOperation();
  }

  protected void validateOperation() {
    Ensure.notNull(literal(), "No object-literal provided.");
  }

  protected void prepareOperation() {
    setModelName(calculateModelName());
    setPage(calculatePage());
  }

  protected void executeOperation() {
    Stream<INodeElementGenerator<?>> generators;
    Map<String, String> declarations = new HashMap<>();
    if (isPage()) {
      var detailFormGenerator = IdObjectTypeMapUtils.createDetailFormGeneratorForPage(modelName(), literal())
          .map(gen -> {
            var detailForm = gen.objectType().flatMap(ObjectType::newClassName).orElseThrow();
            declarations.put(ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_FORM, detailForm);
            return Stream.concat(Stream.of(gen), IdObjectTypeMapUtils.collectAdditionalGenerators(gen));
          }).orElseGet(Stream::empty);
      var detailTableGenerator = IdObjectTypeMapUtils.createDetailTableGeneratorForPage(modelName(), literal())
          .map(gen -> {
            var detailPage = gen.objectType().flatMap(ObjectType::newClassName).orElseThrow();
            declarations.put(ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_TABLE, detailPage);
            return Stream.concat(Stream.of(gen), IdObjectTypeMapUtils.collectAdditionalGenerators(gen));
          }).orElseGet(Stream::empty);
      generators = Stream.concat(detailFormGenerator, detailTableGenerator);
    }
    else {
      generators = IdObjectTypeMapUtils.createWidgetMapGenerator(modelName(), literal(), mainWidget())
          .map(gen -> {
            var widgetMap = gen.map().orElseThrow();
            declarations.put(ScoutJsCoreConstants.PROPERTY_NAME_WIDGET_MAP, widgetMap.name());
            return Stream.concat(Stream.of(gen), IdObjectTypeMapUtils.collectAdditionalGenerators(gen));
          }).orElseGet(Stream::empty);
    }

    var widgetMapSourcesAndImports = executeGenerators(generators);
    setClassSources(new ArrayList<>(widgetMapSourcesAndImports.getKey().values()));
    var modelImports = widgetMapSourcesAndImports.getValue();
    setImportsForModel(modelImports);

    buildDeclarations(declarations);
  }

  protected void buildDeclarations(Map<String, String> declarationInfo) {
    var declarationGenerators = declarationInfo.entrySet().stream()
        .map(e -> createFieldDeclaration(e.getKey(), literal().createDataType(e.getValue())));
    var declarationSourceAndImports = executeGenerators(declarationGenerators);
    setDeclarationSources(declarationSourceAndImports.getKey());
    setImportNamesForDeclarations(declarationSourceAndImports.getValue());
  }

  protected static SimpleEntry<Map<String, CharSequence>, List<ES6ImportDescriptor>> executeGenerators(Stream<? extends INodeElementGenerator<?>> generators) {
    var builderContext = new TypeScriptBuilderContext(new BuilderContext());
    var declarationSources = new LinkedHashMap<String, CharSequence>();
    var imports = new LinkedHashSet<ES6ImportDescriptor>();
    generators.forEach(g -> {
      var sourceAndImports = runGenerator(g, builderContext);
      imports.addAll(sourceAndImports.getValue());
      declarationSources.put(g.elementName().orElse(null), sourceAndImports.getKey());
    });
    return new SimpleEntry<>(declarationSources, new ArrayList<>(imports));
  }

  protected static SimpleEntry<CharSequence, Collection<ES6ImportDescriptor>> runGenerator(ITypeScriptElementGenerator<?> generator, ITypeScriptBuilderContext context) {
    var src = generator.toTypeScriptSource(context);
    return new SimpleEntry<>(src, context.importValidator().importCollector().imports());
  }

  protected static IFieldGenerator<?> createFieldDeclaration(String name, IDataType dataType) {
    return FieldGenerator.create()
        .withElementName(name)
        .withModifier(Modifier.DECLARE)
        .withDataType(dataType);
  }

  protected String calculateModelName() {
    return literal().containingFile()
        .map(Path::getFileName)
        .map(Path::toString)
        .map(filename -> Strings.removeSuffix(filename, IWebConstants.TS_FILE_SUFFIX))
        .orElseThrow(() -> Ensure.newFail("Model name can not be detected."));
  }

  protected boolean calculatePage() {
    if (mainWidget() != null) {
      return mainWidget()
          .supers()
          .withSuperInterfaces(false)
          .stream()
          .anyMatch(es6Class -> ScoutJsCoreConstants.CLASS_NAME_PAGE.equals(es6Class.name()));
    }

    // try to detect based on model name
    return modelName().endsWith(ScoutJsCoreConstants.CLASS_NAME_PAGE)
        || modelName().endsWith(ScoutJsCoreConstants.CLASS_NAME_PAGE + ScoutJsCoreConstants.MODEL_SUFFIX)
        || modelName().contains("PageWithTable")
        || modelName().contains("PageWithNodes")
        || modelName().endsWith("TablePage" + ScoutJsCoreConstants.MODEL_SUFFIX)
        || modelName().endsWith("NodePage" + ScoutJsCoreConstants.MODEL_SUFFIX);
  }

  public IObjectLiteral literal() {
    return m_literal;
  }

  public void setLiteral(IObjectLiteral literal) {
    m_literal = literal;
  }

  public IES6Class mainWidget() {
    return m_mainWidget;
  }

  public void setMainWidget(IES6Class mainWidget) {
    m_mainWidget = mainWidget;
  }

  public String modelName() {
    return m_modelName;
  }

  protected void setModelName(String modelName) {
    m_modelName = modelName;
  }

  public boolean isPage() {
    return m_isPage;
  }

  protected void setPage(boolean page) {
    m_isPage = page;
  }

  public List<CharSequence> classSources() {
    return m_classSources;
  }

  protected void setClassSources(List<CharSequence> classSources) {
    m_classSources = classSources;
  }

  public Map<String, CharSequence> declarationSources() {
    return m_declarationSources;
  }

  protected void setDeclarationSources(Map<String, CharSequence> declarationSources) {
    m_declarationSources = declarationSources;
  }

  public List<ES6ImportDescriptor> importsForModel() {
    return m_importsForModel;
  }

  protected void setImportsForModel(List<ES6ImportDescriptor> importsForModel) {
    m_importsForModel = importsForModel;
  }

  public List<ES6ImportDescriptor> importNamesForDeclarations() {
    return m_importNamesForDeclarations;
  }

  protected void setImportNamesForDeclarations(List<ES6ImportDescriptor> importNamesForDeclarations) {
    m_importNamesForDeclarations = importNamesForDeclarations;
  }
}
