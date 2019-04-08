/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.generator.transformer;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link IWorkingCopyTransformer}</h3>
 * <p>
 * Transforms a {@link IJavaElement} model into a working copy {@link ISourceGenerator}.
 * <p>
 * <b>Examples:</b><br>
 * <br>
 * <b>Override Methods with modifications on the overridden components:</b>
 *
 * <pre>
 * final IWorkingCopyTransformer transformer = new DefaultWorkingCopyTransformer() {
 *   &#64;Override
 *   public IMethodGenerator&#60;?, ? extends IMethodBodyBuilder&#60;?&#62;&#62; transformMethod(final ITransformInput&#60;IMethod, IMethodGenerator&#60;?, ? extends IMethodBodyBuilder&#60;?&#62;&#62;&#62; input) {
 *     final IMethod templateMethod = input.model();
 *     final IMethodGenerator&#60;?, ? extends IMethodBodyBuilder&#60;?&#62;&#62; overrideGenerator = input.requestDefaultWorkingCopy();
 *     switch (templateMethod.elementName()) {
 *       case "toString":
 *         // provide method body for toString method
 *         return overrideGenerator.withBody(b -&#62; b.returnClause().stringLiteral("SampleCloseable class").semicolon());
 *       case "close":
 *         // remove throws declaration for close method
 *         return overrideGenerator.withoutException(Exception.class.getName());
 *       default:
 *         return overrideGenerator;
 *     }
 *   }
 * };
 *
 * final PrimaryTypeGenerator&#60;?&#62; generator = PrimaryTypeGenerator.create()
 *     .withElementName("SampleCloseable")
 *     .withInterface(AutoCloseable.class.getName()) // defines the methods that can be overridden
 *     .withMethod(MethodOverrideGenerator.createOverride(transformer)
 *         .withElementName("toString")) // override toString
 *     .withAllMethodsImplemented(transformer); // override all methods required by super types.
 * </pre>
 *
 * <b>Convert to working copy applying modifications:</b>
 *
 * <pre>
 * final ICompilationUnit icu = env.requireType(Long.class.getName()).requireCompilationUnit();
 * final ICompilationUnitGenerator&#60;?&#62; workingCopy = icu.toWorkingCopy(
 *     new SimpleWorkingCopyTransformerBuilder()
 *         .withAnnotationMapper(this::transformAnnotation)
 *         .build());
 *
 * private IAnnotationGenerator&#60;?&#62; transformAnnotation(final IAnnotationGenerator&#60;?&#62; gen) {
 *   if (SuppressWarnings.class.getName().equals(gen.elementName().orElse(null))) {
 *     // modify all suppress-warning annotations to suppress all warnings
 *     gen.withElement("value", b -&#62; b.stringLiteral("all"));
 *   }
 *   return gen;
 * }
 * </pre>
 *
 * @see DefaultWorkingCopyTransformer
 * @see SimpleWorkingCopyTransformerBuilder
 * @since 8.0.0
 */
public interface IWorkingCopyTransformer {

  /**
   * <h3>{@link ITransformInput}</h3>
   */
  interface ITransformInput<MODEL extends IJavaElement, GENERATOR> {
    /**
     * @return The model {@link IJavaElement} that should be converted to a working copy. Never returns {@code null}.
     */
    MODEL model();

    /**
     * Requests the default working copy for the {@link #model()} that results in structurally the same source as the
     * model was built on.
     *
     * @return The default source generator. This instance is created on first request. Subsequent requests always
     *         return the same instance. Never returns {@code null}.<br>
     *         If this instance is modified as part of transform operation of a single {@link #model()}, this
     *         modification is reflected on subsequent calls!
     */
    GENERATOR requestDefaultWorkingCopy();
  }

  /**
   * Transforms the {@link IAnnotation} specified into a working copy using the specified transformer.
   *
   * @param a
   *          The {@link IAnnotation} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link IAnnotationGenerator} to use as working copy for the {@link IAnnotation} specified.
   */
  static IAnnotationGenerator<?> transformAnnotation(IAnnotation a, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<IAnnotation, IAnnotationGenerator<?>> transform(transformer, a,
        () -> AnnotationGenerator.create(a, transformer), (t, i) -> t.transformAnnotation(i));
  }

  /**
   * Transforms the {@link ICompilationUnit} specified into a working copy using the specified transformer.
   *
   * @param icu
   *          The {@link ICompilationUnit} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ICompilationUnitGenerator} to use as working copy for the {@link ICompilationUnit} specified.
   */
  static ICompilationUnitGenerator<?> transformCompilationUnit(ICompilationUnit icu, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<ICompilationUnit, ICompilationUnitGenerator<?>> transform(transformer, icu,
        () -> CompilationUnitGenerator.create(icu, transformer), (t, i) -> t.transformCompilationUnit(i));
  }

  /**
   * Transforms the {@link ITypeParameter} specified into a working copy using the specified transformer.
   *
   * @param param
   *          The {@link ITypeParameter} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ITypeParameterGenerator} to use as working copy for the {@link ITypeParameter} specified.
   */
  static ITypeParameterGenerator<?> transformTypeParameter(ITypeParameter param, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<ITypeParameter, ITypeParameterGenerator<?>> transform(transformer, param,
        () -> TypeParameterGenerator.create(param), (t, i) -> t.transformTypeParameter(i));
  }

