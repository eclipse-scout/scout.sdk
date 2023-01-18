/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.field;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link FieldGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class FieldGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/java/generator/field/";

  @Test
  public void testField(IJavaEnvironment env) {
    var generator = FieldGenerator.create()
        .asVolatile()
        .asPrivate()
        .asTransient()
        .withElementName("m_map")
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withAnnotation(AnnotationGenerator.createDeprecated())
        .withDataType("java.util.Map<java.lang.String, java.lang.Integer>")
        .withValue(IExpressionBuilder::nullLiteral);

    assertEqualsRefFile(env, REF_FILE_FOLDER + "FieldGeneratorTest1.txt", generator);
  }

  @Test
  public void testFieldWithoutValue() {
    var src = FieldGenerator.create()
        .asFinal()
        .asProtected()
        .asStatic()
        .withElementName("m_member")
        .withDataType(JavaTypes._int)
        .toJavaSource()
        .toString();

    assertEquals("protected static final int m_member;", src);
  }

  @Test
  public void testStaticConstructor() {
    var constrSrc = "static {\nint a = 4;\n}";
    var src = FieldGenerator.create()
        .withValue(b -> b.append(constrSrc))
        .toJavaSource()
        .toString();
    assertEquals(constrSrc, src);
    assertEquals("", FieldGenerator.create().toJavaSource().toString());
  }

  @Test
  public void testSerialVersionUid() {
    assertEquals("private static final long serialVersionUID = 1L;", FieldGenerator.createSerialVersionUid().toJavaSource().toString());
    assertEquals("private static final long serialVersionUID = 1234L;", FieldGenerator.createSerialVersionUid(1234).toJavaSource().toString());
  }
}
