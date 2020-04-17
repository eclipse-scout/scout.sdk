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
package org.eclipse.scout.sdk.core.generator.transformer;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
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

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.fixture.AnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.DefaultDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link WorkingCopyTransformerTest}</h3>
 *
 * @since 8.0.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class WorkingCopyTransformerTest {

  @SuppressWarnings("HardcodedFileSeparator")
  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/transformer/";

  @Test
  public void testTransformationWithModifications(IJavaEnvironment env) {
    String newPackage = "other.movedPackage";
    String newSuppression = "unchecked";
    String newException = TimeoutException.class.getName();
    P_TestingWorkingCopyTransformer transformer = new P_TestingWorkingCopyTransformer() {
      /**
       * Tests a package move
       */
      @Override
      public String transformPackage(ITransformInput<IPackage, String> input) {
        super.transformPackage(input);
        return newPackage;
      }

      /**
       * Tests the modification of a working copy.
       */
      @Override
      public IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
        IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> generator = super.transformMethod(input);
        if ("methodInChildClass".equals(input.model().elementName())) {
          generator.withoutFlags(Flags.AccSynchronized);
          generator.withException(newException);
          generator.withoutException(IOException.class.getName());
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

    ICompilationUnit transformedUnit = assertTransformedComponents(env, ChildClass.class, transformer,
        "package: org.eclipse.scout.sdk.core.fixture",
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
    IMethod methodInChildClass = transformedUnit.requireMainType()
        .methods()
        .withName("methodInChildClass")
        .first().get();
    assertEquals(newPackage, transformedUnit.containingPackage().elementName());
    assertArrayEquals(new String[]{newException}, methodInChildClass.exceptionTypes().map(IType::name).toArray(String[]::new));
    assertEquals(Flags.AccProtected, methodInChildClass.flags());
    assertEquals(newSuppression, transformedUnit.requireMainType()
        .methods()
        .withName("firstCase")
        .first().get()
        .annotations()
        .withName(SuppressWarnings.class.getName())
        .first().get()
        .element("value").get()
        .value().as(String.class));
  }

  @Test
  public void testTransformationOnArrayAnnotationWithoutModifications(IJavaEnvironment env) {
    assertTransformedComponents(env, ClassWithAnnotationWithArrayValues.class,
        "package: org.eclipse.scout.sdk.core.fixture",
        "import: RoundingMode",
        "import: Generated",
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
        "annotation: Generated",
        "annotationElement: value",
        "annotation: AnnotationWithSingleValues",
        "annotationElement: num",
        "annotationElement: enumValue",
        "annotationElement: string",
        "annotationElement: type",
        "annotationElement: anno",
        "annotation: Generated",
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
        "annotation: Generated",
        "annotationElement: value",
        "annotation: AnnotationWithSingleValues",
        "annotationElement: num",
        "annotationElement: enumValue",
        "annotationElement: string",
        "annotationElement: type",
        "annotationElement: anno",
        "annotation: Generated",
        "annotationElement: value",
        "methodParam: a");
  }

  @Test
  public void testTransformationOfUnresolvedType(IJavaEnvironment env) {
    IUnresolvedType notExisting = env.findUnresolvedType("not.existing.WhatEver");
    IUnresolvedType existing = env.findUnresolvedType(Long.class.getName());

    P_TestingWorkingCopyTransformer transformer = new P_TestingWorkingCopyTransformer();
    ITypeGenerator<?> generator = notExisting.toWorkingCopy(transformer);
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
      public IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
        IMethod templateMethod = input.model();
        IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> overrideGenerator = input.requestDefaultWorkingCopy();
        switch (templateMethod.elementName()) {
          case "toString":
            // provide method body for toString method
            return overrideGenerator.withBody(b -> b.returnClause().stringLiteral("SampleCloseable class").semicolon());
          case "close":
            // remove throws declaration for close method
            return overrideGenerator.withoutException(Exception.class.getName());
          default:
            return overrideGenerator;
        }
      }
    };

    PrimaryTypeGenerator<?> generator = PrimaryTypeGenerator.create()
        .withElementName("SampleCloseable")
        .withInterface(AutoCloseable.class.getName()) // defines the methods that can be overridden
        .withMethod(MethodOverrideGenerator.createOverride(transformer)
            .withElementName("toString")) // override toString
        .withAllMethodsImplemented(transformer); // override all methods required by super types.

    assertEqualsRefFile(env, REF_FILE_FOLDER + "TranformerTest1.txt", generator);
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
      public IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
        IMethod templateMethod = input.model();
        IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> overrideGenerator = input.requestDefaultWorkingCopy();
        switch (templateMethod.elementName()) {
          case "toString":
            // provide method body for toString method
            return overrideGenerator.withBody(b -> b.returnClause().stringLiteral("SampleCloseable class").semicolon());
          case "close":
            // remove throws declaration for close method
            return overrideGenerator.withoutException(Exception.class.getName());
          default:
            return overrideGenerator;
        }
      }
    };

    //noinspection unused
    PrimaryTypeGenerator<?> generator = PrimaryTypeGenerator.create()
        .withElementName("SampleCloseable")
        .withInterface(AutoCloseable.class.getName()) // defines the methods that can be overridden
        .withMethod(MethodOverrideGenerator.createOverride(transformer)
            .withElementName("toString")) // override toString
        .withAllMethodsImplemented(transformer); // override all methods required by super types.

    ICompilationUnit icu = env.requireType(Long.class.getName()).requireCompilationUnit();
    //noinspection unused
    ICompilationUnitGenerator<?> workingCopy = icu.toWorkingCopy(
        new SimpleWorkingCopyTransformerBuilder()
            .withAnnotationMapper(this::transformAnnotation) // change SuppressWarnings to 'all'
            .withMethodParameterMapper(IWorkingCopyTransformer::remove) // remove all parameters from methods
            .build());

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
    AtomicInteger transformCount = new AtomicInteger();
    IWorkingCopyTransformer transformer = new SimpleWorkingCopyTransformerBuilder()
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
    String methodToModify = "methodInChildClass";
    IWorkingCopyTransformer transformer = new SimpleWorkingCopyTransformerBuilder()
        .withMethodParameterMapper(IWorkingCopyTransformer::remove) // remove all method parameters
        .build();
    PrimaryTypeGenerator<?> generatorWithoutTransformer = PrimaryTypeGenerator.create()
        .withElementName("TestOnly")
        .withSuperClass(ChildClass.class.getName())
        .withMethod(MethodOverrideGenerator.createOverride()
            .withElementName(methodToModify));
    PrimaryTypeGenerator<?> generatorWithTransformer = PrimaryTypeGenerator.create()
        .withElementName("TestOnly")
        .withSuperClass(ChildClass.class.getName())
        .withMethod(MethodOverrideGenerator.createOverride(transformer)
            .withElementName(methodToModify));

    long numParamsWithoutTransformer = CoreTestingUtils.registerCompilationUnit(env, generatorWithoutTransformer)
        .methods()
        .withName(methodToModify)
        .first().get()
        .parameters().stream()
        .count();
    long numParamsWithTransformer = CoreTestingUtils.registerCompilationUnit(env, generatorWithTransformer)
        .methods()
        .withName(methodToModify)
        .first().get()
        .parameters().stream()
        .count();
    assertEquals(2, numParamsWithoutTransformer);
    assertEquals(0, numParamsWithTransformer);
  }

  @Test
  public void testRemovalOfOverriddenMethod(IJavaEnvironment env) {
    IWorkingCopyTransformer transformer = new SimpleWorkingCopyTransformerBuilder()
        .withMethodMapper(WorkingCopyTransformerTest::transformMethod)
        .build();
    PrimaryTypeGenerator<?> generator = PrimaryTypeGenerator.create()
        .withElementName("SampleSequence")
        .withInterface(CharSequence.class.getName());

    long numMethodsWithoutTransformer = CoreTestingUtils.registerCompilationUnit(env, generator.withAllMethodsImplemented()).methods().stream().count();
    long numMethodsWithTransformer = CoreTestingUtils.registerCompilationUnit(env, generator.withAllMethodsImplemented(transformer)).methods().stream().count();
    assertEquals(numMethodsWithoutTransformer - 1 /* the method removed by the transformer */, numMethodsWithTransformer);
  }

  private static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
    if ("charAt".equals(input.model().elementName())) {
      return null;
    }
    return input.requestDefaultWorkingCopy();
  }

  @Test
  public void testTransformerRemovingAnnotationElement(IJavaEnvironment env) {
    String annotationElementNameToRemove = "strings";
    IType classWithAnnotationArrayValues = env.requireType(ClassWithAnnotationWithArrayValues.class.getName());
    IAnnotation firstAnnotation = classWithAnnotationArrayValues.annotations().withName(AnnotationWithArrayValues.class.getName()).first().get();
    assertFalse(firstAnnotation.element(annotationElementNameToRemove).get().isDefault());

    IWorkingCopyTransformer removeElementTransformer = new DefaultWorkingCopyTransformer() {
      @Override
      public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
        if (annotationElementNameToRemove.equals(input.model().elementName())) {
          return null; // remove the "strings" element from all annotations
        }
        return super.transformAnnotationElement(input);
      }
    };

    assertTrue(classWithAnnotationArrayValues.toWorkingCopy().annotations().findAny().get().element(annotationElementNameToRemove).isPresent()); // is not removed with default transform
    assertFalse(classWithAnnotationArrayValues.toWorkingCopy(removeElementTransformer).annotations().findAny().get().element(annotationElementNameToRemove).isPresent()); // is removed because of transformer
  }

  @Test
  public void testTransformerRemovingAnnotations(IJavaEnvironment env) {
    ICompilationUnit baseType = env.requireType(ClassWithAnnotationWithArrayValues.class.getName()).requireCompilationUnit();
    assertEquals(10, numberOfAnnotationsIn(baseType));

    IWorkingCopyTransformer removeAllAnnotationsTransformer = new DefaultWorkingCopyTransformer() {
      @Override
      public IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input) {
        return null;
      }
    };
    ICompilationUnitGenerator<?> generator = baseType.toWorkingCopy(removeAllAnnotationsTransformer);
    generator.mainType().get().withElementName("OtherClass"); // change the name so that the original class is not changed in the IJavaEnvironment. Otherwise it is modified for later tests
    IType baseTypeWithoutAnnotations = assertNoCompileErrors(env, generator);
    assertEquals(0, numberOfAnnotationsIn(baseTypeWithoutAnnotations));
  }

  private static int numberOfAnnotationsIn(IJavaElement element) {
    AtomicInteger numberOfAnnotations = new AtomicInteger();
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
    P_TestingWorkingCopyTransformer t = Optional.ofNullable(transformer)
        .orElseGet(P_TestingWorkingCopyTransformer::new);
    ICompilationUnitGenerator<?> generator = env.requireType(fixture.getName())
        .requireCompilationUnit()
        .toWorkingCopy(t);

    // build the source here as some of the components are transformed during generation time and not during setup time.
    IType primaryType = CoreTestingUtils.registerCompilationUnit(env, generator);

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
    public IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
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
    public String transformPackage(ITransformInput<IPackage, String> input) {
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
