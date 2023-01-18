/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.transformer;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.fixture.AnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.fixture.ClassWithAnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.java.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.DefaultDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IImport;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.java.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.java.testing.CoreJavaTestingUtils;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link WorkingCopyTransformerTest}</h3>
 *
 * @since 8.0.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class WorkingCopyTransformerTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/java/transformer/";

  @Test
  public void testTransformationWithModifications(IJavaEnvironment env) {
    var newPackage = "other.movedPackage";
    var newSuppression = "unchecked";
    var newException = TimeoutException.class.getName();
    var transformer = new P_TestingWorkingCopyTransformer() {
      /**
       * Tests a package move
       */
      @Override
      public PackageGenerator transformPackage(ITransformInput<IPackage, PackageGenerator> input) {
        super.transformPackage(input);
        return input.requestDefaultWorkingCopy().withElementName(newPackage);
      }

      /**
       * Tests the modification of a working copy.
       */
      @Override
      public IMethodGenerator<?, ?> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ?>> input) {
        var generator = super.transformMethod(input);
        if ("methodInChildClass".equals(input.model().elementName())) {
          generator.withoutFlags(Flags.AccSynchronized);
          generator.withThrowable(newException);
          generator.withoutThrowable(IOException.class.getName());
        }
        return generator;
      }

      /**
       * Tests a complete replacement of the working copy
       */
      @Override
      public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
        if (SuppressWarnings.class.getName().equals(input.model().declaringAnnotation().name())) {
          remember(input.model(), "annotationElement"); // as there is no super call here: remember explicitly.
          return b -> b.stringLiteral(newSuppression);
        }
        return super.transformAnnotationElement(input);
      }
    };

    var transformedUnit = assertTransformedComponents(env, ChildClass.class, transformer,
        "package: org.eclipse.scout.sdk.core.java.fixture",
        "import: IOException",
        "import: Serializable",
        "import: AbstractList",
        "import: HashMap",
        "import: List",
        "import: Set",
        "type: ChildClass",
        "annotation: TestAnnotation",
        "annotationElement: values",
        "typeParam: X",
        "field: myString",
        "field: m_test",
        "annotation: TestAnnotation",
        "method: ChildClass",
        "method: methodInChildClass",
        "annotation: TestAnnotation",
        "annotationElement: values",
        "annotationElement: en",
        "methodParam: firstParam",
        "methodParam: secondParam",
        "method: firstCase",
        "annotation: SuppressWarnings",
        "annotationElement: value");

    // verify transformation modifications
    var methodInChildClass = transformedUnit.requireMainType()
        .methods()
        .withName("methodInChildClass")
        .first().orElseThrow();
    assertEquals(newPackage, transformedUnit.containingPackage().elementName());
    assertArrayEquals(new String[]{newException}, methodInChildClass.exceptionTypes().map(IType::name).toArray(String[]::new));
    assertEquals(Flags.AccProtected, methodInChildClass.flags());
    assertEquals(newSuppression, transformedUnit.requireMainType()
        .methods()
        .withName("firstCase")
        .first().orElseThrow()
        .annotations()
        .withName(SuppressWarnings.class.getName())
        .first().orElseThrow()
        .element("value").orElseThrow()
        .value().as(String.class));
  }

  @Test
  public void testTransformationOnArrayAnnotationWithoutModifications(IJavaEnvironment env) {
    assertTransformedComponents(env, ClassWithAnnotationWithArrayValues.class,
        "package: org.eclipse.scout.sdk.core.java.fixture",
        "import: RoundingMode",
        "type: ClassWithAnnotationWithArrayValues",
        "annotation: AnnotationWithArrayValues",
        "annotationElement: nums",
        "annotationElement: enumValues",
        "annotationElement: strings",
        "annotationElement: types",
        "annotationElement: annos",
        "annotation: AnnotationWithSingleValues",
        "annotationElement: num",
        "annotationElement: enumValue",
        "annotationElement: string",
        "annotationElement: type",
        "annotationElement: anno",
        "annotation: ValueAnnot",
        "annotationElement: value",
        "annotation: AnnotationWithSingleValues",
        "annotationElement: num",
        "annotationElement: enumValue",
        "annotationElement: string",
        "annotationElement: type",
        "annotationElement: anno",
        "annotation: ValueAnnot",
        "annotationElement: value",
        "method: run",
        "annotation: AnnotationWithArrayValues",
        "annotationElement: nums",
        "annotationElement: enumValues",
        "annotationElement: strings",
        "annotationElement: types",
        "annotationElement: annos",
        "annotation: AnnotationWithSingleValues",
        "annotationElement: num",
        "annotationElement: enumValue",
        "annotationElement: string",
        "annotationElement: type",
        "annotationElement: anno",
        "annotation: ValueAnnot",
        "annotationElement: value",
        "annotation: AnnotationWithSingleValues",
        "annotationElement: num",
        "annotationElement: enumValue",
        "annotationElement: string",
        "annotationElement: type",
        "annotationElement: anno",
        "annotation: ValueAnnot",
        "annotationElement: value",
        "methodParam: a");
  }

  @Test
  public void testTransformationOfUnresolvedType(IJavaEnvironment env) {
    var notExisting = env.findUnresolvedType("not.existing.WhatEver");
    var existing = env.findUnresolvedType(Long.class.getName());

    var transformer = new P_TestingWorkingCopyTransformer();
    var generator = notExisting.toWorkingCopy(transformer);
    assertEquals("classWhatEver{}", Pattern.compile("\\s").matcher(generator.toJavaSource(env)).replaceAll(""));

    transformer = new P_TestingWorkingCopyTransformer();
    generator = existing.toWorkingCopy(transformer);
    assertNotNull(generator);
    assertTrue(transformer.m_protocol.size() > 10); // Long must be found and therefore has some elements
  }

  @Test
  public void testTransformationOfOverriddenMethod(IJavaEnvironment env) {
    IWorkingCopyTransformer transformer = new DefaultWorkingCopyTransformer() {
      @Override
      public IMethodGenerator<?, ?> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ?>> input) {
        var templateMethod = input.model();
        var overrideGenerator = input.requestDefaultWorkingCopy();
        return switch (templateMethod.elementName()) {
          case "toString" ->
            // provide method body for toString method
            overrideGenerator.withBody(b -> b.returnClause().stringLiteral("SampleCloseable class").semicolon());
          case "close" ->
            // remove throws declaration for close method
            overrideGenerator.withoutThrowable(Exception.class.getName());
          default -> overrideGenerator;
        };
      }
    };

    var generator = PrimaryTypeGenerator.create()
        .withElementName("SampleCloseable")
        .withInterface(AutoCloseable.class.getName()) // defines the methods that can be overridden
        .withMethod(MethodOverrideGenerator.createOverride(transformer)
            .withElementName("toString")) // override toString
        .withAllMethodsImplemented(transformer); // override all methods required by super types.

    assertEqualsRefFile(env, REF_FILE_FOLDER + "TransformerTest1.txt", generator);
  }

  /**
   * If changes in this method are necessary also update the corresponding example on {@link IWorkingCopyTransformer}
   * class javadoc.
   *
   * @param env
   *          The environment passed
   */
  @Test
  public void testTransformerDocumentation(IJavaEnvironment env) {
    IWorkingCopyTransformer transformer = new DefaultWorkingCopyTransformer() {
      @Override
      public IMethodGenerator<?, ?> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ?>> input) {
        var templateMethod = input.model();
        var overrideGenerator = input.requestDefaultWorkingCopy();
        return switch (templateMethod.elementName()) {
          case "toString" ->
            // provide method body for toString method
            overrideGenerator.withBody(b -> b.returnClause().stringLiteral("SampleCloseable class").semicolon());
          case "close" ->
            // remove throws declaration for close method
            overrideGenerator.withoutThrowable(Exception.class.getName());
          default -> overrideGenerator;
        };
      }
    };

    var generator = PrimaryTypeGenerator.create()
        .withElementName("SampleCloseable")
        .withInterface(AutoCloseable.class.getName()) // defines the methods that can be overridden
        .withMethod(MethodOverrideGenerator.createOverride(transformer)
            .withElementName("toString")) // override toString
        .withAllMethodsImplemented(transformer); // override all methods required by super types.
    assertNotNull(generator);

    var icu = env.requireType(Long.class.getName()).requireCompilationUnit();
    var workingCopy = icu.toWorkingCopy(
        new SimpleWorkingCopyTransformerBuilder()
            .withAnnotationMapper(this::transformAnnotation) // change SuppressWarnings to 'all'
            .withMethodParameterMapper(IWorkingCopyTransformer::remove) // remove all parameters from methods
            .build());
    assertNotNull(workingCopy);
  }

  @SuppressWarnings("MethodMayBeStatic")
  private IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input) {
    if (SuppressWarnings.class.getName().equals(input.model().type().name())) {
      // modify all suppress-warning annotations to suppress all warnings
      return input.requestDefaultWorkingCopy().withElement("value", b -> b.stringLiteral("all"));
    }
    return input.requestDefaultWorkingCopy();
  }

  @Test
  public void testTransformationIsExecutedBeforeGeneratorExecution(IJavaEnvironment env) {
    var transformCount = new AtomicInteger();
    var transformer = new SimpleWorkingCopyTransformerBuilder()
        .withAnnotationElementMapper(input -> {
          if ("type".equals(input.model().elementName())) {
            transformCount.incrementAndGet();
          }
          return input.requestDefaultWorkingCopy();
        }).build();

    env.requireType(ClassWithAnnotationWithArrayValues.class.getName()).toWorkingCopy(transformer); // here the generator is not saved and therefore not executed. even though the transformation must have been executed completely!
    assertEquals(4, transformCount.get());
  }

  @Test
  public void testTransformationOfOverriddenMethodWithRemove(IJavaEnvironment env) {
    var methodToModify = "methodInChildClass";
    var transformer = new SimpleWorkingCopyTransformerBuilder()
        .withMethodParameterMapper(IWorkingCopyTransformer::remove) // remove all method parameters
        .build();
    var generatorWithoutTransformer = PrimaryTypeGenerator.create()
        .withElementName("TestOnly")
        .withSuperClass(ChildClass.class.getName())
        .withMethod(MethodOverrideGenerator.createOverride()
            .withElementName(methodToModify));
    var generatorWithTransformer = PrimaryTypeGenerator.create()
        .withElementName("TestOnly")
        .withSuperClass(ChildClass.class.getName())
        .withMethod(MethodOverrideGenerator.createOverride(transformer)
            .withElementName(methodToModify));

    var numParamsWithoutTransformer = CoreJavaTestingUtils.registerCompilationUnit(env, generatorWithoutTransformer)
        .methods()
        .withName(methodToModify)
        .first().orElseThrow()
        .parameters().stream()
        .count();
    var numParamsWithTransformer = CoreJavaTestingUtils.registerCompilationUnit(env, generatorWithTransformer)
        .methods()
        .withName(methodToModify)
        .first().orElseThrow()
        .parameters().stream()
        .count();
    assertEquals(2, numParamsWithoutTransformer);
    assertEquals(0, numParamsWithTransformer);
  }

  @Test
  public void testRemovalOfOverriddenMethod(IJavaEnvironment env) {
    var transformer = new SimpleWorkingCopyTransformerBuilder()
        .withMethodMapper(WorkingCopyTransformerTest::transformMethod)
        .build();
    var generator = PrimaryTypeGenerator.create()
        .withElementName("SampleSequence")
        .withInterface(CharSequence.class.getName());

    var numMethodsWithoutTransformer = CoreJavaTestingUtils.registerCompilationUnit(env, generator.withAllMethodsImplemented()).methods().stream().count();
    var numMethodsWithTransformer = CoreJavaTestingUtils.registerCompilationUnit(env, generator.withAllMethodsImplemented(transformer)).methods().stream().count();
    assertEquals(numMethodsWithoutTransformer - 1 /* the method removed by the transformer */, numMethodsWithTransformer);
  }

  private static IMethodGenerator<?, ?> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ?>> input) {
    if ("charAt".equals(input.model().elementName())) {
      return null;
    }
    return input.requestDefaultWorkingCopy();
  }

  @Test
  public void testTransformerRemovingAnnotationElement(IJavaEnvironment env) {
    var annotationElementNameToRemove = "strings";
    var classWithAnnotationArrayValues = env.requireType(ClassWithAnnotationWithArrayValues.class.getName());
    var firstAnnotation = classWithAnnotationArrayValues.annotations().withName(AnnotationWithArrayValues.class.getName()).first().orElseThrow();
    assertFalse(firstAnnotation.element(annotationElementNameToRemove).orElseThrow().isDefault());

    IWorkingCopyTransformer removeElementTransformer = new DefaultWorkingCopyTransformer() {
      @Override
      public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
        if (annotationElementNameToRemove.equals(input.model().elementName())) {
          return remove(); // remove the "strings" element from all annotations
        }
        return super.transformAnnotationElement(input);
      }
    };

    var generator1 = classWithAnnotationArrayValues.toWorkingCopy().annotations().findAny().orElseThrow();
    assertTrue(generator1.element(func -> annotationElementNameToRemove.equals(func.apply().orElseThrow())).isPresent()); // is not removed with default transform

    var generator2 = classWithAnnotationArrayValues.toWorkingCopy(removeElementTransformer).annotations().findAny().orElseThrow();
    assertFalse(generator2.element(func -> annotationElementNameToRemove.equals(func.apply().orElseThrow())).isPresent()); // is removed because of transformer
  }

  @Test
  public void testTransformerRemovingAnnotations(IJavaEnvironment env) {
    var baseType = env.requireType(ClassWithAnnotationWithArrayValues.class.getName()).requireCompilationUnit();
    assertEquals(10, numberOfAnnotationsIn(baseType));

    IWorkingCopyTransformer removeAllAnnotationsTransformer = new DefaultWorkingCopyTransformer() {
      @Override
      public IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input) {
        return null;
      }
    };
    var generator = baseType.toWorkingCopy(removeAllAnnotationsTransformer);

    // change the name so that the original class is not changed in the IJavaEnvironment. Otherwise, it is modified for later tests
    var newClassName = "OtherClass";
    generator.mainType().orElseThrow().withElementName(newClassName);
    generator.withElementName(newClassName);

    var baseTypeWithoutAnnotations = assertNoCompileErrors(env, generator);
    assertEquals(0, numberOfAnnotationsIn(baseTypeWithoutAnnotations));
  }

  private static int numberOfAnnotationsIn(IJavaElement element) {
    var numberOfAnnotations = new AtomicInteger();
    element.visit(new DefaultDepthFirstJavaElementVisitor() {
      @Override
      public TreeVisitResult preVisit(IAnnotation annotation, int level, int index) {
        numberOfAnnotations.incrementAndGet();
        return super.preVisit(annotation, level, index);
      }
    });
    return numberOfAnnotations.get();
  }

  /**
   * Parses the specified fixture class and transforms it into a working copy. A default {@link IWorkingCopyTransformer}
   * is used that does not apply any transformation. It is ensured that the specified components have been called on the
   * transformer. The working copy is then parsed into an {@link ICompilationUnit} again and returned.
   *
   * @param env
   *          The {@link IJavaEnvironment} to use.
   * @param fixture
   *          The class to transform.
   * @param components
   *          The components that must be called on the transformer.
   * @return The transformed {@link ICompilationUnit} of the specified fixture class.
   */
  private static ICompilationUnit assertTransformedComponents(IJavaEnvironment env, Class<?> fixture, String... components) {
    return assertTransformedComponents(env, fixture, null, components);
  }

  /**
   * Parses the specified fixture class and transforms it into a working copy using the specified transformer. It is
   * ensured that the specified components have been called on the transformer. The working copy is then parsed into an
   * {@link ICompilationUnit} again and returned.
   *
   * @param env
   *          The {@link IJavaEnvironment} to use.
   * @param fixture
   *          The class to transform.
   * @param transformer
   *          The transformer to use.
   * @param components
   *          The components that must be called on the transformer.
   * @return The transformed {@link ICompilationUnit} of the specified fixture class.
   */
  private static ICompilationUnit assertTransformedComponents(IJavaEnvironment env, Class<?> fixture, P_TestingWorkingCopyTransformer transformer, String... components) {
    var t = Optional.ofNullable(transformer)
        .orElseGet(P_TestingWorkingCopyTransformer::new);
    var generator = env.requireType(fixture.getName())
        .requireCompilationUnit()
        .toWorkingCopy(t);

    // build the source here as some components are transformed during generation time and not during setup time.
    var primaryType = CoreJavaTestingUtils.registerCompilationUnit(env, generator);

    assertArrayEquals(components, t.m_protocol.toArray(new String[0]));
    return assertNoCompileErrors(primaryType).requireCompilationUnit();
  }

  private static class P_TestingWorkingCopyTransformer extends DefaultWorkingCopyTransformer {

    private final List<String> m_protocol = new ArrayList<>();

    protected void remember(IJavaElement model, String type) {
      m_protocol.add(type + ": " + model.elementName());
    }

    @Override
    public IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input) {
      remember(input.model(), "annotation");
      return super.transformAnnotation(input);
    }

    @Override
    public ICompilationUnitGenerator<?> transformCompilationUnit(ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>> input) {
      remember(input.model(), "compilationUnit");
      return super.transformCompilationUnit(input);
    }

    @Override
    public ITypeParameterGenerator<?> transformTypeParameter(ITransformInput<ITypeParameter, ITypeParameterGenerator<?>> input) {
      remember(input.model(), "typeParam");
      return super.transformTypeParameter(input);
    }

    @Override
    public IMethodParameterGenerator<?> transformMethodParameter(ITransformInput<IMethodParameter, IMethodParameterGenerator<?>> input) {
      remember(input.model(), "methodParam");
      return super.transformMethodParameter(input);
    }

    @Override
    public IFieldGenerator<?> transformField(ITransformInput<IField, IFieldGenerator<?>> input) {
      remember(input.model(), "field");
      return super.transformField(input);
    }

    @Override
    public IMethodGenerator<?, ?> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ?>> input) {
      remember(input.model(), "method");
      return super.transformMethod(input);
    }

    @Override
    public ITypeGenerator<?> transformType(ITransformInput<IType, ITypeGenerator<?>> input) {
      remember(input.model(), "type");
      return super.transformType(input);
    }

    @Override
    public ITypeGenerator<?> transformUnresolvedType(ITransformInput<IUnresolvedType, ITypeGenerator<?>> input) {
      remember(input.model(), "unresolvedType");
      return super.transformUnresolvedType(input);
    }

    @Override
    public PackageGenerator transformPackage(ITransformInput<IPackage, PackageGenerator> input) {
      remember(input.model(), "package");
      return super.transformPackage(input);
    }

    @Override
    public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
      remember(input.model(), "annotationElement");
      return super.transformAnnotationElement(input);
    }

    @Override
    public CharSequence transformImport(ITransformInput<IImport, CharSequence> input) {
      m_protocol.add("import: " + input.model().elementName());
      return super.transformImport(input);
    }
  }
}
