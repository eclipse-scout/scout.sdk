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
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.RoundingMode;

import javax.annotation.Generated;

import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationConstants;
import org.eclipse.scout.sdk.core.fixture.managed.AnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This Test verifies that no null annotation elements are processed by the model. According to the JLS annotation
 * elements can never be null. While this is true for <b>valid</b> Java code this must not necessarily be the case for
 * Java files under development. During development it may happen that invalid java files are saved. In this case the
 * corresponding annotation elements should just be ignored instead of throwing an exception like NPE.
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationWithCompileErrorTest {

  @Test
  public void testAnnotationsWithInvalidArrayValue(IJavaEnvironment env) {
    var className = "ClassWithCompileError";
    var pck = "test";
    var testClass = "package " + pck + ";\n\n" +
        "@" + Generated.class.getName() + "(null)\n" +
        "public class " + className + " {}\n";

    env.registerCompilationUnitOverride(pck, className + JavaTypes.JAVA_FILE_SUFFIX, testClass);
    var type = env.requireType(pck + JavaTypes.C_DOT + className);
    assertFalse(env.compileErrors(type).isEmpty());

    var generatedValue = type.annotations().withManagedWrapper(GeneratedAnnotation.class).first().get().value();
    assertEquals(0, generatedValue.length);
  }

  @Test
  public void testAnnotationsWithInvalidManagedArrayValue(IJavaEnvironment env) {
    var className = "ClassWithCompileError2";
    var pck = "test";
    var testClass = "package " + pck + ";\n\n" +
        "import " + org.eclipse.scout.sdk.core.fixture.AnnotationWithArrayValues.class.getName() + ";\n" +
        "import " + ClassWithAnnotationConstants.class.getName() + ";\n" +
        "import " + org.eclipse.scout.sdk.core.fixture.AnnotationWithSingleValues.class.getName() + ";\n" +
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

    env.registerCompilationUnitOverride(pck, className + JavaTypes.JAVA_FILE_SUFFIX, testClass);
    var type = env.requireType(pck + JavaTypes.C_DOT + className);
    assertFalse(env.compileErrors(type).isEmpty());

    var nestedAnnotations = type.annotations().withManagedWrapper(AnnotationWithArrayValues.class).first().get().annos();
    assertEquals(0, nestedAnnotations.length);
  }
}