  /**
   * Transforms the {@link IMethodParameter} specified into a working copy using the specified transformer.
   *
   * @param mp
   *          The {@link IMethodParameter} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link IMethodParameterGenerator} to use as working copy for the {@link IMethodParameter} specified.
   */
  static IMethodParameterGenerator<?> transformMethodParameter(IMethodParameter mp, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<IMethodParameter, IMethodParameterGenerator<?>> transform(transformer, mp,
        () -> MethodParameterGenerator.create(mp, transformer), (t, i) -> t.transformMethodParameter(i));
  }

  /**
   * Transforms the {@link IField} specified into a working copy using the specified transformer.
   *
   * @param f
   *          The {@link IField} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link IFieldGenerator} to use as working copy for the {@link IField} specified.
   */
  static IFieldGenerator<?> transformField(IField f, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<IField, IFieldGenerator<?>> transform(transformer, f,
        () -> FieldGenerator.create(f, transformer), (t, i) -> t.transformField(i));
  }

  /**
   * Transforms the {@link IMethod} specified into a working copy using the specified transformer.
   *
   * @param m
   *          The {@link IMethod} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link IMethodGenerator} to use as working copy for the {@link IMethod} specified.
   */
  static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(IMethod m, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> transform(transformer, m,
        () -> MethodGenerator.create(m, transformer), (t, i) -> t.transformMethod(i));
  }

  /**
   * Transforms the {@link IType} specified into a working copy using the specified transformer.
   *
   * @param type
   *          The {@link IType} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IType} specified.
   */
  static ITypeGenerator<?> transformType(IType type, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<IType, ITypeGenerator<?>> transform(transformer, type,
        () -> TypeGenerator.create(type, transformer).setDeclaringFullyQualifiedName(type.qualifier()), (t, i) -> t.transformType(i));
  }

