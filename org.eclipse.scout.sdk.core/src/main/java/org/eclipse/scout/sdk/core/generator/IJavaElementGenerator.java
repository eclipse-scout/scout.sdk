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
package org.eclipse.scout.sdk.core.generator;

import static java.util.function.Function.identity;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

/**
 * <h3>{@link IJavaElementGenerator}</h3>
 * <p>
 * An {@link ISourceGenerator} that creates source for Java elements.
 *
 * @since 6.1.0
 */
public interface IJavaElementGenerator<TYPE extends IJavaElementGenerator<TYPE>> extends ISourceGenerator<ISourceBuilder<?>> {

  /**
   * Sets the {@link ISourceGenerator} providing the javadoc comment for this {@link IJavaElementGenerator}.
   *
   * @param commentGenerator
   *          The generator for the comment or {@code null} if no comment should be generated.
   * @return This generator
   */
  TYPE withComment(ISourceGenerator<IJavaElementCommentBuilder<?>> commentGenerator);

  /**
   * @return The generator of the javadoc comment of this {@link IJavaElementGenerator}.
   */
  Optional<ISourceGenerator<IJavaElementCommentBuilder<?>>> comment();

  /**
   * Sets the name of this {@link IJavaElementGenerator}.
   *
   * @param newName
   *          The new name or {@code null}.
   * @return This generator
   */
  TYPE withElementName(String newName);

  /**
   * @return The name of the element.
   */
  Optional<String> elementName();

  /**
   * Executes this {@link IJavaElementGenerator} and creates its source in memory.
   * <p>
   * When using this method no {@link IJavaEnvironment} will be used to resolve imports.
   *
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see #toJavaSource(IJavaEnvironment)
   */
  default StringBuilder toJavaSource() {
    return toJavaSource((IJavaEnvironment) null);
  }

  /**
   * Executes this {@link IJavaElementGenerator} and creates its source in memory.
   *
   * @param context
   *          The {@link IJavaEnvironment} in which context the source is created. It will be used to resolve imports.
   *          May be {@code null}.
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see #toJavaSource(IJavaBuilderContext)
   */
  default StringBuilder toJavaSource(IJavaEnvironment context) {
    return toJavaSource(new JavaBuilderContext(context));
  }

  /**
   * Executes this {@link IJavaElementGenerator} and creates its source in memory.
   *
   * @param context
   *          The {@link IJavaBuilderContext} in which the source is created. Must not be {@code null}.
   * @return A {@link StringBuilder} holding the content of the created source.
   * @see #toJavaSource(IJavaEnvironment)
   */
  default StringBuilder toJavaSource(IJavaBuilderContext context) {
    return toSource(identity(), context);
  }
}
