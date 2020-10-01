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
package org.eclipse.scout.sdk.core.generator.field;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link FieldGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class FieldGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/field/";

  @Test
  public void testField(IJavaEnvironment env) {
    IFieldGenerator<?> generator = FieldGenerator.create()
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
    String src = FieldGenerator.create()
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
    String constrSrc = "static {\nint a = 4;\n}";
    String src = FieldGenerator.create()
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
