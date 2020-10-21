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
package org.eclipse.scout.sdk.core.model.api;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ConstantTestClass;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class FieldTest {
  @Test
  public void testStringConstantField(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    var myStringField = childClassType.fields().first().get();
    assertNotNull(myStringField);
    assertEquals("public static final String myString = \"myStringValue\";", myStringField.toWorkingCopy().toJavaSource().toString());

    assertEquals("myStringValue", myStringField.constantValue().get().as(String.class));
    assertEquals(String.class.getName(), myStringField.dataType().name());
    assertEquals(childClassType, myStringField.requireDeclaringType());
    assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, myStringField.flags());
    assertEquals("myString", myStringField.elementName());
  }

  @Test
  public void testJavaDocOnField(IJavaEnvironment env) {
    var javaDocSrc = "/** java doc */";
    env.registerCompilationUnitOverride("abc", "JavaDocTestClass.java", "package abc;\npublic class JavaDocTestClass {\n" + javaDocSrc + "\nint a1;\n}");
    var javaDoc = env.requireType("abc.JavaDocTestClass").requireCompilationUnit().requireMainType().fields().withName("a1").first().get().javaDoc().get();
    assertEquals(javaDocSrc, javaDoc.asCharSequence().toString());
    env.reload();
    javaDoc = env.requireType("abc.JavaDocTestClass").requireCompilationUnit().requireMainType().fields().withName("a1").first().get().javaDoc().get();
    assertEquals(javaDocSrc, javaDoc.asCharSequence().toString());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    var myStringField = childClassType.fields().first().get();
    assertNotNull(myStringField);

    assertFalse(Strings.isBlank(myStringField.toString()));
  }

  @Test
  public void testNullArrayField(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    var mTestField = childClassType.fields().item(1).get();

    assertEquals(MetaValueType.Null, mTestField.constantValue().get().type());
    assertEquals(JavaTypes._int, mTestField.dataType().leafComponentType().get().name());
    assertEquals(2, mTestField.dataType().arrayDimension());
    assertEquals(childClassType, mTestField.requireDeclaringType());
    assertEquals(Flags.AccProtected | Flags.AccFinal, mTestField.flags());
    assertEquals("m_test", mTestField.elementName());
  }

  @Test
  public void testAnnotationOnFieldChild(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    var mTestField = childClassType.fields().item(1).get();

    assertEquals(1, mTestField.annotations().stream().count());
    assertEquals(mTestField, mTestField.annotations().first().get().owner());
    assertEquals(TestAnnotation.class.getName(), mTestField.annotations().first().get().type().name());
  }

  @Test
  public void testAnnotationOnFieldBase(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    var myLongField = baseClassType.fields().first().get();
    assertEquals(1, myLongField.annotations().stream().count());
    assertEquals(myLongField, myLongField.annotations().first().get().owner());
    assertEquals(TestAnnotation.class.getName(), myLongField.annotations().first().get().type().name());
  }

  @Test
  public void testLongConstantField(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    var myLongField = baseClassType.fields().first().get();
    assertEquals(JavaTypes.Long, myLongField.dataType().name());
    assertEquals(baseClassType, myLongField.requireDeclaringType());
    assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, myLongField.flags());
    assertEquals("myLong", myLongField.elementName());
  }

  @Test
  public void testConstantValues(IJavaEnvironment env) {
    var constantTestClass = env.requireType(ConstantTestClass.class.getName());
    var fields = constantTestClass.fields().stream().collect(toList());
    assertEquals(4, fields.size());
    assertFalse(fields.get(0).constantValue().isPresent());
    assertEquals(MetaValueType.String, fields.get(1).constantValue().get().type());
    assertEquals(MetaValueType.Null, fields.get(2).constantValue().get().type());
    assertFalse(fields.get(3).constantValue().isPresent());
  }

  @Test
  public void testAnonymousTypeField(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    var anonymousClassField = baseClassType.fields().item(1).get();
    assertFalse(anonymousClassField.constantValue().isPresent());
    assertEquals(Runnable.class.getName(), anonymousClassField.dataType().name());
    assertEquals(0, anonymousClassField.dataType().arrayDimension());
    assertEquals(baseClassType, anonymousClassField.requireDeclaringType());
    assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, anonymousClassField.flags());
    assertEquals("ANONYMOUS_CLASS", anonymousClassField.elementName());
  }
}
