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
package org.eclipse.scout.sdk.core.generator;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.ClassWithMembers;
import org.eclipse.scout.sdk.core.fixture.InterfaceWithDefaultMethods;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.SdkAssertions;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SourceModelRoundtripTest}</h3>
 *
 * @since 5.1.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class SourceModelRoundtripTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/";

  @Test
  public void testMembersOfSourceClass(IJavaEnvironment env) {
    var type = env.requireType(ClassWithMembers.class.getName());
    assertEqualsRefFile("ClassWithMembers_source.txt", type, env);
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentBinaryOnlyFactory.class)
  public void testMembersOfBinaryClass(IJavaEnvironment env) {
    var type = env.requireType(ClassWithMembers.class.getName());
    testApiOfClassWithMembers(type); // do a structural compare instead of a ref-file compare because depending on compiler the result (order) of the members might be slightly different
  }

  @Test
  public void testInterfaceWithDefaultMethods(IJavaEnvironment env) {
    var type = env.requireType(InterfaceWithDefaultMethods.class.getName());
    assertEqualsRefFile("InterfaceWithDefaultMethods_source.txt", type, env);
  }

  protected static void assertEqualsRefFile(String refFileName, IType type, IJavaEnvironment env) {
    SdkAssertions.assertEqualsRefFile(env, REF_FILE_FOLDER + refFileName, type.requireCompilationUnit().toWorkingCopy());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfClassWithMembers(IType classWithMembers) {
    assertHasFlags(classWithMembers, Flags.AccPublic);
    assertHasSuperClass(classWithMembers, "java.lang.Object");
    assertEquals(0, classWithMembers.annotations().stream().count(), "annotation count");

    // fields of ClassWithMembers
    assertEquals(15, classWithMembers.fields().stream().count(), "field count of 'org.eclipse.scout.sdk.core.fixture.ClassWithMembers'");
    var a1 = assertFieldExist(classWithMembers, "a1");
    assertHasFlags(a1, Flags.AccDefault);
    assertFieldType(a1, "java.lang.String");
    assertEquals(0, a1.annotations().stream().count(), "annotation count");
    var a2 = assertFieldExist(classWithMembers, "a2");
    assertHasFlags(a2, Flags.AccPublic);
    assertFieldType(a2, "java.lang.String");
    assertEquals(0, a2.annotations().stream().count(), "annotation count");
    var a3 = assertFieldExist(classWithMembers, "a3");
    assertHasFlags(a3, Flags.AccProtected);
    assertFieldType(a3, "java.lang.String");
    assertEquals(0, a3.annotations().stream().count(), "annotation count");
    var a4 = assertFieldExist(classWithMembers, "a4");
    assertHasFlags(a4, Flags.AccPrivate);
    assertFieldType(a4, "java.lang.String");
    assertEquals(0, a4.annotations().stream().count(), "annotation count");
    var a5 = assertFieldExist(classWithMembers, "a5");
    assertHasFlags(a5, Flags.AccPrivate | Flags.AccFinal);
    assertFieldType(a5, "java.lang.String");
    assertEquals(0, a5.annotations().stream().count(), "annotation count");
    var b1 = assertFieldExist(classWithMembers, "b1");
    assertHasFlags(b1, Flags.AccPrivate | Flags.AccStatic);
    assertFieldType(b1, "java.lang.String");
    assertEquals(0, b1.annotations().stream().count(), "annotation count");
    var b2 = assertFieldExist(classWithMembers, "b2");
    assertHasFlags(b2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(b2, "java.lang.String");
    assertEquals(0, b2.annotations().stream().count(), "annotation count");
    var b3 = assertFieldExist(classWithMembers, "b3");
    assertHasFlags(b3, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(b3, "java.lang.String");
    assertEquals(0, b3.annotations().stream().count(), "annotation count");
    var c1 = assertFieldExist(classWithMembers, "c1");
    assertHasFlags(c1, Flags.AccPrivate);
    assertFieldType(c1, "java.lang.String");
    assertEquals(0, c1.annotations().stream().count(), "annotation count");
    var c2 = assertFieldExist(classWithMembers, "c2");
    assertHasFlags(c2, Flags.AccPrivate);
    assertFieldType(c2, "java.lang.String");
    assertEquals(0, c2.annotations().stream().count(), "annotation count");
    var c3 = assertFieldExist(classWithMembers, "c3");
    assertHasFlags(c3, Flags.AccPrivate);
    assertFieldType(c3, "java.lang.String");
    assertEquals(0, c3.annotations().stream().count(), "annotation count");
    var c4 = assertFieldExist(classWithMembers, "c4");
    assertHasFlags(c4, Flags.AccPrivate);
    assertFieldType(c4, "java.lang.String");
    assertEquals(0, c4.annotations().stream().count(), "annotation count");
    var d1 = assertFieldExist(classWithMembers, "d1");
    assertHasFlags(d1, Flags.AccPrivate);
    assertFieldType(d1, "int");
    assertEquals(0, d1.annotations().stream().count(), "annotation count");
    var d2 = assertFieldExist(classWithMembers, "d2");
    assertHasFlags(d2, Flags.AccPrivate);
    assertFieldType(d2, "int");
    assertEquals(0, d2.annotations().stream().count(), "annotation count");
    var e1 = assertFieldExist(classWithMembers, "e1");
    assertHasFlags(e1, Flags.AccPrivate);
    assertFieldType(e1, "java.util.Map<T,java.lang.String>");
    assertEquals(0, e1.annotations().stream().count(), "annotation count");

    assertEquals(4, classWithMembers.methods().stream().count(), "method count of 'org.eclipse.scout.sdk.core.fixture.ClassWithMembers'");
    var classWithMembers1 = assertMethodExist(classWithMembers, "ClassWithMembers");
    assertTrue(classWithMembers1.isConstructor());
    assertEquals(0, classWithMembers1.annotations().stream().count(), "annotation count");
    var m1 = assertMethodExist(classWithMembers, "m1", new String[]{"java.lang.String"});
    assertMethodReturnType(m1, "java.lang.String");
    assertEquals(1, m1.annotations().stream().count(), "annotation count");
    assertAnnotation(m1, "org.eclipse.scout.sdk.core.fixture.TestAnnotation");
    var m2 = assertMethodExist(classWithMembers, "m2", new String[]{"java.lang.Class<T>"});
    assertMethodReturnType(m2, "T");
    assertEquals(1, m2.annotations().stream().count(), "annotation count");
    assertAnnotation(m2, "org.eclipse.scout.sdk.core.fixture.TestAnnotation");
    var m3 = assertMethodExist(classWithMembers, "m3", new String[]{"java.lang.Class<U>", "V", "T"});
    assertMethodReturnType(m3, "U");
    assertEquals(1, m3.annotations().stream().count(), "annotation count");
    assertAnnotation(m3, "org.eclipse.scout.sdk.core.fixture.TestAnnotation");

    assertEquals(2, classWithMembers.innerTypes().stream().count(), "inner types count of 'ClassWithMembers'");
    // type InnerStaticClass
    var innerStaticClass = assertTypeExists(classWithMembers, "InnerStaticClass");
    assertHasFlags(innerStaticClass, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(innerStaticClass, "java.lang.Object");
    assertEquals(0, innerStaticClass.annotations().stream().count(), "annotation count");

    // fields of InnerStaticClass
    assertEquals(0, innerStaticClass.fields().stream().count(), "field count of 'org.eclipse.scout.sdk.core.fixture.ClassWithMembers$InnerStaticClass'");

    assertEquals(1, innerStaticClass.methods().stream().count(), "method count of 'org.eclipse.scout.sdk.core.fixture.ClassWithMembers$InnerStaticClass'");
    var innerStaticClass1 = assertMethodExist(innerStaticClass, "InnerStaticClass");
    assertTrue(innerStaticClass1.isConstructor());
    assertEquals(0, innerStaticClass1.annotations().stream().count(), "annotation count");

    assertEquals(0, innerStaticClass.innerTypes().stream().count(), "inner types count of 'InnerStaticClass'");
    // type InnerMemberClass
    var innerMemberClass = assertTypeExists(classWithMembers, "InnerMemberClass");
    assertHasFlags(innerMemberClass, Flags.AccPublic);
    assertHasSuperClass(innerMemberClass, "java.lang.Object");
    assertEquals(0, innerMemberClass.annotations().stream().count(), "annotation count");

    // fields of InnerMemberClass
    assertEquals(0, innerMemberClass.fields().stream().count(), "field count of 'org.eclipse.scout.sdk.core.fixture.ClassWithMembers$InnerMemberClass'");

    assertEquals(1, innerMemberClass.methods().stream().count(), "method count of 'org.eclipse.scout.sdk.core.fixture.ClassWithMembers$InnerMemberClass'");
    var innerMemberClass1 = assertMethodExist(innerMemberClass, "InnerMemberClass");
    assertTrue(innerMemberClass1.isConstructor());
    assertEquals(0, innerMemberClass1.annotations().stream().count(), "annotation count");

    assertEquals(0, innerMemberClass.innerTypes().stream().count(), "inner types count of 'InnerMemberClass'");
  }
}
