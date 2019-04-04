/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.builder.java.comment;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;

/**
 * <h3>{@link IDefaultElementCommentGeneratorSpi}</h3>
 * <p>
 * Service provider interface for default element comment strategies.
 * <p>
 * The SPI implementation can be activated using
 * {@link JavaElementCommentBuilder#setCommentGeneratorSpi(IDefaultElementCommentGeneratorSpi)}
 *
 * @since 6.1.0
 * @see JavaElementCommentBuilder
 * @see IJavaElementCommentBuilder
 */
public interface IDefaultElementCommentGeneratorSpi {

  /**
   * @param target
   *          The {@link ICompilationUnitGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given
   *         {@link ICompilationUnitGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createCompilationUnitComment(ICompilationUnitGenerator<?> target);

  /**
   * @param target
   *          The {@link ITypeGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link ITypeGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createTypeComment(ITypeGenerator<?> target);

  /**
   * @param target
   *          The {@link IMethodGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link IMethodGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createMethodComment(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> target);

  /**
   * @param target
   *          The getter {@link IMethodGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link IMethodGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createGetterMethodComment(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> target);

  /**
   * @param target
   *          The setter {@link IMethodGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link IMethodGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createSetterMethodComment(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> target);

  /**
   * @param target
   *          The {@link IFieldGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link IFieldGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createFieldComment(IFieldGenerator<?> target);
}
