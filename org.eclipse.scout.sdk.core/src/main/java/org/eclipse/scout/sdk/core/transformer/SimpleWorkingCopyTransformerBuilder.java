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
package org.eclipse.scout.sdk.core.transformer;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.ITransformInput;
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
 * <h3>{@link SimpleWorkingCopyTransformerBuilder}</h3>
 * <p>
 * Builder for simple {@link IWorkingCopyTransformer}s if the default working copy should be used (applying any
 * modifications).
 *
 * @since 8.0.0
 */
public class SimpleWorkingCopyTransformerBuilder {

  private Function<ITransformInput<IAnnotation, IAnnotationGenerator<?>>, IAnnotationGenerator<?>> m_annotationMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>>, ICompilationUnitGenerator<?>> m_compilationUnitMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<ITypeParameter, ITypeParameterGenerator<?>>, ITypeParameterGenerator<?>> m_typeParameterMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IMethodParameter, IMethodParameterGenerator<?>>, IMethodParameterGenerator<?>> m_methodParameterMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IField, IFieldGenerator<?>>, IFieldGenerator<?>> m_fieldMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>>, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> m_methodMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IType, ITypeGenerator<?>>, ITypeGenerator<?>> m_typeMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IUnresolvedType, ITypeGenerator<?>>, ITypeGenerator<?>> m_unresolvedTypeMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IPackage, PackageGenerator>, PackageGenerator> m_packageMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>>, ISourceGenerator<IExpressionBuilder<?>>> m_annotationElementMapper = ITransformInput::requestDefaultWorkingCopy;
  private Function<ITransformInput<IImport, CharSequence>, CharSequence> m_importMapper = ITransformInput::requestDefaultWorkingCopy;

