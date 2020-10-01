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
package org.eclipse.scout.sdk.core.model.api;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link IJavaElement}</h3>Represents a Java element.
 *
 * @since 5.1.0
 */
public interface IJavaElement {

  /**
   * Gets the {@link IJavaEnvironment} this element belongs to.
   *
   * @return The owning {@link IJavaEnvironment}.
   */
  IJavaEnvironment javaEnvironment();

  /**
   * @return the name of this element.
   */
  String elementName();

  /**
   * @return The source of the element.<br>
   *         The source is only available if the compilation unit is one of the following:
   *         <ul>
   *         <li>source</li>
   *         <li>class in jar and source attachment to jar is defined</li>
   *         </ul>
   */
  Optional<ISourceRange> source();

  /**
   * Unwraps the java element into its underlying SPI class.
   *
   * @return The service provider interface that belongs to the receiver.
   */
  JavaElementSpi unwrap();

  /**
   * Converts this parsed {@link IJavaElement} into a modifiable working copy.
   *
   * @return This {@link IJavaElement} as {@link ISourceGenerator}. The generator is initialized so that calling
   *         {@link ISourceGenerator#generate(ISourceBuilder)} results in structurally the same source as this
   *         {@link IJavaElement} was built on.
   */
  ISourceGenerator<ISourceBuilder<?>> toWorkingCopy();

  /**
   * Converts this {@link IJavaElement} into a modifiable working copy.
   * <p>
   * The {@link IWorkingCopyTransformer} acts like a visitor and allows to transform each <u>child</u>
   * {@link IJavaElement element} into a custom {@link ISourceGenerator}.
   * <p>
   * Please note that the given transformer is NOT called for the receiver (the root element the transformation was
   * started on) but for its children only! To modify the root generator us the resulting {@link ISourceGenerator}
   * directly.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the element to a
   *          working copy. May be {@code null} if no custom transformation is required and the element should be
   *          converted into a working copy with the default configuration (resulting in structurally the same source as
   *          this element was built on).
   * @return A {@link ISourceGenerator} representing this {@link IJavaElement} as transformed by the specified
   *         {@link IWorkingCopyTransformer}.
   */
  ISourceGenerator<ISourceBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer);

  /**
   * @return A {@link Stream} of {@link IJavaElement}s that are direct children of this element.
   */
  Stream<? extends IJavaElement> children();

  /**
   * Visits this {@link IJavaElement} and all of its child elements recursively using a Breadth-First (level-order)
   * strategy.
   * <p>
   * The specified {@link IBreadthFirstJavaElementVisitor} controls the visited elements using the
   * {@link TreeVisitResult} returned.
   *
   * @param visitor
   *          The {@link IBreadthFirstJavaElementVisitor} to use. Must not be {@code null}.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IBreadthFirstJavaElementVisitor}.
   * @throws IllegalArgumentException
   *           if the visitor is {@code null}.
   * @see IBreadthFirstJavaElementVisitor
   * @see TreeVisitResult
   */
  TreeVisitResult visit(IBreadthFirstJavaElementVisitor visitor);

  /**
   * Visits this {@link IJavaElement} and all of its child elements recursively using a Depth-First strategy. Depending
   * on the visitor a pre-order (top-down) or post-order (bottom-up) traversal can be implemented.
   * <p>
   * The specified {@link IDepthFirstJavaElementVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned.
   *
   * @param visitor
   *          The {@link IDepthFirstJavaElementVisitor} to call. Must not be {@code null}.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IDepthFirstJavaElementVisitor}.
   * @throws IllegalArgumentException
   *           if the visitor is {@code null}.
   * @see IDepthFirstJavaElementVisitor
   * @see TreeVisitResult
   */
  TreeVisitResult visit(IDepthFirstJavaElementVisitor visitor);

  /**
   * Visits this {@link IJavaElement} and all of its child elements recursively (if matching the specified type) by
   * using a pre-order (top-down) traversal strategy.
   * <p>
   * The specified {@link Function} controls the visited elements using the {@link TreeVisitResult} returned.
   * <p>
   * The specified {@link Function} is only called for {@link IJavaElement}s that are of the specified type.
   *
   * @param visitor
   *          The {@link Function} to call for each {@link IJavaElement} that matches the specified type. Must not be
   *          {@code null}.
   * @param type
   *          The type of {@link IJavaElement} that should be visited.
   * @param <T>
   *          The type of {@link IJavaElement} that should be visited.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link Function}.
   * @throws IllegalArgumentException
   *           if one of the arguments is {@code null}.
   * @see TreeVisitResult
   */
  <T extends IJavaElement> TreeVisitResult visit(Function<T, TreeVisitResult> visitor, Class<T> type);

  /**
   * Visits this {@link IJavaElement} and all of its child elements recursively by using a pre-order (top-down)
   * traversal strategy.
   * <p>
   * The specified {@link Function} controls the visited elements using the {@link TreeVisitResult} returned.
   *
   * @param visitor
   *          The {@link Function} to call for each {@link IJavaElement}. Must not be {@code null}.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link Function}.
   * @throws IllegalArgumentException
   *           if the visitor is {@code null}.
   * @see TreeVisitResult
   */
  TreeVisitResult visit(Function<IJavaElement, TreeVisitResult> visitor);

  /**
   * Visits this {@link IJavaElement} and all of its child elements recursively (if matching the specified type) by
   * using a pre-order (top-down) traversal strategy.
   * <p>
   * The specified {@link Consumer} is only called for {@link IJavaElement}s that are of the specified type.
   *
   * @param visitor
   *          The {@link Consumer} to call for each {@link IJavaElement} that matches the specified type. Must not be
   *          {@code null}.
   * @param type
   *          The type of {@link IJavaElement} that should be visited.
   * @param <T>
   *          The type of {@link IJavaElement} that should be visited.
   * @throws IllegalArgumentException
   *           if one of the arguments is {@code null}.
   */
  <T extends IJavaElement> void visit(Consumer<T> visitor, Class<T> type);

  /**
   * Visits this {@link IJavaElement} and all of its child elements recursively by using a pre-order (top-down)
   * traversal strategy.
   *
   * @param visitor
   *          The {@link Consumer} to call for each {@link IJavaElement}. Must not be {@code null}.
   * @throws IllegalArgumentException
   *           if the visitor is {@code null}.
   */
  TreeVisitResult visit(Consumer<IJavaElement> visitor);

}
