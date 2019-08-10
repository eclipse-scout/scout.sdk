/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.SourceBuilderWrapper;
import org.eclipse.scout.sdk.core.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.model.api.IType;

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
    return currentInstance();
  }

  @Override
  public TYPE ref(CharSequence ref) {
    inner().ref(ref);
    return currentInstance();
  }

  @Override
  public TYPE blockStart() {
    inner().blockStart();
    return currentInstance();
  }

  @Override
  public TYPE blockEnd() {
    inner().blockEnd();
    return currentInstance();
  }

  @Override
  public TYPE atSign() {
    inner().atSign();
    return currentInstance();
  }

  @Override
  public TYPE parenthesisOpen() {
    inner().parenthesisOpen();
    return currentInstance();
  }

  @Override
  public TYPE parenthesisClose() {
    inner().parenthesisClose();
    return currentInstance();
  }

  @Override
  public TYPE genericStart() {
    inner().genericStart();
    return currentInstance();
  }

  @Override
  public TYPE genericEnd() {
    inner().genericEnd();
    return currentInstance();
  }

  @Override
  public TYPE equalSign() {
    inner().equalSign();
    return currentInstance();
  }

  @Override
  public TYPE dotSign() {
    inner().dotSign();
    return currentInstance();
  }

  @Override
  public TYPE semicolon() {
    inner().semicolon();
    return currentInstance();
  }

  @Override
  public TYPE comma() {
    inner().comma();
    return currentInstance();
  }

  @Override
  public TYPE appendReferences(Stream<? extends CharSequence> refs, CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
    inner().appendReferences(refs, prefix, delimiter, suffix);
    return currentInstance();
  }
}
