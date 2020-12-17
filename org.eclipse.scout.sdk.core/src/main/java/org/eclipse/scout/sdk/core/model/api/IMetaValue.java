/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link IMetaValue}</h3>
 *
 * @since 5.1.0
 */
public interface IMetaValue {

  /**
   * @return the real type of the object returned with {@link #as(Class)}
   */
  MetaValueType type();

  /**
   * Gets the value converted into the given class.
   * <p>
   * If the expected type is an array type, the result will never be {@code null} and the array does not contain any
   * {@code null} values.<br>
   * For non-array types the result may be {@code null} only if the compiler was unable to calculate the constant value
   * (which is not allowed according to the JLS). This might happen if invalid Java files are parsed.
   * <p>
   * See <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.7">the JLS</a> for details.
   *
   * @param expectedType
   *          The class it should be converted. Must be compatible with the value returned in {@link #type()}.
   * @return the value converted into the given class.
   */
  <T> T as(Class<T> expectedType);

  /**
   * @return A {@link Stream} of {@link IJavaElement}s that are children of this {@link IMetaValue}. The {@link Stream}
   *         can only contain {@link IAnnotation}s as all oder values are no {@link IJavaElement}s.
   */
  Stream<IJavaElement> children();

  /**
   * Converts this {@link IMetaValue} into a {@link ISourceGenerator} that generates source that is structurally equal
   * to this {@link IMetaValue}. This may be a simple scalar value or a complex annotation structure.
   *
   * @return A generator initialized so that calling {@link ISourceGenerator#generate(ISourceBuilder)} results in
   *         structurally the same source as this {@link IMetaValue} was built on.
   */
  ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy();

  /**
   * Converts this {@link IMetaValue} into a {@link ISourceGenerator} that generates source that is structurally equal
   * to this {@link IMetaValue}. This may be a simple scalar value or a complex annotation structure.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the value to a
   *          {@link ISourceGenerator}. May be {@code null} if no custom transformation is required and the value should
   *          be converted with the default configuration (resulting in structurally the same source as this element was
   *          built on).
   * @return A {@link ISourceGenerator} representing this {@link IMetaValue} as transformed by the specified
   *         {@link IWorkingCopyTransformer}.
   */
  ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer);
}
