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
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
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

  private Function<IAnnotationGenerator<?>, IAnnotationGenerator<?>> m_annotationMapper = Function.identity();
  private Function<ICompilationUnitGenerator<?>, ICompilationUnitGenerator<?>> m_compilationUnitMapper = Function.identity();
  private Function<ITypeParameterGenerator<?>, ITypeParameterGenerator<?>> m_typeParameterMapper = Function.identity();
  private Function<IMethodParameterGenerator<?>, IMethodParameterGenerator<?>> m_methodParameterMapper = Function.identity();
  private Function<IFieldGenerator<?>, IFieldGenerator<?>> m_fieldMapper = Function.identity();
  private Function<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> m_methodMapper = Function.identity();
  private Function<ITypeGenerator<?>, ITypeGenerator<?>> m_typeMapper = Function.identity();
  private Function<ITypeGenerator<?>, ITypeGenerator<?>> m_unresolvedTypeMapper = Function.identity();
  private Function<String, String> m_packageMapper = Function.identity();
  private Function<ISourceGenerator<IExpressionBuilder<?>>, ISourceGenerator<IExpressionBuilder<?>>> m_annotationElementMapper = Function.identity();
  private Function<CharSequence, CharSequence> m_importMapper = Function.identity();

  public Function<IAnnotationGenerator<?>, IAnnotationGenerator<?>> annotationMapper() {
    return m_annotationMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withAnnotationMapper(Function<IAnnotationGenerator<?>, IAnnotationGenerator<?>> annotationFunction) {
    m_annotationMapper = identityIfNull(annotationFunction);
    return this;
  }

  public Function<ICompilationUnitGenerator<?>, ICompilationUnitGenerator<?>> compilationUnitMapper() {
    return m_compilationUnitMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withCompilationUnitMapper(Function<ICompilationUnitGenerator<?>, ICompilationUnitGenerator<?>> compilationUnitFunction) {
    m_compilationUnitMapper = identityIfNull(compilationUnitFunction);
    return this;
  }

  public Function<ITypeParameterGenerator<?>, ITypeParameterGenerator<?>> typeParameterMapper() {
    return m_typeParameterMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withTypeParameterMapper(Function<ITypeParameterGenerator<?>, ITypeParameterGenerator<?>> typeParameterFunction) {
    m_typeParameterMapper = identityIfNull(typeParameterFunction);
    return this;
  }

  public Function<IMethodParameterGenerator<?>, IMethodParameterGenerator<?>> methodParameterMapper() {
    return m_methodParameterMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withMethodParameterMapper(Function<IMethodParameterGenerator<?>, IMethodParameterGenerator<?>> methodParameterFunction) {
    m_methodParameterMapper = identityIfNull(methodParameterFunction);
    return this;
  }

  public Function<IFieldGenerator<?>, IFieldGenerator<?>> fieldMapper() {
    return m_fieldMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withFieldMapper(Function<IFieldGenerator<?>, IFieldGenerator<?>> fieldFunction) {
    m_fieldMapper = identityIfNull(fieldFunction);
    return this;
  }

  public Function<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> methodMapper() {
    return m_methodMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withMethodMapper(Function<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> methodFunction) {
    m_methodMapper = identityIfNull(methodFunction);
    return this;
  }

  public Function<ITypeGenerator<?>, ITypeGenerator<?>> typeMapper() {
    return m_typeMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withTypeMapper(Function<ITypeGenerator<?>, ITypeGenerator<?>> typeFunction) {
    m_typeMapper = identityIfNull(typeFunction);
    return this;
  }

  public Function<ITypeGenerator<?>, ITypeGenerator<?>> unresolvedTypeMapper() {
    return m_unresolvedTypeMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withUnresolvedTypeMapper(Function<ITypeGenerator<?>, ITypeGenerator<?>> unresolvedTypeFunction) {
    m_unresolvedTypeMapper = identityIfNull(unresolvedTypeFunction);
    return this;
  }

  public Function<String, String> packageMapper() {
    return m_packageMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withPackageMapper(Function<String, String> packageFunction) {
    m_packageMapper = identityIfNull(packageFunction);
    return this;
  }

  public Function<ISourceGenerator<IExpressionBuilder<?>>, ISourceGenerator<IExpressionBuilder<?>>> annotationElementMapper() {
    return m_annotationElementMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withAnnotationElementMapper(Function<ISourceGenerator<IExpressionBuilder<?>>, ISourceGenerator<IExpressionBuilder<?>>> annotationElementFunction) {
    m_annotationElementMapper = identityIfNull(annotationElementFunction);
    return this;
  }

  public Function<CharSequence, CharSequence> importMapper() {
    return m_importMapper;
  }

  public SimpleWorkingCopyTransformerBuilder withImportMapper(Function<CharSequence, CharSequence> importFunction) {
    m_importMapper = identityIfNull(importFunction);
    return this;
  }

  protected static <T> Function<T, T> identityIfNull(Function<T, T> candidate) {
    return Optional.ofNullable(candidate).orElseGet(Function::identity);
  }

  public IWorkingCopyTransformer build() {
    return new IWorkingCopyTransformer() {

      @Override
      public IAnnotationGenerator<?> transformAnnotation(ITransformInput<IAnnotation, IAnnotationGenerator<?>> input) {
        return annotationMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public ICompilationUnitGenerator<?> transformCompilationUnit(ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>> input) {
        return compilationUnitMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public ITypeParameterGenerator<?> transformTypeParameter(ITransformInput<ITypeParameter, ITypeParameterGenerator<?>> input) {
        return typeParameterMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public IMethodParameterGenerator<?> transformMethodParameter(ITransformInput<IMethodParameter, IMethodParameterGenerator<?>> input) {
        return methodParameterMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public IFieldGenerator<?> transformField(ITransformInput<IField, IFieldGenerator<?>> input) {
        return fieldMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
        return methodMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public ITypeGenerator<?> transformType(ITransformInput<IType, ITypeGenerator<?>> input) {
        return typeMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public ITypeGenerator<?> transformUnresolvedType(ITransformInput<IUnresolvedType, ITypeGenerator<?>> input) {
        return unresolvedTypeMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public String transformPackage(ITransformInput<IPackage, String> input) {
        return packageMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
        return annotationElementMapper().apply(input.requestDefaultWorkingCopy());
      }

      @Override
      public CharSequence transformImport(ITransformInput<IImport, CharSequence> input) {
        return importMapper().apply(input.requestDefaultWorkingCopy());
      }
    };
  }
}
