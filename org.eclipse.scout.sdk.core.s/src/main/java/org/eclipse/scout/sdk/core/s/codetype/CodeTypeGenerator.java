/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.codetype;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link CodeTypeGenerator}</h3>
 *
 * @since 5.2.0
 */
public class CodeTypeGenerator<TYPE extends CodeTypeGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  public static final String ID_CONSTANT_NAME = "ID";

  private String m_codeTypeIdDataType;
  private String m_classIdValue;
  private ISourceGenerator<IExpressionBuilder<?>> m_idValueGenerator;

  @Override
  protected void setup() {
    classIdValue()
        .map(ScoutAnnotationGenerator::createClassId)
        .ifPresent(this::withAnnotation);
    this.withField(FieldGenerator.createSerialVersionUid())
        .withField(createId())
        .withMethod(createIdGetter());
  }

  protected static IMethodGenerator<?, ?> createIdGetter() {
    return MethodOverrideGenerator.createOverride()
        .withElementNameFrom(IScoutApi.class, api -> api.ICodeType().getIdMethodName())
        .withBody(b -> b.returnClause().append(ID_CONSTANT_NAME).semicolon());
  }

  protected IFieldGenerator<? extends IFieldGenerator<?>> createId() {
    return FieldGenerator.create()
        .asPublic()
        .asStatic()
        .asFinal()
        .withElementName(ID_CONSTANT_NAME)
        .withDataType(JavaTypes.unboxToPrimitive(codeTypeIdDataType().orElseThrow(() -> newFail("CodeType id datatype is missing."))))
        .withValue(idValueBuilder()
            .orElse(IExpressionBuilder::nullLiteral))
        .withComment(idValueBuilder()
            .<ISourceGenerator<IJavaElementCommentBuilder<?>>> map(b -> ISourceGenerator.empty())
            .orElseGet(() -> b -> b.appendTodo("set id value")));
  }

  public Optional<String> codeTypeIdDataType() {
    return Optional.ofNullable(m_codeTypeIdDataType);
  }

  public TYPE withCodeTypeIdDataType(String codeTypeIdDataType) {
    m_codeTypeIdDataType = codeTypeIdDataType;
    return thisInstance();
  }

  public Optional<ISourceGenerator<IExpressionBuilder<?>>> idValueBuilder() {
    return Optional.ofNullable(m_idValueGenerator);
  }

  public TYPE withIdValueBuilder(ISourceGenerator<IExpressionBuilder<?>> idValueGenerator) {
    m_idValueGenerator = idValueGenerator;
    return thisInstance();
  }

  public Optional<String> classIdValue() {
    return Strings.notBlank(m_classIdValue);
  }

  public TYPE withClassIdValue(String classIdValue) {
    m_classIdValue = classIdValue;
    return thisInstance();
  }
}
