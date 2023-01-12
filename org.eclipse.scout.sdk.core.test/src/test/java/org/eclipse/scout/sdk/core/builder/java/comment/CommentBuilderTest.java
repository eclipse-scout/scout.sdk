/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.builder.java.comment;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.util.JavaTypes.arrayMarker;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

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
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
  public void testAppendLink(IJavaEnvironment env) {
    assertJavaElementCommentEquals("", b -> b.appendLink((CharSequence) null), env);
    assertJavaElementCommentEquals("", b -> b.appendLink((IType) null), env);
    assertJavaElementCommentEquals("", b -> b.appendLink(""), env);

    assertJavaElementCommentEquals("{@link " + BaseClass.class.getSimpleName() + '}', BaseClass.class.getName(),
        b -> b.appendLink(env.requireType(BaseClass.class.getName())), env);
    assertJavaElementCommentEquals("{@link " + BaseClass.class.getSimpleName() + '}', BaseClass.class.getName(),
        b -> b.appendLink(BaseClass.class.getName()), env);
    assertJavaElementCommentEquals(JavaTypes._boolean, b -> b.appendLink(env.requireType(JavaTypes._boolean)), env);
    assertJavaElementCommentEquals(JavaTypes._void, b -> b.appendLink(env.requireType(JavaTypes._void)), env);
    assertJavaElementCommentEquals(List.class.getSimpleName() + arrayMarker(), List.class.getName(),
        b -> b.appendLink(env.requireType(List.class.getName() + arrayMarker())), env);
    assertJavaElementCommentEquals("{@link #localMethod()}",
        b -> b.appendLink("#localMethod()"), env);
    assertJavaElementCommentEquals("{@link #localMethod() label}",
        b -> b.appendLink("#localMethod()", "label"), env);
    assertJavaElementCommentEquals("{@link #localMethod(List)}", List.class.getName(),
        b -> b.appendLink("#localMethod(" + List.class.getName() + ")"), env);
    assertJavaElementCommentEquals("{@link List#addAll(int, Collection) label}", Set.of(List.class.getName(), Collection.class.getName()),
        b -> b.appendLink(List.class.getName() + "#addAll(int, " + Collection.class.getName() + ")", " label"), env);
    assertJavaElementCommentEquals("{@link List#size() label with multiple words}", List.class.getName(),
        b -> b.appendLink(List.class.getName() + "#size()", "label with multiple words"), env);

    // test that generics are removed (not supported by @link)
    assertJavaElementCommentEquals("{@link List#addAll(int, Collection) label}", Set.of(List.class.getName(), Collection.class.getName()),
        b -> b.appendLink(List.class.getName() + "#addAll(int, " + Collection.class.getName() + "<" + BigInteger.class.getName() + ">)", " label"), env);

    // test that for each type a @link is created
    var refWithTypeArguments = Map.class.getName() + JavaTypes.C_GENERIC_START + Integer.class.getName() + ", " +
        List.class.getName() + JavaTypes.C_GENERIC_START + BigDecimal.class.getName() + arrayMarker() + JavaTypes.C_GENERIC_END + JavaTypes.C_GENERIC_END;
    assertJavaElementCommentEquals("{@link Map}<{@link Integer},{@link List}<{@link BigDecimal}[]>>",
        Set.of(BigDecimal.class.getName(), Map.class.getName(), List.class.getName(), Integer.class.getName()),
        b -> b.appendLink(refWithTypeArguments), env);
  }

  protected static void assertJavaElementCommentEquals(String expectedSrc, Consumer<IJavaElementCommentBuilder<?>> task, IJavaEnvironment env) {
    assertJavaElementCommentEquals(expectedSrc, emptySet(), task, env);
  }

  protected static void assertJavaElementCommentEquals(String expectedSrc, String expectedImport, Consumer<IJavaElementCommentBuilder<?>> task, IJavaEnvironment env) {
    assertJavaElementCommentEquals(expectedSrc, singleton(expectedImport), task, env);
  }

  protected static void assertJavaElementCommentEquals(String expectedSrc, Set<String> expectedImports, Consumer<IJavaElementCommentBuilder<?>> task, IJavaEnvironment env) {
    var context = new JavaBuilderContext(env);
    var inner = MemorySourceBuilder.create(context);
    IJavaElementCommentBuilder<?> builder = new JavaElementCommentBuilder<>(inner, () -> null);

    task.accept(builder);

    var imports = context.validator()
        .importCollector()
        .getImports()
        .map(StringBuilder::toString)
        .collect(toSet());
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
