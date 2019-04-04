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
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public ICompilationUnitGenerator<?> transformCompilationUnit(ITransformInput<ICompilationUnit, ICompilationUnitGenerator<?>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public ITypeParameterGenerator<?> transformTypeParameter(ITransformInput<ITypeParameter, ITypeParameterGenerator<?>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public IMethodParameterGenerator<?> transformMethodParameter(ITransformInput<IMethodParameter, IMethodParameterGenerator<?>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public IFieldGenerator<?> transformField(ITransformInput<IField, IFieldGenerator<?>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> transformMethod(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public ITypeGenerator<?> transformType(ITransformInput<IType, ITypeGenerator<?>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public ITypeGenerator<?> transformUnresolvedType(ITransformInput<IUnresolvedType, ITypeGenerator<?>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public String transformPackage(ITransformInput<IPackage, String> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
    return input.requestDefaultWorkingCopy();
  }

  @Override
  public CharSequence transformImport(ITransformInput<IImport, CharSequence> input) {
    return input.requestDefaultWorkingCopy();
  }
}
