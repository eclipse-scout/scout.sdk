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
package org.eclipse.scout.sdk.core.builder.java.expression;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilderWrapper;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ExpressionBuilder}</h3>
 *
 * @since 6.1.0
 */
public class ExpressionBuilder<TYPE extends IExpressionBuilder<TYPE>> extends JavaSourceBuilderWrapper<TYPE> implements IExpressionBuilder<TYPE> {

  protected ExpressionBuilder(ISourceBuilder<?> inner) {
    super(inner);
  }

  /**
   * Creates a new {@link IExpressionBuilder} wrapping the given inner {@link ISourceBuilder}.
   * <p>
   * If the context of the inner {@link ISourceBuilder} is an {@link IJavaBuilderContext}, this context and its
   * {@link IJavaEnvironment} is re-used. Otherwise a new {@link IJavaBuilderContext} without a {@link IJavaEnvironment}
   * is created.
   *
   * @param inner
   *          The inner {@link ISourceBuilder}. Must not be {@code null}.
   * @return A new {@link IExpressionBuilder}.
   */
  public static IExpressionBuilder<?> create(ISourceBuilder<?> inner) {
    return new ExpressionBuilder<>(inner);
  }

  @Override
  public TYPE classLiteral(CharSequence reference) {
    return ref(reference).append(JavaTypes.CLASS_FILE_SUFFIX);
  }

  @Override
  public <T extends IApiSpecification> TYPE classLiteralFrom(Class<T> apiClass, Function<T, ITypeNameSupplier> nameSupplier) {
    return refClassFrom(apiClass, nameSupplier).append(JavaTypes.CLASS_FILE_SUFFIX);
  }

  @Override
  public TYPE classLiteralFunc(Function<IJavaBuilderContext, ITypeNameSupplier> func) {
    return refClassFunc(func).append(JavaTypes.CLASS_FILE_SUFFIX);
  }

  @Override
  public TYPE appendNew() {
    return append("new ");
  }

  @Override
  public <API extends IApiSpecification> TYPE appendNewFrom(Class<API> apiClass, Function<API, ITypeNameSupplier> sourceProvider) {
    return appendNew().refClassFrom(apiClass, sourceProvider).parenthesisOpen();
  }

  @Override
  public TYPE appendNew(CharSequence ref) {
    return appendNew().ref(ref).parenthesisOpen();
  }

  @Override
  public TYPE appendThrow() {
    return append("throw ");
  }

  @Override
  public TYPE appendNot() {
    return append('!');
  }

  @Override
  public TYPE appendIf() {
    return append("if").space().parenthesisOpen();
  }

  @Override
  public TYPE stringLiteral(CharSequence literalValue) {
    if (literalValue == null) {
      return nullLiteral();
    }
    return append(Strings.toStringLiteral(literalValue));
  }

  @Override
  public TYPE appendDefaultValueOf(CharSequence dataTypeFqn) {
    var defaultVal = JavaTypes.defaultValueOf(dataTypeFqn);
    if (defaultVal != null) {
      return append(defaultVal);
    }
    return thisInstance();
  }

  @Override
  public TYPE enumValue(CharSequence enumType, CharSequence enumField) {
    return ref(enumType).dot().append(Ensure.notNull(enumField));
  }

  @Override
  public TYPE stringLiteralArray(CharSequence... elements) {
    return stringLiteralArray(elements, false);
  }

  @Override
  public TYPE stringLiteralArray(CharSequence[] elements, boolean formatWithNewlines) {
    return stringLiteralArray(elements, formatWithNewlines, false);
  }

  @Override
  public TYPE stringLiteralArray(CharSequence[] elements, boolean formatWithNewlines, boolean stringLiteralOnSingleElementArray) {
    Ensure.notNull(elements);
    if (stringLiteralOnSingleElementArray && elements.length == 1) {
      return stringLiteral(elements[0]);
    }
    return stringLiteralArray(Arrays.stream(elements), formatWithNewlines);
  }

  @Override
  public TYPE stringLiteralArray(Stream<? extends CharSequence> elements, boolean formatWithNewlines) {
    var stringLiteralGenerators = Ensure.notNull(elements)
        .<ISourceGenerator<IExpressionBuilder<?>>> map(e -> b -> b.stringLiteral(e))
        .map(g -> g.generalize(ExpressionBuilder::create));
    return array(stringLiteralGenerators, formatWithNewlines);
  }

  @Override
  public TYPE nullLiteral() {
    return append("null");
  }

  @Override
  public TYPE array(Stream<? extends ISourceGenerator<ISourceBuilder<?>>> elements, boolean formatWithNewlines) {
    String blockSeparator;
    String elementSeparator;
    if (formatWithNewlines) {
      blockSeparator = context().lineDelimiter();
      elementSeparator = JavaTypes.C_COMMA + blockSeparator;
    }
    else {
      blockSeparator = null;
      elementSeparator = JavaTypes.C_COMMA + " ";
    }

    return blockStart()
        .append(Ensure.notNull(elements), blockSeparator, elementSeparator, blockSeparator)
        .blockEnd();
  }
}
