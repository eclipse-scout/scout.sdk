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
package org.eclipse.scout.sdk.core.generator.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link AnnotationGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(UsernameExtension.class)
public class AnnotationGeneratorTest {

  @Test
  public void testMultiValueAnnotation() {
    IAnnotationGenerator<?> generator = AnnotationGenerator.create()
        .withElementName("scout.test.TestAnnotation")
        .withElement("value", "4")
        .withElement("second", b -> b.append(10.0f))
        .withElement("third", b -> b.append(false))
        .withElement("fourth", b -> b.append(100.11))
        .withElement("fifth", b -> b.append(100L))
        .withElement("sixth", b -> b.append(134L))
        .withComment(b -> b.appendTodo("nothing"))
        .withoutElement("sixth");

    assertEquals(5, generator.elements().size());
    assertFalse(generator.element("notExisting").isPresent());
    assertTrue(generator.element("second").isPresent());

    assertEquals("// TODO [anonymous] nothing\n@TestAnnotation(value = 4,\nsecond = 10.0,\nthird = false,\nfourth = 100.11,\nfifth = 100)", generator.toJavaSource().toString());
  }

  @Test
  public void testOverride() {
    assertEquals("@Override", AnnotationGenerator.createOverride().toJavaSource().toString());
  }

  @Test
  public void testGenerated() {
    assertEquals("@Generated(value = \"Generator\", comments = \"This class is auto generated. No manual modifications recommended.\")", AnnotationGenerator.createGenerated("Generator").toJavaSource().toString());
    assertEquals("@Generated(value = \"Generator\", comments = \"Test\\\"Comment\")", AnnotationGenerator.createGenerated("Generator", "Test\"Comment").toJavaSource().toString());
  }

  @Test
  public void testDeprecated() {
    assertEquals("@Deprecated", AnnotationGenerator.createDeprecated().toJavaSource().toString());
  }

  @Test
  public void testSuppressWarnings() {
    assertEquals("@SuppressWarnings({\"checked\", \"all\"})", AnnotationGenerator.createSupressWarnings("checked", "all").toJavaSource().toString());
    assertEquals("@SuppressWarnings(\"checked\")", AnnotationGenerator.createSupressWarnings("checked").toJavaSource().toString());
  }

  @Test
  public void testNoValueAnnotation() {
    String src = AnnotationGenerator.create()
        .withElementName("scout.test.TestAnnotation")
        .withoutElement("third")
        .toJavaSource()
        .toString();

    assertEquals("@TestAnnotation", src);
  }

  @Test
  public void testSingleValueAnnotationSpecialName() {
    String src = AnnotationGenerator.create()
        .withElementName("scout.test.TestAnnotation")
        .withElement("special", "4")
        .toJavaSource()
        .toString();

    assertEquals("@TestAnnotation(special = 4)", src);
  }

  @Test
  public void testSingleValueAnnotation() {
    String src = AnnotationGenerator.create()
        .withElementName("scout.test.TestAnnotation")
        .withElement("value", "4")
        .toJavaSource()
        .toString();

    assertEquals("@TestAnnotation(4)", src);
  }
}
