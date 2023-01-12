/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.transformer;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
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

/**
 * <h3>{@link DefaultWorkingCopyTransformer}</h3>
 * <p>
 * Default {@link IWorkingCopyTransformer} implementation that does not apply any modification and always uses the
 * default working copy (as returned by {@link ITransformInput#requestDefaultWorkingCopy()}).
 *
 * @since 8.0.0
 */
public class DefaultWorkingCopyTransformer implements IWorkingCopyTransformer {

  @Override
  public IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input) {
    return transformElement(input);
  }

  @Override
  public ICompilationUnitGenerator<?> transformCompilationUnit(ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>> input) {
    return transformElement(input);
  }

  @Override
  public ITypeParameterGenerator<?> transformTypeParameter(ITransformInput<ITypeParameter, ITypeParameterGenerator<?>> input) {
    return transformElement(input);
  }

  @Override
  public IMethodParameterGenerator<?> transformMethodParameter(ITransformInput<IMethodParameter, IMethodParameterGenerator<?>> input) {
    return transformElement(input);
  }

  @Override
  public IFieldGenerator<?> transformField(ITransformInput<IField, IFieldGenerator<?>> input) {
    return transformElement(input);
  }

  @Override
  public IMethodGenerator<?, ?> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ?>> input) {
    return transformElement(input);
  }

  @Override
  public ITypeGenerator<?> transformType(ITransformInput<IType, ITypeGenerator<?>> input) {
    return transformElement(input);
  }

  @Override
  public ITypeGenerator<?> transformUnresolvedType(ITransformInput<IUnresolvedType, ITypeGenerator<?>> input) {
    return transformElement(input);
  }

  @Override
  public PackageGenerator transformPackage(ITransformInput<IPackage, PackageGenerator> input) {
    return transformElement(input);
  }

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
    return transformElement(input);
  }

  @Override
  public CharSequence transformImport(ITransformInput<IImport, CharSequence> input) {
    return transformElement(input);
  }

  /**
   * Default transformation implementation for all elements.
   * 
   * @param input
   *          The {@link ITransformInput} that holds the input {@link IJavaElement} and access to the original default
   *          transformation.
   * @param <M>
   *          The {@link IJavaElement model} to transform
   * @param <R>
   *          The created source generator
   * @return The created source generator
   */
  @SuppressWarnings("MethodMayBeStatic")
  protected <M extends IJavaElement, R> R transformElement(ITransformInput<M, R> input) {
    return input.requestDefaultWorkingCopy();
  }
}
