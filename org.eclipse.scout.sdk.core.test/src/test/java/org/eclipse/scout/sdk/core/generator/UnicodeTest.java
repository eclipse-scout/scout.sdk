/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.generator;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.fixture.ClassWithUnicode;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link UnicodeTest}</h3>
 *
 * @since 9.0.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class UnicodeTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/";

  @Test
  public void testWithUnicodeClass(IJavaEnvironment env) {
    IType type = env.requireType(ClassWithUnicode.class.getName());
    testApiOfClassWithUnicode(type);

    assertEquals(ClassWithUnicode.UNICODE, type.fields().first().get().constantValue().get().as(String.class));
    assertEqualsRefFile(REF_FILE_FOLDER + "MethodWithUnicode.txt", type.methods().first().get().source().get().asCharSequence().toString());
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
    assertEquals(1, classWithUnicode.fields().stream().count(), "field count of 'org.eclipse.scout.sdk.core.fixture.ClassWithUnicode'");
    IField UNICODE = assertFieldExist(classWithUnicode, "UNICODE");
    assertHasFlags(UNICODE, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(UNICODE, "java.lang.String");
    assertEquals(0, UNICODE.annotations().stream().count(), "annotation count");

    assertEquals(1, classWithUnicode.methods().stream().count(), "method count of 'org.eclipse.scout.sdk.core.fixture.ClassWithUnicode'");
    IMethod getValue = assertMethodExist(classWithUnicode, "getValue", new String[]{});
    assertMethodReturnType(getValue, "java.lang.String");
    assertEquals(0, getValue.annotations().stream().count(), "annotation count");

    assertEquals(0, classWithUnicode.innerTypes().stream().count(), "inner types count of 'ClassWithUnicode'");
  }
}
