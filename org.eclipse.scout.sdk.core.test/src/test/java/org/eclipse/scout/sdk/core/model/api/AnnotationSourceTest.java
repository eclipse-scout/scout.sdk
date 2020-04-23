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

import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.removeWhitespace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationConstants;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithDefaultValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithSingleValues;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.TypeImplementor;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link AnnotationSourceTest}</h3>
 * <p>
 * Used classes are {@link ClassWithAnnotationWithDefaultValues}, {@link ClassWithAnnotationWithArrayValues},
 * {@link ClassWithAnnotationWithSingleValues}, {@link ClassWithAnnotationConstants}
 *
 * @since 5.1.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationSourceTest {

  @Test
  public void testAnnotationWithDefaultValuesInSourceFile(IJavaEnvironment env) {
    IType t = env.requireType(ClassWithAnnotationWithDefaultValues.class.getName());
    assertEquals(1, t.annotations().stream().count());
    IAnnotation a = t.annotations().first().get();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "1", null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"one\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g\")", null);
  }

  @Test
  public void testAnnotationWithSingleValuesInSourceFile(IJavaEnvironment env) {
    IType t = env.requireType(ClassWithAnnotationWithSingleValues.class.getName());
    assertEquals(1, t.annotations().stream().count());
    IAnnotation a = t.annotations().first().get();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MIN_VALUE, "Integer.MIN_VALUE");
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", "RoundingMode.HALF_UP");
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", "\"alpha\"");
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", "String.class");
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g1\")", "@ValueAnnot(\"g1\")");

    IMethod m = t.methods().first().get();//the constructor is a synthetic method
    assertEquals(1, m.annotations().stream().count());
    a = m.annotations().first().get();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MAX_VALUE, "Integer.MAX_VALUE");
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", "RoundingMode.HALF_DOWN");
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", "ClassWithAnnotationConstants.ALPHA");
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", "Integer.class");
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g2\")", "@ValueAnnot(\"g2\")");
  }

  @Test
  public void testAnnotationWithArrayValuesInSourceFile(IJavaEnvironment env) {
    IType t = env.requireType(ClassWithAnnotationWithArrayValues.class.getName());
    assertEquals(1, t.annotations().stream().count());
    IAnnotation a = t.annotations().first().get();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"1", "2"}, "{1, 2}");
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_UP", "RoundingMode.HALF_DOWN"}, "{RoundingMode.HALF_UP, RoundingMode.HALF_DOWN}");
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"alpha\"", "\"alpha\""}, "{\"alpha\", ClassWithAnnotationConstants.ALPHA}");
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"String.class", "String.class"}, "{String.class, String.class}");
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"},
        "{@AnnotationWithSingleValues(type = Integer.class,enumValue = RoundingMode.HALF_UP,num = 11,string = \"beta\",anno = @ValueAnnot(\"g1\")), @AnnotationWithSingleValues(type = Integer.class,enumValue = RoundingMode.HALF_DOWN,num = 12,string = ClassWithAnnotationConstants.BETA,anno = @ValueAnnot(\"g2\"))}");
    //deep check
    IAnnotation annos0 = a.element("annos").get().value().as(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "11", "11");
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", "RoundingMode.HALF_UP");
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"beta\"", "\"beta\"");
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", "Integer.class");
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g1\")", "@ValueAnnot(\"g1\")");
    IAnnotation annos1 = a.element("annos").get().value().as(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "12", "12");
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", "RoundingMode.HALF_DOWN");
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"beta\"", "ClassWithAnnotationConstants.BETA");
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", "Integer.class");
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g2\")", "@ValueAnnot(\"g2\")");

    IMethod m = t.methods().first().get();//the constructor is a synthetic method
    assertEquals(1, m.annotations().stream().count());
    a = m.annotations().first().get();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"21", "22"}, "{21, 22}");
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN"}, "{RoundingMode.HALF_EVEN, RoundingMode.HALF_EVEN}");
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"gamma\"", "\"gamma\""}, "{\"gamma\", ClassWithAnnotationConstants.GAMMA}");
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"Float.class", "Float.class"}, "{Float.class, Float.class}");
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"},
        "{@AnnotationWithSingleValues(type = Double.class,enumValue = RoundingMode.HALF_EVEN,num = 31,string = \"delta\",anno = @ValueAnnot(\"g3\")), @AnnotationWithSingleValues(type = Double.class,enumValue = RoundingMode.HALF_EVEN,num = 32,string = ClassWithAnnotationConstants.DELTA,anno = @ValueAnnot(\"g4\"))}");
    //deep check
    annos0 = a.element("annos").get().value().as(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "31", "31");
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN");
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"delta\"", "\"delta\"");
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", "Double.class");
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g3\")", "@ValueAnnot(\"g3\")");
    annos1 = a.element("annos").get().value().as(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "32", "32");
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN");
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"delta\"", "ClassWithAnnotationConstants.DELTA");
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", "Double.class");
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g4\")", "@ValueAnnot(\"g4\")");
  }

  @Test
  public void testAnnotationWithDefaultValuesInBinaryFile(IJavaEnvironment env) {
    IType t = env.requireType(ClassWithAnnotationWithDefaultValues.class.getName());
    assertEquals(1, t.annotations().stream().count());
    IAnnotation a = t.annotations().first().get();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "1", null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"one\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g\")", null);
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentBinaryOnlyFactory.class)
  public void testAnnotationWithSingleValuesInBinaryFile(IJavaEnvironment env) {
    IType t = env.requireType(ClassWithAnnotationWithSingleValues.class.getName());
    assertEquals(1, t.annotations().stream().count());
    IAnnotation a = t.annotations().first().get();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MIN_VALUE, null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g1\")", null);

    IMethod m = t.methods().withName("run").first().get();
    assertEquals(1, m.annotations().stream().count());
    a = m.annotations().first().get();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MAX_VALUE, null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g2\")", null);
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentBinaryOnlyFactory.class)
  public void testAnnotationWithArrayValuesInBinaryFile(IJavaEnvironment env) {
    IType t = env.requireType(ClassWithAnnotationWithArrayValues.class.getName());
    assertEquals(1, t.annotations().stream().count());
    IAnnotation a = t.annotations().first().get();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"1", "2"}, null);
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_UP", "RoundingMode.HALF_DOWN"}, null);
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"alpha\"", "\"alpha\""}, null);
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"String.class", "String.class"}, null);
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"}, null);
    //deep check
    IAnnotation annos0 = a.element("annos").get().value().as(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "11", null);
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"beta\"", null);
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", null);
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g1\")", null);
    IAnnotation annos1 = a.element("annos").get().value().as(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "12", null);
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", null);
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"beta\"", null);
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", null);
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g2\")", null);

    IMethod m = t.methods().withName("run").first().get();
    assertEquals(1, m.annotations().stream().count());
    a = m.annotations().first().get();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"21", "22"}, null);
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN"}, null);
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"gamma\"", "\"gamma\""}, null);
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"Float.class", "Float.class"}, null);
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"}, null);
    //deep check
    annos0 = a.element("annos").get().value().as(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "31", null);
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", null);
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"delta\"", null);
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", null);
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g3\")", null);
    annos1 = a.element("annos").get().value().as(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "32", null);
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", null);
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"delta\"", null);
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", null);
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@ValueAnnot(\"g4\")", null);
  }

  private static void assertAnnotationValue(IAnnotation a, String key, MetaValueType valueType, Class<?> modelType, CharSequence aptSource, CharSequence expressionSource) {
    IAnnotationElement av = a.element(key).get();
    IMetaValue mv = av.value();
    assertEquals(valueType, mv.type());
    assertSame(modelType, mv.as(Object.class).getClass());

    ISourceGenerator<IExpressionBuilder<?>> generator = mv.toWorkingCopy();
    StringBuilder buf = generator.toSource(ExpressionBuilder::create, new BuilderContext());
    assertEquals(removeWhitespace(aptSource), removeWhitespace(buf.toString()));

    if (expressionSource == null) {
      assertFalse(av.sourceOfExpression().isPresent());
    }
    else {
      assertEquals(removeWhitespace(expressionSource), removeWhitespace(av.sourceOfExpression().get().asCharSequence()));
    }
  }

  private static void assertAnnotationArrayValues(IAnnotation a, String key, MetaValueType elementValueType, Class<?> elementModelType, String[] aptSources, CharSequence expressionSource) {
    IAnnotationElement av = a.element(key).get();
    IMetaValue mv = av.value();
    assertEquals(MetaValueType.Array, mv.type());
    assertTrue(mv instanceof IArrayMetaValue);
    IMetaValue[] elements = ((IArrayMetaValue) mv).metaValueArray();
    assertEquals(aptSources.length, elements.length);
    for (int i = 0; i < elements.length; i++) {
      assertEquals(elementValueType, elements[i].type());
      assertSame(elementModelType, elements[i].as(Object.class).getClass());
      if (!"*".equals(aptSources[i])) {
        ISourceGenerator<IExpressionBuilder<?>> generator = elements[i].toWorkingCopy();
        StringBuilder buf = generator.toSource(ExpressionBuilder::create, new BuilderContext());
        assertEquals(removeWhitespace(aptSources[i]), removeWhitespace(buf.toString()));
      }
    }
    if (expressionSource == null) {
      assertFalse(av.sourceOfExpression().isPresent());
    }
    else {
      assertEquals(removeWhitespace(expressionSource), removeWhitespace(av.sourceOfExpression().get().asCharSequence()));
    }
  }

}
