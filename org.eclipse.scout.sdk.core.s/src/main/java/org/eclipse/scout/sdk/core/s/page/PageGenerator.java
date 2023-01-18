/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.page;

import static org.eclipse.scout.sdk.core.java.model.api.Flags.isAbstract;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.java.builder.body.MethodBodyBuilder;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.builder.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.java.builder.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.java.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link PageGenerator}</h3>
 *
 * @since 5.2.0
 */
public class PageGenerator<TYPE extends PageGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  public static final String INNER_TABLE_NAME = ISdkConstants.INNER_TABLE_TYPE_NAME;
  public static final String EXEC_LOAD_DATA_FILTER_ARG_NAME = "filter";
  private static final String TABLE_TYPE_PARAM_NAME = "T";

  private boolean m_isPageWithTable;
  private boolean m_isLeaf = true;
  private boolean m_createLeafMethod = true;
  private boolean m_createNlsMethod = true;
  private String m_pageData;
  private String m_pageServiceIfc;
  private String m_classIdValue;
  private String m_tableClassIdValue;
  private String m_dataFetchMethodName;
  private String m_tableSuperType;
  private Function<String, ISourceGenerator<IScoutMethodBodyBuilder<?>>> m_pageSvcIfcToBodySrc;

  @Override
  protected void setup() {
    this
        .withSuperClassFunc(buildSuperTypeAndAppendTable())
        .withAnnotation(classIdValue()
            .map(ScoutAnnotationGenerator::createClassId)
            .orElse(null));

    if (isAbstract(flags())) {
      if (isPageWithTable()) {
        appendTypeParameter();
      }
    }
    else {
      if (isCreateLeafMethod()) {
        withMethod(MethodGenerator.create()
            .withAnnotation(AnnotationGenerator.createOverride())
            .asProtected()
            .withReturnType(JavaTypes._boolean)
            .withElementNameFrom(IScoutApi.class, api -> api.AbstractTreeNode().getConfiguredLeafMethodName())
            .withBody(b -> b
                .returnClause()
                .append(isLeaf())
                .semicolon())

        );
      }
      if (isPageWithTable()) {
        withMethod(createExecLoadData());
      }
      else {
        var variableName = "pageList";
        withMethod(MethodGenerator.create()
            .asProtected()
            .withReturnType(JavaTypes._void)
            .withElementNameFrom(IScoutApi.class, api -> api.AbstractPageWithNodes().execCreateChildPagesMethodName())
            .withAnnotation(AnnotationGenerator.createOverride())
            .withParameter(MethodParameterGenerator.create()
                .withDataTypeFrom(IScoutApi.class, api -> List.class.getName() + JavaTypes.C_GENERIC_START + api.IPage().fqn() +
                    JavaTypes.C_GENERIC_START + JavaTypes.C_QUESTION_MARK + JavaTypes.C_GENERIC_END + JavaTypes.C_GENERIC_END)
                .withElementName(variableName))
            .withBody(IMethodBodyBuilder::appendSuperCall));
      }
    }

    if (isCreateNlsMethod()) {
      withMethod(ScoutMethodGenerator.createNlsMethodFrom(IScoutApi.class, api -> api.AbstractPage().getConfiguredTitleMethodName(), Strings.capitalize(elementName().orElseThrow())));
    }
  }

  protected void appendTypeParameter() {
    var typeParamBoundary = fullyQualifiedName()
        + JavaTypes.C_GENERIC_START
        + TABLE_TYPE_PARAM_NAME
        + JavaTypes.C_GENERIC_END + JavaTypes.C_DOT
        + INNER_TABLE_NAME;

    withTypeParameter(
        TypeParameterGenerator.create()
            .withElementName(TABLE_TYPE_PARAM_NAME)
            .withBinding(typeParamBoundary));
  }

  protected IMethodGenerator<?, ?> createExecLoadData() {
    var execLoadData = MethodGenerator.create()
        .asProtected()
        .withReturnType(JavaTypes._void)
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractPageWithTable().execLoadDataMethodName())
        .withParameter(MethodParameterGenerator.create()
            .withElementName(EXEC_LOAD_DATA_FILTER_ARG_NAME)
            .withDataTypeFrom(IScoutApi.class, api -> api.SearchFilter().fqn()))
        .withAnnotation(AnnotationGenerator.createOverride());

    execLoadData.withBody(body -> body.append(
        pageServiceInterface()
            .map(execLoadBodyGenerator()
                .orElseGet(() -> ifc -> b -> b.appendFrom(IScoutApi.class, api -> api.AbstractPageWithTable().importPageDataMethodName()).parenthesisOpen().appendBeansGet(ifc)
                    .dot().append(dataFetchMethodName()).parenthesisOpen().append(EXEC_LOAD_DATA_FILTER_ARG_NAME).parenthesisClose().parenthesisClose().semicolon()))
            .map(g -> g.generalize(inner -> ScoutMethodBodyBuilder.create(inner, execLoadData)))
            .orElseGet(
                () -> b -> MethodBodyBuilder.create(b, execLoadData).appendTodo("implement data load")
                    .appendSingleLineComment("e.g.: importPageData(BEANS.get(IMyService.class).getTableData(" + EXEC_LOAD_DATA_FILTER_ARG_NAME + "))"))));
    return execLoadData;
  }

  protected Function<IJavaBuilderContext, String> buildSuperTypeAndAppendTable() {
    if (isPageWithTable()) {
      var tableBuilder = createTableBuilder();
      withType(tableBuilder)
          .withAnnotation(pageData()
              .map(ScoutAnnotationGenerator::createData)
              .orElse(null));
      return buildPageWithTableSuperType(tableBuilder);
    }
    var predefinedSuperClass = superClassFunc();
    return c -> predefinedSuperClass
        .map(f -> f.apply(c))
        .orElseGet(() -> c.requireApi(IScoutApi.class).AbstractPageWithNodes().fqn());
  }

  protected Function<IJavaBuilderContext, String> buildPageWithTableSuperType(ITypeGenerator<?> tableBuilder) {
    var predefinedSuperClass = superClassFunc();
    return c -> {
      var existingSuperClassFqn = predefinedSuperClass.map(af -> af.apply(c)).orElseGet(() -> c.requireApi(IScoutApi.class).AbstractPageWithTable().fqn());
      var superTypeBuilder = new StringBuilder(existingSuperClassFqn).append(JavaTypes.C_GENERIC_START);
      if (isAbstract(flags())) {
        superTypeBuilder.append(TABLE_TYPE_PARAM_NAME);
      }
      else {
        superTypeBuilder.append(tableBuilder.fullyQualifiedName());
      }
      superTypeBuilder.append(JavaTypes.C_GENERIC_END);
      return superTypeBuilder.toString();
    };
  }

  protected ITypeGenerator<?> createTableBuilder() {
    return TypeGenerator.create()
        .asPublic()
        .withElementName(INNER_TABLE_NAME)
        .withSuperClassFrom(IScoutApi.class, api -> tableSuperType().orElse(api.AbstractTable().fqn()))
        .withAnnotation(tableClassIdValue()
            .map(ScoutAnnotationGenerator::createClassId)
            .orElse(null));
  }

  public boolean isPageWithTable() {
    return m_isPageWithTable;
  }

  public TYPE asPageWithTable(boolean isPageWithTable) {
    m_isPageWithTable = isPageWithTable;
    return thisInstance();
  }

  public boolean isLeaf() {
    return m_isLeaf;
  }

  public TYPE asLeaf(boolean isLeaf) {
    m_isLeaf = isLeaf;
    return thisInstance();
  }

  public boolean isCreateLeafMethod() {
    return m_createLeafMethod;
  }

  public TYPE withLeafMethod(boolean createLeafMethod) {
    m_createLeafMethod = createLeafMethod;
    return thisInstance();
  }

  public Optional<String> pageData() {
    return Strings.notBlank(m_pageData);
  }

  public TYPE withPageData(String pageData) {
    m_pageData = pageData;
    return thisInstance();
  }

  public Optional<String> classIdValue() {
    return Strings.notBlank(m_classIdValue);
  }

  public TYPE withClassIdValue(String classIdValue) {
    m_classIdValue = classIdValue;
    return thisInstance();
  }

  public Optional<String> tableClassIdValue() {
    return Strings.notBlank(m_tableClassIdValue);
  }

  public TYPE withTableClassIdValue(String tableClassIdValue) {
    m_tableClassIdValue = tableClassIdValue;
    return thisInstance();
  }

  public Optional<String> pageServiceInterface() {
    return Strings.notBlank(m_pageServiceIfc);
  }

  public TYPE withPageServiceInterface(String pageServiceIfc) {
    m_pageServiceIfc = pageServiceIfc;
    return thisInstance();
  }

  public String dataFetchMethodName() {
    return m_dataFetchMethodName;
  }

  public TYPE withDataFetchMethodName(String dataFetchMethodName) {
    m_dataFetchMethodName = dataFetchMethodName;
    return thisInstance();
  }

  public Optional<String> tableSuperType() {
    return Strings.notBlank(m_tableSuperType);
  }

  public TYPE withTableSuperType(String tableSuperType) {
    m_tableSuperType = tableSuperType;
    return thisInstance();
  }

  public boolean isCreateNlsMethod() {
    return m_createNlsMethod;
  }

  public TYPE withNlsMethod(boolean createNlsMethod) {
    m_createNlsMethod = createNlsMethod;
    return thisInstance();
  }

  public Optional<Function<String, ISourceGenerator<IScoutMethodBodyBuilder<?>>>> execLoadBodyGenerator() {
    return Optional.ofNullable(m_pageSvcIfcToBodySrc);
  }

  public TYPE withExecLoadBodyGenerator(Function<String, ISourceGenerator<IScoutMethodBodyBuilder<?>>> pageSvcIfcToBodySrc) {
    m_pageSvcIfcToBodySrc = pageSvcIfcToBodySrc;
    return thisInstance();
  }
}
