/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java;

import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
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
   * Appends the annotation @ sign.
   *
   * @return This builder
   */
  TYPE at();

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
  TYPE dot();

  /**
   * Appends a comma: ,
   * 
   * @return This builder
   */
  TYPE comma();

  /**
   * Appends a semicolon: ;
   *
   * @return This builder
   */
  TYPE semicolon();

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
   * @see #referencesFrom(Stream, CharSequence, CharSequence, CharSequence)
   */
  TYPE references(Stream<? extends CharSequence> references, CharSequence prefix, CharSequence delimiter, CharSequence suffix);

  /**
   * Appends a Java type reference for the result of the specified {@link Function}.
   * <p>
   * This method may be handy if the reference to append changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * 
   * @param apiClass
   *          The API type that contains the {@link CharSequence} to append. An instance of this type is passed to the
   *          sourceProvider. May be {@code null} in case the given sourceProvider can handle a {@code null} input.
   * @param sourceProvider
   *          A {@link Function} to be called to obtain the reference that should be appended. Must not be {@code null}
   *          and the function must not return {@code null}.
   * @param <API>
   *          The API type that contains the {@link CharSequence}
   * @return This builder
   * @see #ref(CharSequence)
   * @see #refFunc(Function)
   * @see #references(Stream, CharSequence, CharSequence, CharSequence)
   */
  <API extends IApiSpecification> TYPE refFrom(Class<API> apiClass, Function<API, ? extends CharSequence> sourceProvider);

  /**
   * Appends a Java type reference for the result of the specified {@link Function}.
   * <p>
   * This method may be handy if the reference to append depends on the {@link #context()} of this builder.
   * </p>
   *
   * @param func
   *          A {@link Function} to be called to obtain the reference that should be appended. Must not be {@code null}
   *          and the function must not return {@code null}.
   * @return This builder
   * @see #ref(CharSequence)
   * @see #refFrom(Class, Function)
   * @see #references(Stream, CharSequence, CharSequence, CharSequence)
   */
  TYPE refFunc(Function<IJavaBuilderContext, ? extends CharSequence> func);

  /**
   * Appends a reference to a fully qualified class name obtained from an {@link IApiSpecification}. The fully qualified
   * name to use is obtained by using the {@link ITypeNameSupplier} returned by invoking the given sourceProvider.
   * <p>
   * This method may be handy if the name of a class changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code builder.refClassFrom(IJavaApi.class, IJavaApi::Long)}.
   * 
   * @param apiClass
   *          The api type that contains the class name. An instance of this type is passed to the sourceProvider. May
   *          be {@code null} in case the given sourceProvider can handle a {@code null} input.
   * @param sourceProvider
   *          A {@link Function} to be called to obtain the {@link ITypeNameSupplier} whose fully qualified name should
   *          be added as class reference. Must not be {@code null} and the function must return a not {@code null}
   *          {@link ITypeNameSupplier} which must not return {@code null}.
   * @param <API>
   *          The api type that contains the class name
   * @return This builder
   * @see #ref(CharSequence)
   * @see #refClassFunc(Function)
   * @see #references(Stream, CharSequence, CharSequence, CharSequence)
   */
  <API extends IApiSpecification> TYPE refClassFrom(Class<API> apiClass, Function<API, ITypeNameSupplier> sourceProvider);

  /**
   * Appends a reference to a fully qualified class name obtained by using the {@link ITypeNameSupplier} returned by
   * invoking the given {@link Function}.
   * <p>
   * This method may be handy if the name of a class depends on the {@link #context()} of this builder.
   * </p>
   *
   * @param func
   *          A {@link Function} to be called to obtain the {@link ITypeNameSupplier} whose fully qualified name should
   *          be added as class reference. Must not be {@code null} and the function must return a not {@code null}
   *          {@link ITypeNameSupplier} which returns a class name (not empty).
   * @return This builder
   * @see #ref(CharSequence)
   * @see #refClassFrom(Class, Function)
   * @see #references(Stream, CharSequence, CharSequence, CharSequence)
   */
  TYPE refClassFunc(Function<IJavaBuilderContext, ITypeNameSupplier> func);

  /**
   * Appends the {@link CharSequence} returned by the given sourceProvider.
   * <p>
   * This method may be handy if the {@link CharSequence} to append changes between different versions of an API. The
   * builder then decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code sourceBuilder.appendFrom(IJavaApi.class, IJavaApi::myStringToAppend)}
   * 
   * @param apiClass
   *          The API type that contains the {@link CharSequence} to append. An instance of this type is passed to the
   *          sourceProvider. May be {@code null} in case the given sourceProvider can handle a {@code null} input.
   * @param sourceProvider
   *          A {@link Function} to be called to obtain the {@link CharSequence} that should be appended. Must not be
   *          {@code null} and the function must not return {@code null}.
   * @param <API>
   *          The API type that contains the {@link CharSequence}
   * @return This builder
   * @see #append(CharSequence)
   */
  <API extends IApiSpecification> TYPE appendFrom(Class<API> apiClass, Function<API, ? extends CharSequence> sourceProvider);

  /**
   * Appends the {@link CharSequence} returned by the given {@link Function}.
   * <p>
   * This method may be handy if the {@link CharSequence} to append depends on the {@link #context()} of this builder.
   * </p>
   *
   * @param sourceProvider
   *          A {@link Function} to be called to obtain the {@link CharSequence} that should be appended. Must not be
   *          {@code null} and the function must not return {@code null}.
   * @return This builder
   * @see #append(CharSequence)
   */
  TYPE appendFunc(Function<IJavaBuilderContext, ? extends CharSequence> sourceProvider);

  /**
   * Appends the results of all {@link Function functions} of the stream given.
   * 
   * @param references
   *          The references to append. May not be {@code null}.
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
   * @see #references(Stream, CharSequence, CharSequence, CharSequence)
   */
  TYPE referencesFrom(Stream<Function<IJavaBuilderContext, ? extends CharSequence>> references, CharSequence prefix, CharSequence delimiter, CharSequence suffix);

  /**
   * @return The {@link IJavaBuilderContext} of this {@link IJavaSourceBuilder}.
   */
  @Override
  IJavaBuilderContext context();
}
