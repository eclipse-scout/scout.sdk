/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java.comment;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link CommentBuilderTest}</h3>
 *
 * @since 7.0.0
 */
public class CommentBuilderTest {
  @Test
  public void testJavaDoc() {
    assertJavaDocEquals("/**\n* a\n* b\n*/\n", "a\nb");
    assertJavaDocEquals("/**\n* a\n* b\n*/\n", "a\n* b");
    assertJavaDocEquals("/**\n* abc\n*/\n", "/**\nabc\n**/");
    assertJavaDocEquals("/**\n* abc\n*/\n", "/*\nabc\n*/");
  }

  @Test
  public void testBlockComment() {
    assertCommentEquals("/*\n* a\n* b\n*/\n", "a\nb");
    assertCommentEquals("/*\n* a\n* b\n*/\n", "a\n* b");
    assertCommentEquals("/*\n* abc\n*/\n", "/**\nabc\n**/");
    assertCommentEquals("/*\n* abc\n*/\n", "/*\nabc\n*/");
  }

  @Test
  @ExtendWith(JavaEnvironmentExtension.class)
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
  public void testAppendLink(IJavaEnvironment env) {
    assertJavaElementCommentEquals("{@link}", "", b -> b.appendLink((CharSequence) null), env);
    assertJavaElementCommentEquals("{@link}", "", b -> b.appendLink((IType) null), env);
    assertJavaElementCommentEquals("{@link " + BaseClass.class.getSimpleName() + '}', "import " + BaseClass.class.getName() + ';',
        b -> b.appendLink(env.requireType(BaseClass.class.getName())), env);
    assertJavaElementCommentEquals("{@link}", "", b -> b.appendLink(""), env);
    assertJavaElementCommentEquals("{@link " + BaseClass.class.getSimpleName() + '}', "import " + BaseClass.class.getName() + ';',
        b -> b.appendLink(BaseClass.class.getName()), env);
    assertJavaElementCommentEquals(JavaTypes._boolean, "", b -> b.appendLink(env.requireType(JavaTypes._boolean)), env);
    assertJavaElementCommentEquals(JavaTypes._void, "", b -> b.appendLink(env.requireType(JavaTypes._void)), env);
    assertJavaElementCommentEquals(String.class.getSimpleName() + "[]", "", b -> b.appendLink(env.requireType(String.class.getName() + "[]")), env);
  }

  protected static void assertJavaElementCommentEquals(String expectedSrc, String expectedImports, Consumer<IJavaElementCommentBuilder<?>> task, IJavaEnvironment env) {
    var context = new JavaBuilderContext(env);
    var inner = MemorySourceBuilder.create(context);
    IJavaElementCommentBuilder<?> builder = new JavaElementCommentBuilder<>(inner, () -> null);

    task.accept(builder);

    var imports = context.validator()
        .importCollector()
        .createImportDeclarations()
        .collect(joining(", "));
    assertEquals(expectedSrc, inner.source().toString());
    assertEquals(expectedImports, imports);
  }

  protected static void assertJavaDocEquals(String expected, String input) {
    var inner = MemorySourceBuilder.create();
    new CommentBuilder<>(inner).appendJavaDocComment(input);
    assertEquals(expected, inner.source().toString());
  }

  protected static void assertCommentEquals(String expected, String input) {
    var inner = MemorySourceBuilder.create();
    new CommentBuilder<>(inner).appendBlockComment(input);
    assertEquals(expected, inner.source().toString());
  }
}
