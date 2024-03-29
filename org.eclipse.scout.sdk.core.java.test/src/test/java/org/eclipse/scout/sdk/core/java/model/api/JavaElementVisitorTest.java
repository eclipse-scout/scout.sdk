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

import static org.eclipse.scout.sdk.core.util.Strings.repeat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.java.fixture.BaseClass;
import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.fixture.ClassWithAnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JavaElementVisitorTest}</h3>
 *
 * @since 8.0.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class JavaElementVisitorTest {

  @Test
  public void testDepthFirstVisit(IJavaEnvironment env) {
    var icu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var visitor = new DepthFirstProtocolVisitor();
    var result = icu.visit(visitor);

    assertEquals(TreeVisitResult.CONTINUE, result);
    visitor.assertEqualsRefFile("JavaElementVisitorTest1.txt");
  }

  @Test
  public void testDepthFirstVisitSkippingElements(IJavaEnvironment env) {
    var icu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var visitor = new DepthFirstProtocolVisitor() {
      /**
       * abort protocol after the first annotation element
       */
      @Override
      public TreeVisitResult preVisit(IAnnotationElement annotationElement, int level, int index) {
        super.preVisit(annotationElement, level, index);
        return TreeVisitResult.SKIP_SIBLINGS;
      }

      /**
       * skip subtree of method
       */
      @Override
      public TreeVisitResult preVisit(IMethod method, int level, int index) {
        var result = super.preVisit(method, level, index);
        if ("methodInChildClass".equals(method.elementName())) {
          return TreeVisitResult.SKIP_SUBTREE;
        }
        return result;
      }

      /**
       * terminate visiting after the first SuppressWarnings annotation
       */
      @Override
      public TreeVisitResult preVisit(IAnnotation annotation, int level, int index) {
        var result = super.preVisit(annotation, level, index);
        if (SuppressWarnings.class.getName().equals(annotation.type().name())) {
          return TreeVisitResult.TERMINATE;
        }
        return result;
      }
    };
    var result = icu.visit(visitor);
    assertEquals(TreeVisitResult.TERMINATE, result);
    visitor.assertEqualsRefFile("JavaElementVisitorTest2.txt");
  }

  @Test
  public void testBreadthFirstVisit(IJavaEnvironment env) {
    var icu = env.requireType(BaseClass.class.getName()).requireCompilationUnit();
    var visitor = new BreadthFirstProtocolVisitor();
    var result = icu.visit(visitor);

    assertEquals(TreeVisitResult.CONTINUE, result);
    visitor.assertEqualsRefFile("JavaElementVisitorTest3.txt");
  }

  @Test
  public void testBreadthFirstVisitSkippingElements(IJavaEnvironment env) {
    var icu = env.requireType(BaseClass.class.getName()).requireCompilationUnit();
    var visitor = new BreadthFirstProtocolVisitor() {
      @Override
      protected TreeVisitResult visitElement(IJavaElement element, int level, int index) {
        if (level == 4) {
          return TreeVisitResult.TERMINATE;
        }
        return super.visitElement(element, level, index);
      }

      @Override
      public TreeVisitResult visit(IMethod method, int level, int index) {
        if ("methodInBaseClass".equals(method.elementName())) {
          return TreeVisitResult.SKIP_SUBTREE;
        }
        return super.visit(method, level, index);
      }

      @Override
      public TreeVisitResult visit(IType type, int level, int index) {
        if ("InnerClass1".equals(type.elementName())) {
          return TreeVisitResult.SKIP_SIBLINGS;
        }
        return super.visit(type, level, index);
      }
    };

    var result = icu.visit(visitor);
    assertEquals(TreeVisitResult.TERMINATE, result);
    visitor.assertEqualsRefFile("JavaElementVisitorTest4.txt");
  }

  @Test
  public void testVisitFunction(IJavaEnvironment env) {
    var icu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var aggregator1 = new StringBuilder();
    Function<IJavaElement, TreeVisitResult> function1 = element -> {
      if ("ChildClass".equals(element.elementName())) {
        return TreeVisitResult.TERMINATE;
      }
      aggregator1.append(element.elementName()).append(',');
      return TreeVisitResult.CONTINUE;
    };
    var result = icu.visit(function1);
    assertEquals(TreeVisitResult.TERMINATE, result);
    assertEquals("ChildClass.java,"
        + "org.eclipse.scout.sdk.core.java.fixture,"
        + "IOException,"
        + "Serializable,"
        + "AbstractList,"
        + "HashMap,"
        + "List,"
        + "Set,", aggregator1.toString());

    var aggregator2 = new StringBuilder();
    Function<IMethod, TreeVisitResult> function2 = element -> {
      aggregator2.append(element.elementName()).append(',');
      return TreeVisitResult.CONTINUE;
    };
    icu.visit(function2, IMethod.class);
    assertEquals("ChildClass,methodInChildClass,firstCase,", aggregator2.toString());
  }

  @Test
  public void testVisitNestedAnnotations(IJavaEnvironment env) {
    var type = env.requireType(ClassWithAnnotationWithArrayValues.class.getName());
    var protocol = new StringJoiner(",");
    Consumer<IJavaElement> visitor = element -> protocol.add(element.elementName());
    type.visit(visitor);
    assertEquals("ClassWithAnnotationWithArrayValues," +
        "AnnotationWithArrayValues," +
        "nums," +
        "enumValues," +
        "strings," +
        "types," +
        "annos," +
        "AnnotationWithSingleValues," +
        "num," +
        "enumValue," +
        "string," +
        "type," +
        "anno," +
        "ValueAnnot," +
        "value," +
        "AnnotationWithSingleValues," +
        "num," +
        "enumValue," +
        "string," +
        "type," +
        "anno," +
        "ValueAnnot," +
        "value," +
        "run," +
        "AnnotationWithArrayValues," +
        "nums," +
        "enumValues," +
        "strings," +
        "types," +
        "annos," +
        "AnnotationWithSingleValues," +
        "num," +
        "enumValue," +
        "string," +
        "type," +
        "anno," +
        "ValueAnnot," +
        "value," +
        "AnnotationWithSingleValues," +
        "num," +
        "enumValue," +
        "string," +
        "type," +
        "anno," +
        "ValueAnnot," +
        "value," +
        "a", protocol.toString());
  }

  @Test
  public void testVisitConsumer(IJavaEnvironment env) {
    var icu = env.requireType(ChildClass.class.getName()).requireCompilationUnit();
    var aggregator1 = new StringBuilder();
    Consumer<IJavaElement> consumer1 = element -> aggregator1.append(element.elementName()).append(',');
    icu.visit(consumer1);
    assertEquals("ChildClass.java,"
        + "org.eclipse.scout.sdk.core.java.fixture,"
        + "IOException,"
        + "Serializable,"
        + "AbstractList,"
        + "HashMap,"
        + "List,"
        + "Set,"
        + "ChildClass,"
        + "TestAnnotation,"
        + "values,"
        + "en,"
        + "inner,"
        + "X,"
        + "myString,"
        + "m_test,"
        + "TestAnnotation,"
        + "values,"
        + "en,"
        + "inner,"
        + "ChildClass,"
        + "methodInChildClass,"
        + "TestAnnotation,"
        + "values,"
        + "en,"
        + "inner,"
        + "firstParam,"
        + "secondParam,"
        + "firstCase,"
        + "SuppressWarnings,"
        + "value,", aggregator1.toString());

    var aggregator2 = new StringBuilder();
    Consumer<IAnnotation> consumer2 = element -> aggregator2.append(element.elementName()).append(',');
    icu.visit(consumer2, IAnnotation.class);
    assertEquals("TestAnnotation,TestAnnotation,TestAnnotation,SuppressWarnings,", aggregator2.toString());
  }

  private interface ITestingVisitor {

    StringBuilder protocol();

    default void append(IJavaElement e, int level, int index) {
      protocol()
          .append(repeat("  ", level))
          .append(e.elementName())
          .append(' ')
          .append(index)
          .append(" {")
          .append("\n");
    }

    default void postAppend(int level) {
      protocol()
          .append(repeat("  ", level))
          .append("}\n");
    }

    default void assertEqualsRefFile(String refFile) {
      try (var in = JavaElementVisitorTest.class.getResourceAsStream(refFile)) {
        var expected = Strings.fromInputStream(in, StandardCharsets.UTF_8).toString();
        assertEquals(expected, protocol().toString());
      }
      catch (IOException e) {
        throw new SdkException(e);
      }
    }
  }

  private static class BreadthFirstProtocolVisitor extends DefaultBreadthFirstJavaElementVisitor implements ITestingVisitor {

    protected final StringBuilder m_protocol = new StringBuilder();

    @Override
    protected TreeVisitResult visitElement(IJavaElement element, int level, int index) {
      append(element, level, index);
      return super.visitElement(element, level, index);
    }

    @Override
    public StringBuilder protocol() {
      return m_protocol;
    }
  }

  private static class DepthFirstProtocolVisitor extends DefaultDepthFirstJavaElementVisitor implements ITestingVisitor {

    protected final StringBuilder m_protocol = new StringBuilder();

    @Override
    protected TreeVisitResult preVisitElement(IJavaElement element, int level, int index) {
      append(element, level, index);
      return super.preVisitElement(element, level, index);
    }

    @Override
    protected boolean postVisitElement(IJavaElement element, int level, int index) {
      postAppend(level);
      return super.postVisitElement(element, level, index);
    }

    @Override
    public StringBuilder protocol() {
      return m_protocol;
    }
  }
}
