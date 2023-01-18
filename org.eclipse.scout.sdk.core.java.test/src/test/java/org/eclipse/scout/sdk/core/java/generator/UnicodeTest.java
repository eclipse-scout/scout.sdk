/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.fixture.ClassWithUnicode;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.SdkAssertions;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link UnicodeTest}</h3>
 *
 * @since 9.0.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class UnicodeTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/java/generator/";

  @Test
  public void testWithUnicodeClass(IJavaEnvironment env) {
    var type = env.requireType(ClassWithUnicode.class.getName());
    testApiOfClassWithUnicode(type);

    assertEquals(ClassWithUnicode.UNICODE, type.fields().first().orElseThrow().constantValue().orElseThrow().as(String.class));
    SdkAssertions.assertEqualsRefFile(REF_FILE_FOLDER + "MethodWithUnicode.txt", type.methods().first().orElseThrow().source().orElseThrow().asCharSequence());
    assertEqualsRefFile(env, REF_FILE_FOLDER + "ClassWithUnicode.txt", type.toWorkingCopy());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfClassWithUnicode(IType classWithUnicode) {
    assertHasFlags(classWithUnicode, Flags.AccPublic);
    assertHasSuperClass(classWithUnicode, "java.lang.Object");
    assertEquals(0, classWithUnicode.annotations().stream().count(), "annotation count");

    // fields of ClassWithUnicode
    assertEquals(1, classWithUnicode.fields().stream().count(), "field count of 'org.eclipse.scout.sdk.core.java.fixture.ClassWithUnicode'");
    var UNICODE = assertFieldExist(classWithUnicode, "UNICODE");
    assertHasFlags(UNICODE, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(UNICODE, "java.lang.String");
    assertEquals(0, UNICODE.annotations().stream().count(), "annotation count");

    assertEquals(1, classWithUnicode.methods().stream().count(), "method count of 'org.eclipse.scout.sdk.core.java.fixture.ClassWithUnicode'");
    var getValue = assertMethodExist(classWithUnicode, "getValue");
    assertMethodReturnType(getValue, "java.lang.String");
    assertEquals(0, getValue.annotations().stream().count(), "annotation count");

    assertEquals(0, classWithUnicode.innerTypes().stream().count(), "inner types count of 'ClassWithUnicode'");
  }
}
