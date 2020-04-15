/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.page;

import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.MethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.builder.java.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.builder.java.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link PageGenerator}</h3>
 *
 * @since 5.2.0
 */
public class PageGenerator<TYPE extends PageGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  public static final String INNER_TABLE_NAME = "Table";
  public static final String EXEC_LOAD_DATA_FILTER_ARG_NAME = "filter";
  private static final String TABLE_TYPE_PARAM_NAME = "T";

  private boolean m_isPageWithTable;
  private boolean m_createNlsMethod = true;
  private String m_pageData;
  private String m_pageServiceIfc;
  private String m_classIdValue;
  private String m_tableClassIdValue;
  private String m_dataFetchMethodName;
  private String m_tableSuperType;
  private Function<String, ISourceGenerator<IScoutMethodBodyBuilder<?>>> m_pageSvcIfcToBodySrc;

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType
        .withSuperClass(buildSuperTypeAndAppendTable())
        .withAnnotation(classIdValue()
            .map(ScoutAnnotationGenerator::createClassId)
            .orElse(null));

    if (isAbstract(flags())) {
      if (isPageWithTable()) {
        appendTypeParameter();
      }
    }
    else {
      if (isPageWithTable()) {
        mainType.withMethod(createExecLoadData());
      }
      else {
        mainType.withMethod(MethodOverrideGenerator.createOverride()
            .withElementName("execCreateChildPages"));
      }
    }

    if (isCreateNlsMethod()) {
      mainType
          .withMethod(ScoutMethodGenerator.createNlsMethod("getConfiguredTitle", Strings.ensureStartWithUpperCase(elementName().get())));
    }
  }

  protected void appendTypeParameter() {
    String typeParamBoundary = fullyQualifiedName()
        + JavaTypes.C_GENERIC_START
        + TABLE_TYPE_PARAM_NAME
        + ">."
        + INNER_TABLE_NAME;

    withTypeParameter(
        TypeParameterGenerator.create()
            .withElementName(TABLE_TYPE_PARAM_NAME)
            .withBound(typeParamBoundary));
  }

  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createExecLoadData() {
    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> execLoadData = MethodGenerator.create()
        .asProtected()
        .withReturnType(JavaTypes._void)
        .withElementName("execLoadData")
        .withParameter(MethodParameterGenerator.create()
            .withElementName("filter")
            .withDataType(IScoutRuntimeTypes.SearchFilter))
        .withAnnotation(AnnotationGenerator.createOverride());

    execLoadData.withBody(body -> body.append(
        pageServiceInterface()
            .map(execLoadBodyGenerator()
                .orElseGet(() -> ifc -> b -> b.append("importPageData").parenthesisOpen().appendBeansGet(ifc)
                    .dot().append(dataFetchMethodName()).parenthesisOpen().append(EXEC_LOAD_DATA_FILTER_ARG_NAME).parenthesisClose().parenthesisClose().semicolon()))
            .map(g -> g.generalize(inner -> ScoutMethodBodyBuilder.create(inner, execLoadData)))
            .orElseGet(
                () -> b -> MethodBodyBuilder.create(b, execLoadData).appendTodo("implement data load")
                    .appendSingleLineComment("e.g.: importPageData(BEANS.get(IMyService.class).getTableData(" + EXEC_LOAD_DATA_FILTER_ARG_NAME + "))"))));
    return execLoadData;
  }

  protected String buildSuperTypeAndAppendTable() {
    if (isPageWithTable()) {
      ITypeGenerator<?> tableBuilder = createTableBuilder();

      withType(tableBuilder)
          .withAnnotation(pageData()
              .map(ScoutAnnotationGenerator::createData)
              .orElse(null));

      String existingSuperClassFqn = superClass().orElse(IScoutRuntimeTypes.AbstractPageWithTable);
      StringBuilder superTypeBuilder = new StringBuilder(existingSuperClassFqn)
          .append(JavaTypes.C_GENERIC_START);
      if (isAbstract(flags())) {
        superTypeBuilder.append(TABLE_TYPE_PARAM_NAME);
      }
      else {
        superTypeBuilder.append(tableBuilder.fullyQualifiedName());
      }
      superTypeBuilder.append(JavaTypes.C_GENERIC_END);
      return superTypeBuilder.toString();
    }

    return superClass().orElse(IScoutRuntimeTypes.AbstractPageWithNodes);
  }

  protected ITypeGenerator<?> createTableBuilder() {
    return TypeGenerator.create()
        .asPublic()
        .withElementName(INNER_TABLE_NAME)
        .withSuperClass(tableSuperType().orElse(IScoutRuntimeTypes.AbstractTable))
        .withAnnotation(tableClassIdValue()
            .map(ScoutAnnotationGenerator::createClassId)
            .orElse(null));
  }

  public boolean isPageWithTable() {
    return m_isPageWithTable;
  }

  public TYPE asPageWithTable(boolean isPageWithTable) {
    m_isPageWithTable = isPageWithTable;
    return currentInstance();
  }

  public Optional<String> pageData() {
    return Strings.notBlank(m_pageData);
  }

  public TYPE withPageData(String pageData) {
    m_pageData = pageData;
    return currentInstance();
  }

  public Optional<String> classIdValue() {
    return Strings.notBlank(m_classIdValue);
  }

  public TYPE withClassIdValue(String classIdValue) {
    m_classIdValue = classIdValue;
    return currentInstance();
  }

  public Optional<String> tableClassIdValue() {
    return Strings.notBlank(m_tableClassIdValue);
  }

  public TYPE withTableClassIdValue(String tableClassIdValue) {
    m_tableClassIdValue = tableClassIdValue;
    return currentInstance();
  }

  public Optional<String> pageServiceInterface() {
    return Strings.notBlank(m_pageServiceIfc);
  }

  public TYPE withPageServiceInterface(String pageServiceIfc) {
    m_pageServiceIfc = pageServiceIfc;
    return currentInstance();
  }

  public String dataFetchMethodName() {
    return m_dataFetchMethodName;
  }

  public TYPE withDataFetchMethodName(String dataFetchMethodName) {
    m_dataFetchMethodName = dataFetchMethodName;
    return currentInstance();
  }

  public Optional<String> tableSuperType() {
    return Strings.notBlank(m_tableSuperType);
  }

  public TYPE withTableSuperType(String tableSuperType) {
    m_tableSuperType = tableSuperType;
    return currentInstance();
  }

  public boolean isCreateNlsMethod() {
    return m_createNlsMethod;
  }

  public TYPE withNlsMethod(boolean createNlsMethod) {
    m_createNlsMethod = createNlsMethod;
    return currentInstance();
  }

  public Optional<Function<String, ISourceGenerator<IScoutMethodBodyBuilder<?>>>> execLoadBodyGenerator() {
    return Optional.ofNullable(m_pageSvcIfcToBodySrc);
  }

  public TYPE withExecLoadBodyGenerator(Function<String, ISourceGenerator<IScoutMethodBodyBuilder<?>>> pageSvcIfcToBodySrc) {
    m_pageSvcIfcToBodySrc = pageSvcIfcToBodySrc;
    return currentInstance();
  }
}
