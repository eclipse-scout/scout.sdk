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
package org.eclipse.scout.sdk.core.builder.java;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.JavaTypes.ReferenceParser;

/**
 * <h3>{@link IJavaSourceBuilder}</h3>
 * <p>
 * An {@link ISourceBuilder} that provides methods to apply java source fragments.
 *
 * @since 6.1.0
 */
public interface IJavaSourceBuilder<TYPE extends IJavaSourceBuilder<TYPE>> extends ISourceBuilder<TYPE> {

  /**
   * Appends a Java type reference for the specified {@link IType}.
   * <p>
   * An internal {@link IImportValidator} decides whether the reference to this {@link IType} can be simple (in that
   * case an import is registered automatically) or if a full qualification is required.
   * <p>
   * If the specified {@link IType} contains type arguments, these arguments will be appended too.
   *
   * @param t
   *          The {@link IType} to reference. Must not be {@code null}.
   * @return This builder
   * @see IType#reference()
   * @see #ref(CharSequence)
   */
  TYPE ref(IType t);

  /**
   * Appends a Java type reference for the specified {@link CharSequence}.
   * <p>
   * An internal {@link IImportValidator} decides whether the reference can be simple (in that case an import is
   * registered automatically) or if a full qualification is required.
   * <p>
   * If the specified {@link IType} contains type arguments, these arguments will be appended too.
   * <p>
   * <b>Example references:</b>
   * <ul>
   * <li>{@code java.lang.String}</li>
   * <li>{@code java.util.List<java.math.BigDecimal>}</li>
   * <li>{@code java.util.Map<java.lang.Integer, java.util.List<? extends java.lang.CharSequence>>}</li>
   * </ul>
   *
   * @param ref
   *          The type reference to add. Must not be {@code null}.
   * @return This builder
   * @see IImportValidator#useReference(CharSequence)
   * @see ReferenceParser
   */
  TYPE ref(CharSequence ref);

  /**
   * Appends a new Java block start: {
   *
   * @return This builder
   */
  TYPE blockStart();

  /**
   * Appends a new Java block end: }
   *
   * @return This builder
   */
  TYPE blockEnd();

  /**
   * Appends an opening parenthesis: (
   *
   * @return This builder
   */
  TYPE parenthesisOpen();

  /**
   * Appends a closing parenthesis: )
   *
   * @return This builder
   */
  TYPE parenthesisClose();

  /**
   * Appends a type parameter start: &lt;
   *
   * @return This builder
   */
  TYPE genericStart();

  /**
   * Appends a type parameter end: &gt;
   *
   * @return This builder
   */
  TYPE genericEnd();

  /**
   * Appends the annotation @ sign.
   *
   * @return This builder
   */
  TYPE atSign();

  /**
   * Appends the equal sign (=) including a leading and trailing space.
   *
   * @return This builder
   */
  TYPE equalSign();

  /**
   * Appends a dot: .
   *
   * @return This builder
   */
  TYPE dotSign();

  /**
   * Appends a semicolon: ;
   *
   * @return This builder
   */
  TYPE semicolon();

  /**
   * Appends a comma: ,
   *
   * @return This builder
   */
  TYPE comma();

  /**
   * @return The {@link IJavaBuilderContext} of this {@link IJavaSourceBuilder}.
   */
  @Override
  IJavaBuilderContext context();

  /**
   * Appends the specified references using {@link #ref(CharSequence)} for each reference.
   *
   * @param references
   *          The references to append.
   * @param prefix
   *          The {@link CharSequence} to be appended before the first reference. If the {@link Stream} does not provide
   *          any references, the prefix is not appended. If the prefix is {@code null}, nothing will be appended.
   * @param delimiter
   *          The {@link CharSequence} to be appended between two references. If the {@link Stream} is empty or does
   *          only contain one reference, no delimiter is appended. If the delimiter is {@code null}, nothing will be
   *          appended between two references.
   * @param suffix
   *          The {@link CharSequence} to be appended after the last reference. If the {@link Stream} does not provide
   *          any references, the suffix is not appended. If the suffix is {@code null}, nothing will be appended.
   * @return This builder
   * @see #ref(CharSequence)
   */
  TYPE appendReferences(Stream<? extends CharSequence> references, CharSequence prefix, CharSequence delimiter, CharSequence suffix);
}