  /**
   * Transforms the {@link IUnresolvedType} specified into a working copy using the specified transformer.
   *
   * @param ut
   *          The {@link IUnresolvedType} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IUnresolvedType} specified.
   */
  static ITypeGenerator<?> transformUnresolvedType(IUnresolvedType ut, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<IUnresolvedType, ITypeGenerator<?>> transform(transformer, ut,
        () -> ut.type()
            .<ITypeGenerator<?>> map(t -> TypeGenerator.create(t, transformer))
            .orElseGet(() -> TypeGenerator.create()
                .setDeclaringFullyQualifiedName(JavaTypes.qualifier(ut.name().replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT)))
                .withElementName(ut.elementName())),
        (t, i) -> t.transformUnresolvedType(i));
  }

  /**
   * Transforms the {@link IPackage} specified into a {@link String} using the specified transformer.
   *
   * @param p
   *          The {@link IPackage} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link String} to use as package (without package key word and semicolon). May be {@code null} if the
   *         default package should be used.
   */
  static String transformPackage(IPackage p, IWorkingCopyTransformer transformer) {
    return transform(transformer, p, p::elementName, (t, i) -> t.transformPackage(i));
  }

  /**
   * Transforms the {@link IAnnotationElement} specified into a working copy using the specified transformer.
   *
   * @param ae
   *          The {@link IAnnotationElement} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ISourceGenerator} to use as working copy for the {@link IAnnotationElement} specified.
   */
  static ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(IAnnotationElement ae, IWorkingCopyTransformer transformer) {
    return transform(transformer, ae,
        () -> ae.value().toWorkingCopy(transformer), (t, i) -> t.transformAnnotationElement(i));
  }

  /**
   * Transforms the {@link IImport} specified into a {@link CharSequence} using the specified transformer.
   *
   * @param imp
   *          The {@link IImport} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link CharSequence} to use as import (without import key word and semicolon). Must not be
   *         {@code null}.
   */
  static CharSequence transformImport(IImport imp, IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.<IImport, CharSequence> transform(transformer, imp, imp::name, (t, i) -> t.transformImport(i));
  }

  /**
   * Applies a transformation
   *
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @param model
   *          The model object to transform. May not be {@code null}.
   * @param generatorSupplier
   *          {@link Supplier} to create a default transformer.
   * @param transformerCall
   *          The transformation call to execute if a transformer is present. The inputs of the {@link BiFunction} are
   *          never {@code null}.
   * @return The transformed model.
   */
  static <M extends IJavaElement, G> G transform(IWorkingCopyTransformer transformer, M model, Supplier<G> generatorSupplier,
      BiFunction<IWorkingCopyTransformer, ITransformInput<M, G>, G> transformerCall) {
    return Optional.ofNullable(transformer)
        .map(t -> transformerCall.apply(t, new TransformInput<>(model, generatorSupplier)))
        .orElseGet(generatorSupplier);
  }

  /**
   * Transforms an {@link IAnnotation} to an {@link IAnnotationGenerator}. Either the default working copy can be
   * modified and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link IAnnotationGenerator}
   * can be created that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IAnnotation} and a default
   *          {@link IAnnotationGenerator}. Is never {@code null}.
   * @return The {@link IAnnotationGenerator} to use as working copy for the {@link IAnnotation}. Must not be
   *         {@code null}.
   */
  IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input);

  /**
   * Transforms an {@link ICompilationUnit} to an {@link ICompilationUnitGenerator}. Either the default working copy can
   * be modified and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new
   * {@link ICompilationUnitGenerator} can be created that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link ICompilationUnit} and a default
   *          {@link ICompilationUnitGenerator}. Is never {@code null}.
   * @return The {@link ICompilationUnitGenerator} to use as working copy for the {@link ICompilationUnit}. Must not be
   *         {@code null}.
   */
  ICompilationUnitGenerator<?> transformCompilationUnit(ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>> input);

  /**
   * Transforms an {@link ITypeParameter} to an {@link ITypeParameterGenerator}. Either the default working copy can be
   * modified and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new
   * {@link ITypeParameterGenerator} can be created that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link ITypeParameter} and a default
   *          {@link ITypeParameterGenerator}. Is never {@code null}.
   * @return The {@link ITypeParameterGenerator} to use as working copy for the {@link ITypeParameter}. Must not be
   *         {@code null}.
   */
  ITypeParameterGenerator<?> transformTypeParameter(ITransformInput<ITypeParameter, ITypeParameterGenerator<?>> input);

  /**
   * Transforms an {@link IMethodParameter} to an {@link IMethodParameterGenerator}. Either the default working copy can
   * be modified and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new
   * {@link IMethodParameterGenerator} can be created that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IMethodParameter} and a default
   *          {@link IMethodParameterGenerator}. Is never {@code null}.
   * @return The {@link IMethodParameterGenerator} to use as working copy for the {@link IMethodParameter}. Must not be
   *         {@code null}.
   */
  IMethodParameterGenerator<?> transformMethodParameter(ITransformInput<IMethodParameter, IMethodParameterGenerator<?>> input);

  /**
   * Transforms an {@link IField} to an {@link IFieldGenerator}. Either the default working copy can be modified and
   * returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link IFieldGenerator} can be created that
   * should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IField} and a default {@link IFieldGenerator}. Is
   *          never {@code null}.
   * @return The {@link IFieldGenerator} to use as working copy for the {@link IField}. Must not be {@code null}.
   */
  IFieldGenerator<?> transformField(ITransformInput<IField, IFieldGenerator<?>> input);

  /**
   * Transforms an {@link IMethod} to an {@link IMethodGenerator}. Either the default working copy can be modified and
   * returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link IMethodGenerator} can be created
   * that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IMethod} and a default {@link IMethodGenerator}. Is
   *          never {@code null}.
   * @return The {@link IMethodGenerator} to use as working copy for the {@link IMethod}. Must not be {@code null}.
   */
  IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input);

  /**
   * Transforms an {@link IType} to an {@link ITypeGenerator}. Either the default working copy can be modified and
   * returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link ITypeGenerator} can be created that
   * should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IType} and a default {@link ITypeGenerator}. Is never
   *          {@code null}.
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IType}. Must not be {@code null}.
   */
  ITypeGenerator<?> transformType(ITransformInput<IType, ITypeGenerator<?>> input);

  /**
   * Transforms an {@link IUnresolvedType} to an {@link ITypeGenerator}. Either the default working copy can be modified
   * and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link ITypeGenerator} can be created
   * that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IUnresolvedType} and a default
   *          {@link ITypeGenerator}. Is never {@code null}.
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IUnresolvedType}. Must not be
   *         {@code null}.
   */
  ITypeGenerator<?> transformUnresolvedType(ITransformInput<IUnresolvedType, ITypeGenerator<?>> input);

  /**
   * Transforms an {@link IPackage} to a {@link String}.
   *
   * @param input
   *          The transformation input providing the source {@link IPackage} and a default {@link String} as package
   *          declaration (same as {@link IPackage#elementName()}). Is never {@code null}.
   * @return The {@link String} to use as package (without package key word and semicolon). May be {@code null} if the
   *         default package should be used.
   */
  String transformPackage(ITransformInput<IPackage, String> input);

  /**
   * Transforms an {@link IAnnotationElement} to an {@link ISourceGenerator}. Either the default working copy can be
   * modified and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link ISourceGenerator} can
   * be created that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IAnnotationElement} and a default
   *          {@link ISourceGenerator}. Is never {@code null}.
   * @return The {@link ISourceGenerator} to use as working copy for the {@link IAnnotationElement}. Must not be
   *         {@code null}.
   */
  ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input);

  /**
   * Transforms an {@link IImport} to a {@link CharSequence}.
   *
   * @param input
   *          The transformation input providing the source {@link IImport} and a default {@link CharSequence} as import
   *          (same as {@link IImport#name()}). Is never {@code null}.
   * @return The {@link CharSequence} to use as import (without import key word and semicolon). Must not be
   *         {@code null}.
   */
  CharSequence transformImport(ITransformInput<IImport, CharSequence> input);
}