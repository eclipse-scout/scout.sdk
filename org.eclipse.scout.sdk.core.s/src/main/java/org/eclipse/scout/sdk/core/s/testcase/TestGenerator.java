/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testcase;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.CommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link TestGenerator}</h3>
 *
 * @since 5.2.0
 */
public class TestGenerator<TYPE extends TestGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_runner;
  private ApiFunction<?, ITypeNameSupplier> m_session;
  private ISourceGenerator<IExpressionBuilder<?>> m_runWithSubjectValueGenerator;
  private boolean m_isClientTest;

  @Override
  protected void setup() {
    this
        .withField(new TodoCommentBuilder())
        .withAnnotation(ScoutAnnotationGenerator.createRunWithSubject(runWithSubjectValueGenerator()));

    // @RunWith
    runner()
        .map(ScoutAnnotationGenerator::createRunWith)
        .ifPresent(this::withAnnotation);

    // @RunWithSession
    if (isClientTest()) {
      withAnnotation(ScoutAnnotationGenerator.createRunWithClientSession(session().orElse(null)));
    }
    else {
      session()
          .map(ScoutAnnotationGenerator::createRunWithServerSession)
          .ifPresent(this::withAnnotation);
    }
  }

  private static final class TodoCommentBuilder extends FieldGenerator<TodoCommentBuilder> {

    private TodoCommentBuilder() {
      withElementName("todo");
    }

    @Override
    protected void build(IJavaSourceBuilder<?> builder) {
      CommentBuilder.create(builder).appendTodo("add test cases");
    }
  }

  public Optional<String> runner() {
    return Strings.notBlank(m_runner);
  }

  public TYPE withRunner(String runner) {
    m_runner = runner;
    return thisInstance();
  }

  public Optional<ApiFunction<?, ITypeNameSupplier>> session() {
    return Optional.ofNullable(m_session);
  }

  public TYPE withSession(CharSequence session) {
    return withSessionFrom(null, api -> ITypeNameSupplier.of(session));
  }

  public <API extends IApiSpecification> TYPE withSessionFrom(Class<API> apiSpec, Function<API, ITypeNameSupplier> sessionFunction) {
    if (sessionFunction == null) {
      m_session = null;
    }
    else {
      m_session = new ApiFunction<>(apiSpec, sessionFunction);
    }
    return thisInstance();
  }

  public boolean isClientTest() {
    return m_isClientTest;
  }

  public TYPE asClientTest(boolean isClientTest) {
    m_isClientTest = isClientTest;
    return thisInstance();
  }

  public ISourceGenerator<IExpressionBuilder<?>> runWithSubjectValueGenerator() {
    return m_runWithSubjectValueGenerator;
  }

  /**
   * Sets an optional {@link ISourceGenerator} for creating the value of the {@code @RunWithSubject} annotation value.
   *
   * @param runWithSubjectValueGenerator
   *          the builder or {@code null}.
   */
  public TYPE withRunWithSubjectValueBuilder(ISourceGenerator<IExpressionBuilder<?>> runWithSubjectValueGenerator) {
    m_runWithSubjectValueGenerator = runWithSubjectValueGenerator;
    return thisInstance();
  }
}
