/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import static org.eclipse.scout.sdk.core.java.testing.CoreJavaTestingUtils.registerCompilationUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.RoundingMode;

import javax.annotation.Generated;

import org.eclipse.scout.sdk.core.java.fixture.ClassWithAnnotationConstants;
import org.eclipse.scout.sdk.core.java.fixture.managed.AnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.java.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * This Test verifies that no null annotation elements are processed by the model. According to the JLS annotation
 * elements can never be null. While this is true for <b>valid</b> Java code this must not necessarily be the case for
 * Java files under development. During development, it may happen that invalid java files are saved. In this case the
 * corresponding annotation elements should just be ignored instead of throwing an exception like NPE.
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationWithCompileErrorTest {

  @Test
  public void testAnnotationsWithInvalidArrayValue(IJavaEnvironment env) {
    var className = "ClassWithCompileError";
    var pck = "test";
    var testClass = "package " + pck + ";\n\n" +
        "@" + Generated.class.getName() + "(null)\n" +
        "public class " + className + " {}\n";

    var type = registerCompilationUnit(env, testClass, pck, className);
    assertFalse(env.compileErrors(type).isEmpty());

    var generatedValue = type.annotations().withManagedWrapper(GeneratedAnnotation.class).first().orElseThrow().value();
    assertEquals(0, generatedValue.length);
  }

  @Test
  public void testAnnotationsWithInvalidManagedArrayValue(IJavaEnvironment env) {
    var className = "ClassWithCompileError2";
    var pck = "test";
    var testClass = "package " + pck + ";\n\n" +
        "import " + org.eclipse.scout.sdk.core.java.fixture.AnnotationWithArrayValues.class.getName() + ";\n" +
        "import " + ClassWithAnnotationConstants.class.getName() + ";\n" +
        "import " + org.eclipse.scout.sdk.core.java.fixture.AnnotationWithSingleValues.class.getName() + ";\n" +
        "import " + Generated.class.getName() + ";\n" +
        "import " + RoundingMode.class.getName() + ";\n\n" +
        "@AnnotationWithArrayValues(\n" +
        "      nums = {21, 22},\n" +
        "      enumValues = {RoundingMode.HALF_EVEN, RoundingMode.HALF_EVEN},\n" +
        "      strings = {\"testle\", ClassWithAnnotationConstants.GAMMA},\n" +
        "      types = {Float.class, Float.class},\n" +
        "      annos = {\n" +
        "          @AnnotationWithSingleValues(type = Double.class, enumValue = RoundingMode.HALF_EVEN, num = 31, string = \"asdf\"),\n" + // <-- compile error here: anno element is mandatory
        "          @AnnotationWithSingleValues(type = Double.class, enumValue = RoundingMode.HALF_EVEN, num = 32, string = ClassWithAnnotationConstants.DELTA, anno = @Generated(\"gen\")}),\n" + // <-- compile error here (curly brace at the end)
        "      })\n" +
        "public class " + className + " {}\n";

    var type = registerCompilationUnit(env, testClass, pck, className);
    assertFalse(env.compileErrors(type).isEmpty());

    var nestedAnnotations = type.annotations().withManagedWrapper(AnnotationWithArrayValues.class).first().orElseThrow().annos();
    assertEquals(0, nestedAnnotations.length);
  }
}