  public Function<ITransformInput<IAnnotation, IAnnotationGenerator<?>>, IAnnotationGenerator<?>> annotationMapper() {
    return m_annotationMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withAnnotationMapper(Function<ITransformInput<IAnnotation, IAnnotationGenerator<?>>, IAnnotationGenerator<?>> annotationFunction) {
    m_annotationMapper = defaultIfNull(annotationFunction);
    return this;
  }

  public Function<ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>>, ICompilationUnitGenerator<?>> compilationUnitMapper() {
    return m_compilationUnitMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withCompilationUnitMapper(Function<ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>>, ICompilationUnitGenerator<?>> compilationUnitFunction) {
    m_compilationUnitMapper = defaultIfNull(compilationUnitFunction);
    return this;
  }

  public Function<ITransformInput<ITypeParameter, ITypeParameterGenerator<?>>, ITypeParameterGenerator<?>> typeParameterMapper() {
    return m_typeParameterMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withTypeParameterMapper(Function<ITransformInput<ITypeParameter, ITypeParameterGenerator<?>>, ITypeParameterGenerator<?>> typeParameterFunction) {
    m_typeParameterMapper = defaultIfNull(typeParameterFunction);
    return this;
  }

  public Function<ITransformInput<IMethodParameter, IMethodParameterGenerator<?>>, IMethodParameterGenerator<?>> methodParameterMapper() {
    return m_methodParameterMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withMethodParameterMapper(Function<ITransformInput<IMethodParameter, IMethodParameterGenerator<?>>, IMethodParameterGenerator<?>> methodParameterFunction) {
    m_methodParameterMapper = defaultIfNull(methodParameterFunction);
    return this;
  }

  public Function<ITransformInput<IField, IFieldGenerator<?>>, IFieldGenerator<?>> fieldMapper() {
    return m_fieldMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withFieldMapper(Function<ITransformInput<IField, IFieldGenerator<?>>, IFieldGenerator<?>> fieldFunction) {
    m_fieldMapper = defaultIfNull(fieldFunction);
    return this;
  }

  public Function<ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>>, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> methodMapper() {
    return m_methodMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withMethodMapper(Function<ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>>, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> methodFunction) {
    m_methodMapper = defaultIfNull(methodFunction);
    return this;
  }

  public Function<ITransformInput<IType, ITypeGenerator<?>>, ITypeGenerator<?>> typeMapper() {
    return m_typeMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withTypeMapper(Function<ITransformInput<IType, ITypeGenerator<?>>, ITypeGenerator<?>> typeFunction) {
    m_typeMapper = defaultIfNull(typeFunction);
    return this;
  }

  public Function<ITransformInput<IUnresolvedType, ITypeGenerator<?>>, ITypeGenerator<?>> unresolvedTypeMapper() {
    return m_unresolvedTypeMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withUnresolvedTypeMapper(Function<ITransformInput<IUnresolvedType, ITypeGenerator<?>>, ITypeGenerator<?>> unresolvedTypeFunction) {
    m_unresolvedTypeMapper = defaultIfNull(unresolvedTypeFunction);
    return this;
  }

  public Function<ITransformInput<IPackage, PackageGenerator>, PackageGenerator> packageMapper() {
    return m_packageMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withPackageMapper(Function<ITransformInput<IPackage, PackageGenerator>, PackageGenerator> packageFunction) {
    m_packageMapper = defaultIfNull(packageFunction);
    return this;
  }

  public Function<ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>>, ISourceGenerator<IExpressionBuilder<?>>> annotationElementMapper() {
    return m_annotationElementMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withAnnotationElementMapper(Function<ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>>, ISourceGenerator<IExpressionBuilder<?>>> annotationElementFunction) {
    m_annotationElementMapper = defaultIfNull(annotationElementFunction);
    return this;
  }

  public Function<ITransformInput<IImport, CharSequence>, CharSequence> importMapper() {
    return m_importMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withImportMapper(Function<ITransformInput<IImport, CharSequence>, CharSequence> importFunction) {
    m_importMapper = defaultIfNull(importFunction);
    return this;
  }

  protected static <M extends IJavaElement, G> Function<ITransformInput<M, G>, G> defaultIfNull(Function<ITransformInput<M, G>, G> candidate) {
    return Optional.ofNullable(candidate).orElseGet(() -> ITransformInput::requestDefaultWorkingCopy);
  }

  @SuppressWarnings("ReturnOfInnerClass")
  public IWorkingCopyTransformer build() {
    return new IWorkingCopyTransformer() {

      @Override
      public IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input) {
        return annotationMapper().apply(input);
      }

      @Override
      public ICompilationUnitGenerator<?> transformCompilationUnit(ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>> input) {
        return compilationUnitMapper().apply(input);
      }

      @Override
      public ITypeParameterGenerator<?> transformTypeParameter(ITransformInput<ITypeParameter, ITypeParameterGenerator<?>> input) {
        return typeParameterMapper().apply(input);
      }

      @Override
      public IMethodParameterGenerator<?> transformMethodParameter(ITransformInput<IMethodParameter, IMethodParameterGenerator<?>> input) {
        return methodParameterMapper().apply(input);
      }

      @Override
      public IFieldGenerator<?> transformField(ITransformInput<IField, IFieldGenerator<?>> input) {
        return fieldMapper().apply(input);
      }

      @Override
      public IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
        return methodMapper().apply(input);
      }

      @Override
      public ITypeGenerator<?> transformType(ITransformInput<IType, ITypeGenerator<?>> input) {
        return typeMapper().apply(input);
      }

      @Override
      public ITypeGenerator<?> transformUnresolvedType(ITransformInput<IUnresolvedType, ITypeGenerator<?>> input) {
        return unresolvedTypeMapper().apply(input);
      }

      @Override
      public PackageGenerator transformPackage(ITransformInput<IPackage, PackageGenerator> input) {
        return packageMapper().apply(input);
      }

      @Override
      public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
        return annotationElementMapper().apply(input);
      }

      @Override
      public CharSequence transformImport(ITransformInput<IImport, CharSequence> input) {
        return importMapper().apply(input);
      }
    };
  }
}
