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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IImport;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.java.model.api.IUnresolvedType;

/**
 * <h3>{@link IWorkingCopyTransformer}</h3>
 * <p>
 * Transforms a {@link IJavaElement} model into a working copy {@link ISourceGenerator}.
 * <p>
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
   * @return The {@link IAnnotationGenerator} to use as working copy for the {@link IAnnotation} specified or an empty
   *         {@link Optional} if the annotation should be removed.
   */
  static Optional<IAnnotationGenerator<?>> transformAnnotation(IAnnotation a, IWorkingCopyTransformer transformer) {
    return transform(transformer, a, () -> a.toWorkingCopy(transformer), (t, i) -> t.transformAnnotation(i));
  }

  /**
   * Transforms the {@link ICompilationUnit} specified into a working copy using the specified transformer.
   *
   * @param icu
   *          The {@link ICompilationUnit} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ICompilationUnitGenerator} to use as working copy for the {@link ICompilationUnit} specified or
   *         an empty {@link Optional} if the compilation unit should be removed.
   */
  static Optional<ICompilationUnitGenerator<?>> transformCompilationUnit(ICompilationUnit icu, IWorkingCopyTransformer transformer) {
    return transform(transformer, icu, () -> icu.toWorkingCopy(transformer), (t, i) -> t.transformCompilationUnit(i));
  }

  /**
   * Transforms the {@link ITypeParameter} specified into a working copy using the specified transformer.
   *
   * @param param
   *          The {@link ITypeParameter} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ITypeParameterGenerator} to use as working copy for the {@link ITypeParameter} specified or an
   *         empty {@link Optional} if the type parameter should be removed.
   */
  static Optional<ITypeParameterGenerator<?>> transformTypeParameter(ITypeParameter param, IWorkingCopyTransformer transformer) {
    return transform(transformer, param, () -> param.toWorkingCopy(transformer), (t, i) -> t.transformTypeParameter(i));
  }

  /**
   * Transforms the {@link IMethodParameter} specified into a working copy using the specified transformer.
   *
   * @param mp
   *          The {@link IMethodParameter} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link IMethodParameterGenerator} to use as working copy for the {@link IMethodParameter} specified or
   *         an empty {@link Optional} if the parameter should be removed.
   */
  static Optional<IMethodParameterGenerator<?>> transformMethodParameter(IMethodParameter mp, IWorkingCopyTransformer transformer) {
    return transform(transformer, mp, () -> mp.toWorkingCopy(transformer), (t, i) -> t.transformMethodParameter(i));
  }

  /**
   * Transforms the {@link IField} specified into a working copy using the specified transformer.
   *
   * @param f
   *          The {@link IField} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link IFieldGenerator} to use as working copy for the {@link IField} specified or an empty
   *         {@link Optional} if the field should be removed.
   */
  static Optional<IFieldGenerator<?>> transformField(IField f, IWorkingCopyTransformer transformer) {
    return transform(transformer, f, () -> f.toWorkingCopy(transformer), (t, i) -> t.transformField(i));
  }

  /**
   * Transforms the {@link IMethod} specified into a working copy using the specified transformer.
   *
   * @param m
   *          The {@link IMethod} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link IMethodGenerator} to use as working copy for the {@link IMethod} specified or an empty
   *         {@link Optional} if the method should be removed.
   */
  static Optional<IMethodGenerator<?, ?>> transformMethod(IMethod m, IWorkingCopyTransformer transformer) {
    return transform(transformer, m, () -> m.toWorkingCopy(transformer), (t, i) -> t.transformMethod(i));
  }

  /**
   * Transforms the {@link IType} specified into a working copy using the specified transformer.
   *
   * @param type
   *          The {@link IType} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IType} specified or an empty
   *         {@link Optional} if the type should be removed.
   */
  static Optional<ITypeGenerator<?>> transformType(IType type, IWorkingCopyTransformer transformer) {
    return transform(transformer, type, () -> type.toWorkingCopy(transformer), (t, i) -> t.transformType(i));
  }

  /**
   * Transforms the {@link IUnresolvedType} specified into a working copy using the specified transformer.
   *
   * @param ut
   *          The {@link IUnresolvedType} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IUnresolvedType} specified or an empty
   *         {@link Optional} if the type should be removed.
   */
  static Optional<ITypeGenerator<?>> transformUnresolvedType(IUnresolvedType ut, IWorkingCopyTransformer transformer) {
    return transform(transformer, ut,
        () -> ut.toWorkingCopy(transformer),
        (t, i) -> t.transformUnresolvedType(i));
  }

  /**
   * Transforms the {@link IPackage} specified into a {@link String} using the specified transformer.
   *
   * @param p
   *          The {@link IPackage} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link String} to use as package (without package key word and semicolon) or an empty {@link Optional}
   *         if the package should be removed (move to the default package).
   */
  static Optional<PackageGenerator> transformPackage(IPackage p, IWorkingCopyTransformer transformer) {
    return transform(transformer, p, () -> p.toWorkingCopy(transformer), (t, i) -> t.transformPackage(i));
  }

  /**
   * Transforms the {@link IAnnotationElement} specified into a working copy using the specified transformer.
   *
   * @param ae
   *          The {@link IAnnotationElement} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link ISourceGenerator} to use as working copy for the value (without annotation element name and
   *         equal sign) of the {@link IAnnotationElement} specified or an empty {@link Optional} if the element should
   *         be removed.
   */
  static Optional<ISourceGenerator<IExpressionBuilder<?>>> transformAnnotationElement(IAnnotationElement ae, IWorkingCopyTransformer transformer) {
    return transform(transformer, ae,
        () -> ae.value().toWorkingCopy(transformer),
        (t, i) -> t.transformAnnotationElement(i));
  }

  /**
   * Transforms the {@link IImport} specified into a {@link CharSequence} using the specified transformer.
   *
   * @param imp
   *          The {@link IImport} to transform. Must not be {@code null}.
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @return The {@link CharSequence} to use as import (without import key word and semicolon) or an empty
   *         {@link Optional} if the import should be removed.
   */
  static Optional<CharSequence> transformImport(IImport imp, IWorkingCopyTransformer transformer) {
    return transform(transformer, imp, imp::name, (t, i) -> t.transformImport(i));
  }

  /**
   * Applies a transformation
   *
   * @param transformer
   *          The transformer to use. May be {@code null} if a default transformation should be performed.
   * @param model
   *          The model object to transform. May not be {@code null}.
   * @param defaultGeneratorSupplier
   *          {@link Supplier} to create a default transformer.
   * @param transformerCall
   *          The transformation call to execute if a transformer is present. The inputs of the {@link BiFunction} are
   *          never {@code null}.
   * @return The transformed model or an empty {@link Optional} if the model element should not be generated at all.
   */
  static <M extends IJavaElement, G> Optional<G> transform(IWorkingCopyTransformer transformer, M model, Supplier<G> defaultGeneratorSupplier,
      BiFunction<IWorkingCopyTransformer, ITransformInput<M, G>, G> transformerCall) {
    if (transformer == null) {
      return Optional.of(defaultGeneratorSupplier.get()); // default transformers are not allowed to return a null generator
    }
    return Optional.ofNullable(transformerCall.apply(transformer, new TransformInput<>(model, defaultGeneratorSupplier)));
  }

  /**
   * This function may be used as reference to remove elements during transformation.
   * <p>
   * <b>Example:</b>
   * <p>
   * 
   * <pre>
   * IWorkingCopyTransformer transformer = new SimpleWorkingCopyTransformerBuilder()
   *     .withMethodParameterMapper(IWorkingCopyTransformer::remove) // remove all method parameters
   *     .build();
   * </pre>
   * 
   * @param input
   *          The input element to remove.
   * @param <G>
   * @return always {@code null} which removes the element.
   */
  static <G> G remove(ITransformInput<?, G> input) {
    return null; // removes the element
  }

  /**
   * This method can be called if an {@link IWorkingCopyTransformer} would like to completely remove an element.
   * 
   * @param <G>
   * @return always {@code null} which removes the element.
   */
  default <G> G remove() {
    return remove(null);
  }

  /**
   * Transforms an {@link IAnnotation} to an {@link IAnnotationGenerator}. Either the default working copy can be
   * modified and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link IAnnotationGenerator}
   * can be created that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IAnnotation} and a default
   *          {@link IAnnotationGenerator}. Is never {@code null}.
   * @return The {@link IAnnotationGenerator} to use as working copy for the {@link IAnnotation}. May be {@code null} if
   *         the annotation should be removed (use {@link #remove()}).
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
   * @return The {@link ICompilationUnitGenerator} to use as working copy for the {@link ICompilationUnit}. May be
   *         {@code null} if the compilation unit should be removed (use {@link #remove()}).
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
   * @return The {@link ITypeParameterGenerator} to use as working copy for the {@link ITypeParameter}. May be
   *         {@code null} if the type parameter should be removed (use {@link #remove()}).
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
   * @return The {@link IMethodParameterGenerator} to use as working copy for the {@link IMethodParameter}. May be
   *         {@code null} if the method parameter should be removed (use {@link #remove()}).
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
   * @return The {@link IFieldGenerator} to use as working copy for the {@link IField}. May be {@code null} if the field
   *         should be removed (use {@link #remove()}).
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
   * @return The {@link IMethodGenerator} to use as working copy for the {@link IMethod}. May be {@code null} if the
   *         method should be removed (use {@link #remove()}).
   */
  IMethodGenerator<?, ?> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ?>> input);

  /**
   * Transforms an {@link IType} to an {@link ITypeGenerator}. Either the default working copy can be modified and
   * returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link ITypeGenerator} can be created that
   * should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IType} and a default {@link ITypeGenerator}. Is never
   *          {@code null}.
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IType}. May be {@code null} if the type
   *         should be removed (use {@link #remove()}).
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
   * @return The {@link ITypeGenerator} to use as working copy for the {@link IUnresolvedType}. May be {@code null} if
   *         the annotation should be removed (use {@link #remove()}).
   */
  ITypeGenerator<?> transformUnresolvedType(ITransformInput<IUnresolvedType, ITypeGenerator<?>> input);

  /**
   * Transforms an {@link IPackage} to a {@link PackageGenerator}.
   *
   * @param input
   *          The transformation input providing the source {@link IPackage} and a default {@link PackageGenerator}. Is
   *          never {@code null}.
   * @return The {@link PackageGenerator} to use as working copy. May be {@code null} if the default package should be
   *         used.
   */
  PackageGenerator transformPackage(ITransformInput<IPackage, PackageGenerator> input);

  /**
   * Transforms an {@link IAnnotationElement} to an {@link ISourceGenerator}. Either the default working copy can be
   * modified and returned ({@link ITransformInput#requestDefaultWorkingCopy()}) or a new {@link ISourceGenerator} can
   * be created that should be used.
   *
   * @param input
   *          The transformation input providing the source {@link IAnnotationElement} and a default
   *          {@link ISourceGenerator}. Is never {@code null}.
   * @return The {@link ISourceGenerator} to use as working copy for the {@link IAnnotationElement}. May be {@code null}
   *         if the annotation element should be removed (use {@link #remove()}).
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
