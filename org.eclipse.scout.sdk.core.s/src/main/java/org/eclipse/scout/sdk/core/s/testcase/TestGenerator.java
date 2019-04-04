/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.testcase;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.CommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link TestGenerator}</h3>
 *
 * @since 5.2.0
 */
public class TestGenerator<TYPE extends TestGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_runner;
  private String m_session;
  private ISourceGenerator<IExpressionBuilder<?>> m_runWithSubjectValueGenerator;
  private boolean m_isClientTest;

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {

    mainType
        .withField(new TodoCommentBuilder())
        .withAnnotation(ScoutAnnotationGenerator.createRunWithSubject(runWithSubjectValueGenerator()));

    // @RunWith
    runner()
        .map(ScoutAnnotationGenerator::createRunWith)
        .ifPresent(mainType::withAnnotation);

    // @RunWithSession
    if (isClientTest()) {
      mainType
          .withAnnotation(ScoutAnnotationGenerator
              .createRunWithClientSession(session()
                  .orElse(null)));
    }
    else {
      session()
          .map(ScoutAnnotationGenerator::createRunWithServerSession)
          .ifPresent(mainType::withAnnotation);
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
    return currentInstance();
  }

  public Optional<String> session() {
    return Strings.notBlank(m_session);
  }

  public TYPE withSession(String session) {
    m_session = session;
    return currentInstance();
  }

  public boolean isClientTest() {
    return m_isClientTest;
  }

  public TYPE asClientTest(boolean isClientTest) {
    m_isClientTest = isClientTest;
    return currentInstance();
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
    return currentInstance();
  }
}
