/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.builder.comment;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;

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
  ISourceGenerator<ICommentBuilder<?>> createMethodComment(IMethodGenerator<?, ?> target);

  /**
   * @param target
   *          The getter {@link IMethodGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link IMethodGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createGetterMethodComment(IMethodGenerator<?, ?> target);

  /**
   * @param target
   *          The setter {@link IMethodGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link IMethodGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createSetterMethodComment(IMethodGenerator<?, ?> target);

  /**
   * @param target
   *          The {@link IFieldGenerator} for which the default comment should be generated.
   * @return The {@link ISourceGenerator} that creates the default java comment for the given {@link IFieldGenerator}.
   */
  ISourceGenerator<ICommentBuilder<?>> createFieldComment(IFieldGenerator<?> target);
}
