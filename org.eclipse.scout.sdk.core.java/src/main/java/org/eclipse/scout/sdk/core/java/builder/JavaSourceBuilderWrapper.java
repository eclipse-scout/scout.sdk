/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.builder;

import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.SourceBuilderWrapper;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IType;

/**
 * <h3>{@link JavaSourceBuilderWrapper}</h3>
 *
 * @since 6.1.0
 */
public class JavaSourceBuilderWrapper<TYPE extends IJavaSourceBuilder<TYPE>> extends SourceBuilderWrapper<TYPE> implements IJavaSourceBuilder<TYPE> {

  public JavaSourceBuilderWrapper(ISourceBuilder<?> inner) {
    super(AbstractJavaElementGenerator.ensureJavaSourceBuilder(inner));
  }

  @Override
  public IJavaSourceBuilder<?> inner() {
    return (IJavaSourceBuilder<?>) super.inner();
  }

  @Override
  public IJavaBuilderContext context() {
    return (IJavaBuilderContext) super.context();
  }

  @Override
  public TYPE ref(IType t) {
    inner().ref(t);
    return thisInstance();
  }

  @Override
  public TYPE ref(CharSequence ref) {
    inner().ref(ref);
    return thisInstance();
  }

  @Override
  public TYPE blockStart() {
    inner().blockStart();
    return thisInstance();
  }

  @Override
  public TYPE blockEnd() {
    inner().blockEnd();
    return thisInstance();
  }

  @Override
  public TYPE at() {
    inner().at();
    return thisInstance();
  }

  @Override
  public TYPE parenthesisOpen() {
    inner().parenthesisOpen();
    return thisInstance();
  }

  @Override
  public TYPE parenthesisClose() {
    inner().parenthesisClose();
    return thisInstance();
  }

  @Override
  public TYPE equalSign() {
    inner().equalSign();
    return thisInstance();
  }

  @Override
  public TYPE dot() {
    inner().dot();
    return thisInstance();
  }

  @Override
  public TYPE comma() {
    inner().comma();
    return thisInstance();
  }

  @Override
  public TYPE semicolon() {
    inner().semicolon();
    return thisInstance();
  }

  @Override
  public TYPE references(Stream<? extends CharSequence> refs, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    inner().references(refs, prefix, delimiter, suffix);
    return thisInstance();
  }

  @Override
  public TYPE referencesFrom(Stream<Function<IJavaBuilderContext, ? extends CharSequence>> references, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    inner().referencesFrom(references, prefix, delimiter, suffix);
    return thisInstance();
  }

  @Override
  public <API extends IApiSpecification> TYPE refFrom(Class<API> apiClass, Function<API, ? extends CharSequence> sourceProvider) {
    inner().refFrom(apiClass, sourceProvider);
    return thisInstance();
  }

  @Override
  public TYPE refFunc(Function<IJavaBuilderContext, ? extends CharSequence> func) {
    inner().refFunc(func);
    return thisInstance();
  }

  @Override
  public <API extends IApiSpecification> TYPE refClassFrom(Class<API> apiClass, Function<API, ITypeNameSupplier> sourceProvider) {
    inner().refClassFrom(apiClass, sourceProvider);
    return thisInstance();
  }

  @Override
  public TYPE refClassFunc(Function<IJavaBuilderContext, ITypeNameSupplier> func) {
    inner().refClassFunc(func);
    return thisInstance();
  }

  @Override
  public <API extends IApiSpecification> TYPE appendFrom(Class<API> apiClass, Function<API, ? extends CharSequence> sourceProvider) {
    inner().appendFrom(apiClass, sourceProvider);
    return thisInstance();
  }

  @Override
  public TYPE appendFunc(Function<IJavaBuilderContext, ? extends CharSequence> sourceProvider) {
    inner().appendFunc(sourceProvider);
    return thisInstance();
  }
}
