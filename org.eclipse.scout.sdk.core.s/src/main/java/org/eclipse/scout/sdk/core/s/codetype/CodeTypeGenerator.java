/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.codetype;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    classIdValue()
        .map(ScoutAnnotationGenerator::createClassId)
        .ifPresent(mainType::withAnnotation);

    mainType
        .withField(FieldGenerator.createSerialVersionUid())
        .withField(createId())
        .withMethod(createIdGetter());
  }

  protected static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createIdGetter() {
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
