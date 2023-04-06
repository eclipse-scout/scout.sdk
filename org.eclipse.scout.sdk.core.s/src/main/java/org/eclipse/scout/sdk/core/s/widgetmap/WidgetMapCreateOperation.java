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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.typescript.IWebConstants;
import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

public class WidgetMapCreateOperation {

  // in 
  private IObjectLiteral m_literal;
  private boolean m_isPage;

  // out
  private List<CharSequence> m_classSources;
  private Map<String /* widgetMap declaration field name */, CharSequence /* field declaration source */> m_declarationSources;
  private List<IES6Class> m_importsForModel;
  private List<String> m_importNamesForDeclarations;

  public void execute() {
    validateOperation();

    var modelName = literal().containingFile()
        .map(Path::getFileName)
        .map(Path::toString)
        .map(filename -> Strings.removeSuffix(filename, IWebConstants.TS_FILE_SUFFIX))
        .orElseThrow(() -> Ensure.newFail("Model name can not be detected."));

    Stream<INodeElementGenerator<?>> generators;

    if (isPage()) {
      Map<String, CharSequence> declarations = new HashMap<>();
      List<String> declarationImports = new ArrayList<>();

      generators = Stream.concat(
          IdObjectTypeMapUtils.createDetailFormGeneratorForPage(modelName, literal()).stream()
              .peek(gen -> {
                var detailForm = gen.objectType().flatMap(ObjectType::newClassName).orElseThrow();
                declarations.put(ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_FORM, createFieldDeclaration(ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_FORM, detailForm)
                    .toTypeScriptSource());
                declarationImports.add(detailForm);
              })
              .flatMap(gen -> Stream.concat(Stream.of(gen), IdObjectTypeMapUtils.collectAdditionalGenerators(gen))),
          IdObjectTypeMapUtils.createDetailTableGeneratorForPage(modelName, literal()).stream()
              .peek(gen -> {
                var detailPage = gen.objectType().flatMap(ObjectType::newClassName).orElseThrow();
                declarations.put(ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_TABLE, createFieldDeclaration(ScoutJsCoreConstants.PROPERTY_NAME_DETAIL_TABLE, detailPage)
                    .toTypeScriptSource());
                declarationImports.add(detailPage);
              })
              .flatMap(gen -> Stream.concat(Stream.of(gen), IdObjectTypeMapUtils.collectAdditionalGenerators(gen))));

      setDeclarationSources(declarations);
      setImportNamesForDeclarations(declarationImports);
    }
    else {
      generators = IdObjectTypeMapUtils.createWidgetMapGenerator(modelName, literal()).stream()
          .peek(gen -> {
            var widgetMap = gen.map().orElseThrow().name();
            setDeclarationSources(Stream.of(ScoutJsCoreConstants.PROPERTY_NAME_WIDGET_MAP)
                .collect(Collectors.toMap(Function.identity(), prop -> createFieldDeclaration(prop, widgetMap)
                    .toTypeScriptSource())));
            setImportNamesForDeclarations(List.of(widgetMap));
          })
          .flatMap(gen -> Stream.concat(Stream.of(gen), IdObjectTypeMapUtils.collectAdditionalGenerators(gen)));
    }

    setClassSources(generators
        .map(ITypeScriptElementGenerator::toTypeScriptSource)
        .collect(Collectors.toList()));

    // TODO fsh imports
    setImportsForModel(Collections.emptyList());
  }

  protected void validateOperation() {
    Ensure.notNull(literal(), "No object-literal provided.");
  }

  protected static IFieldGenerator<?> createFieldDeclaration(String name, String dataType) {
    return FieldGenerator.create()
        .withElementName(name)
        .withModifier(Modifier.DECLARE)
        .withDataType(dataType);
  }

  public IObjectLiteral literal() {
    return m_literal;
  }

  public void setLiteral(IObjectLiteral literal) {
    m_literal = literal;
  }

  public boolean isPage() {
    return m_isPage;
  }

  public void setPage(boolean page) {
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

  public List<IES6Class> importsForModel() {
    return m_importsForModel;
  }

  protected void setImportsForModel(List<IES6Class> importsForModel) {
    m_importsForModel = importsForModel;
  }

  public List<String> importNamesForDeclarations() {
    return m_importNamesForDeclarations;
  }

  protected void setImportNamesForDeclarations(List<String> importNamesForDeclarations) {
    m_importNamesForDeclarations = importNamesForDeclarations;
  }
}
