/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model;

import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.removeWhitespace;

import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationConstants;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithDefaultValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithSingleValues;
import org.eclipse.scout.sdk.core.importcollector.EmptyImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.TypeImplementor;
import org.eclipse.scout.sdk.core.sourcebuilder.ExpressionSourceBuilderFactory;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link AnnotationSourceTest}</h3>
 * <p>
 * Used classes are {@link ClassWithAnnotationWithDefaultValues}, {@link ClassWithAnnotationWithArrayValues},
 * {@link ClassWithAnnotationWithSingleValues}, {@link ClassWithAnnotationConstants}
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class AnnotationSourceTest {

  @Test
  public void testAnnotationWithDefaultValuesInSourceFile() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();//src/main/fixture

    IType t = env.findType(ClassWithAnnotationWithDefaultValues.class.getName());
    Assert.assertEquals(1, t.annotations().list().size());
    IAnnotation a = t.annotations().first();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "1", null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"one\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g\")", null);
  }

  @Test
  public void testAnnotationWithSingleValuesInSourceFile() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();//src/main/fixture

    IType t = env.findType(ClassWithAnnotationWithSingleValues.class.getName());
    Assert.assertEquals(1, t.annotations().list().size());
    IAnnotation a = t.annotations().first();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MIN_VALUE, "Integer.MIN_VALUE");
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", "RoundingMode.HALF_UP");
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", "\"alpha\"");
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", "String.class");
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g1\")", "@Generated(\"g1\")");

    IMethod m = t.methods().first();//the constructor is a synthetic method
    Assert.assertEquals(1, m.annotations().list().size());
    a = m.annotations().first();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MAX_VALUE, "Integer.MAX_VALUE");
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", "RoundingMode.HALF_DOWN");
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", "ClassWithAnnotationConstants.ALPHA");
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", "Integer.class");
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g2\")", "@Generated(\"g2\")");
  }

  @Test
  public void testAnnotationWithArrayValuesInSourceFile() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();//src/main/fixture

    IType t = env.findType(ClassWithAnnotationWithArrayValues.class.getName());
    Assert.assertEquals(1, t.annotations().list().size());
    IAnnotation a = t.annotations().first();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"1", "2"}, "{1, 2}");
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_UP", "RoundingMode.HALF_DOWN"}, "{RoundingMode.HALF_UP, RoundingMode.HALF_DOWN}");
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"alpha\"", "\"alpha\""}, "{\"alpha\", ClassWithAnnotationConstants.ALPHA}");
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"String.class", "String.class"}, "{String.class, String.class}");
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"},
        "{@AnnotationWithSingleValues(type = Integer.class,enumValue = RoundingMode.HALF_UP,num = 11,string = \"beta\",anno = @Generated(\"g1\")), @AnnotationWithSingleValues(type = Integer.class,enumValue = RoundingMode.HALF_DOWN,num = 12,string = ClassWithAnnotationConstants.BETA,anno = @Generated(\"g2\"))}");
    //deep check
    IAnnotation annos0 = a.value("annos").metaValue().get(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "11", "11");
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", "RoundingMode.HALF_UP");
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"beta\"", "\"beta\"");
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", "Integer.class");
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g1\")", "@Generated(\"g1\")");
    IAnnotation annos1 = a.value("annos").metaValue().get(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "12", "12");
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", "RoundingMode.HALF_DOWN");
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"beta\"", "ClassWithAnnotationConstants.BETA");
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", "Integer.class");
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g2\")", "@Generated(\"g2\")");

    IMethod m = t.methods().first();//the constructor is a synthetic method
    Assert.assertEquals(1, m.annotations().list().size());
    a = m.annotations().first();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"21", "22"}, "{21, 22}");
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN"}, "{RoundingMode.HALF_EVEN, RoundingMode.HALF_EVEN}");
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"gamma\"", "\"gamma\""}, "{\"gamma\", ClassWithAnnotationConstants.GAMMA}");
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"Float.class", "Float.class"}, "{Float.class, Float.class}");
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"},
        "{@AnnotationWithSingleValues(type = Double.class,enumValue = RoundingMode.HALF_EVEN,num = 31,string = \"delta\",anno = @Generated(\"g3\")), @AnnotationWithSingleValues(type = Double.class,enumValue = RoundingMode.HALF_EVEN,num = 32,string = ClassWithAnnotationConstants.DELTA,anno = @Generated(\"g4\"))}");
    //deep check
    annos0 = a.value("annos").metaValue().get(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "31", "31");
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN");
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"delta\"", "\"delta\"");
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", "Double.class");
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g3\")", "@Generated(\"g3\")");
    annos1 = a.value("annos").metaValue().get(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "32", "32");
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN");
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"delta\"", "ClassWithAnnotationConstants.DELTA");
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", "Double.class");
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g4\")", "@Generated(\"g4\")");
  }

  @Test
  public void testAnnotationWithDefaultValuesInBinaryFile() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();//src/main/fixture

    IType t = env.findType(ClassWithAnnotationWithDefaultValues.class.getName());
    Assert.assertEquals(1, t.annotations().list().size());
    IAnnotation a = t.annotations().first();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "1", null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"one\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated(\"g\")", null);
  }

  @Test
  public void testAnnotationWithSingleValuesInBinaryFile() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironmentWithBinaries();//target/classes

    IType t = env.findType(ClassWithAnnotationWithSingleValues.class.getName());
    Assert.assertEquals(1, t.annotations().list().size());
    IAnnotation a = t.annotations().first();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MIN_VALUE, null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "String.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated({\"g1\"})", null);

    IMethod m = t.methods().withName("run").first();
    Assert.assertEquals(1, m.annotations().list().size());
    a = m.annotations().first();
    assertAnnotationValue(a, "num", MetaValueType.Int, Integer.class, "" + Integer.MAX_VALUE, null);
    assertAnnotationValue(a, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", null);
    assertAnnotationValue(a, "string", MetaValueType.String, String.class, "\"alpha\"", null);
    assertAnnotationValue(a, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", null);
    assertAnnotationValue(a, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated({\"g2\"})", null);
  }

  @Test
  public void testAnnotationWithArrayValuesInBinaryFile() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironmentWithBinaries();//target/classes

    IType t = env.findType(ClassWithAnnotationWithArrayValues.class.getName());
    Assert.assertEquals(1, t.annotations().list().size());
    IAnnotation a = t.annotations().first();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"1", "2"}, null);
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_UP", "RoundingMode.HALF_DOWN"}, null);
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"alpha\"", "\"alpha\""}, null);
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"String.class", "String.class"}, null);
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"}, null);
    //deep check
    IAnnotation annos0 = a.value("annos").metaValue().get(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "11", null);
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_UP", null);
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"beta\"", null);
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", null);
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated({\"g1\"})", null);
    IAnnotation annos1 = a.value("annos").metaValue().get(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "12", null);
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_DOWN", null);
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"beta\"", null);
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Integer.class", null);
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated({\"g2\"})", null);

    IMethod m = t.methods().withName("run").first();
    Assert.assertEquals(1, m.annotations().list().size());
    a = m.annotations().first();
    assertAnnotationArrayValues(a, "nums", MetaValueType.Int, Integer.class, new String[]{"21", "22"}, null);
    assertAnnotationArrayValues(a, "enumValues", MetaValueType.Enum, FieldImplementor.class, new String[]{"RoundingMode.HALF_EVEN", "RoundingMode.HALF_EVEN"}, null);
    assertAnnotationArrayValues(a, "strings", MetaValueType.String, String.class, new String[]{"\"gamma\"", "\"gamma\""}, null);
    assertAnnotationArrayValues(a, "types", MetaValueType.Type, TypeImplementor.class, new String[]{"Float.class", "Float.class"}, null);
    assertAnnotationArrayValues(a, "annos", MetaValueType.Annotation, AnnotationImplementor.class, new String[]{"*", "*"}, null);
    //deep check
    annos0 = a.value("annos").metaValue().get(IAnnotation[].class)[0];
    assertAnnotationValue(annos0, "num", MetaValueType.Int, Integer.class, "31", null);
    assertAnnotationValue(annos0, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", null);
    assertAnnotationValue(annos0, "string", MetaValueType.String, String.class, "\"delta\"", null);
    assertAnnotationValue(annos0, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", null);
    assertAnnotationValue(annos0, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated({\"g3\"})", null);
    annos1 = a.value("annos").metaValue().get(IAnnotation[].class)[1];
    assertAnnotationValue(annos1, "num", MetaValueType.Int, Integer.class, "32", null);
    assertAnnotationValue(annos1, "enumValue", MetaValueType.Enum, FieldImplementor.class, "RoundingMode.HALF_EVEN", null);
    assertAnnotationValue(annos1, "string", MetaValueType.String, String.class, "\"delta\"", null);
    assertAnnotationValue(annos1, "type", MetaValueType.Type, TypeImplementor.class, "Double.class", null);
    assertAnnotationValue(annos1, "anno", MetaValueType.Annotation, AnnotationImplementor.class, "@Generated({\"g4\"})", null);
  }

  private static void assertAnnotationValue(IAnnotation a, String key, MetaValueType valueType, Class<?> modelType, String aptSource, String expressionSource) {
    IAnnotationValue av = a.value(key);
    IMetaValue mv = av.metaValue();
    Assert.assertEquals(valueType, mv.type());
    Assert.assertEquals(modelType, mv.get(Object.class).getClass());
    StringBuilder buf = new StringBuilder();
    ExpressionSourceBuilderFactory.createFromMetaValue(mv).createSource(buf, "\n", new PropertyMap(), new ImportValidator(new EmptyImportCollector()));
    Assert.assertEquals(removeWhitespace(aptSource), removeWhitespace(buf.toString()));
    ISourceRange range = av.sourceOfExpression();
    Assert.assertEquals(removeWhitespace(expressionSource), removeWhitespace(range != null ? range.toString() : null));
  }

  private static void assertAnnotationArrayValues(IAnnotation a, String key, MetaValueType elementValueType, Class<?> elementModelType, String[] aptSources, String expressionSource) {
    IAnnotationValue av = a.value(key);
    IMetaValue mv = av.metaValue();
    Assert.assertEquals(MetaValueType.Array, mv.type());
    Assert.assertTrue(mv instanceof IArrayMetaValue);
    IMetaValue[] elements = ((IArrayMetaValue) mv).metaValueArray();
    Assert.assertEquals(aptSources.length, elements.length);
    for (int i = 0; i < elements.length; i++) {
      Assert.assertEquals(elementValueType, elements[i].type());
      Assert.assertEquals(elementModelType, elements[i].get(Object.class).getClass());
      if (!"*".equals(aptSources[i])) {
        StringBuilder buf = new StringBuilder();
        ExpressionSourceBuilderFactory.createFromMetaValue(elements[i]).createSource(buf, "\n", new PropertyMap(), new ImportValidator(new EmptyImportCollector()));
        Assert.assertEquals(removeWhitespace(aptSources[i]), removeWhitespace(buf.toString()));
      }
    }
    ISourceRange range = av.sourceOfExpression();
    Assert.assertEquals(removeWhitespace(expressionSource), removeWhitespace(range != null ? range.toString() : null));
  }

}
