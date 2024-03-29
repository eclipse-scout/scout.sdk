/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.typeparam;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link TypeParameterGenerator}</h3>
 *
 * @since 6.1.0
 */
public class TypeParameterGenerator<TYPE extends ITypeParameterGenerator<TYPE>> extends AbstractJavaElementGenerator<TYPE> implements ITypeParameterGenerator<TYPE> {

  private final List<JavaBuilderContextFunction<String>> m_bounds;

  protected TypeParameterGenerator() {
    m_bounds = new ArrayList<>();
  }

  protected TypeParameterGenerator(ITypeParameter param) {
    super(param);
    m_bounds = param.bounds()
        .map(IType::reference)
        .map(JavaBuilderContextFunction::create)
        .collect(toList());
  }

  /**
   * @return A new empty {@link ITypeParameterGenerator}.
   */
  public static ITypeParameterGenerator<?> create() {
    return new TypeParameterGenerator<>();
  }

  /**
   * Creates a new {@link ITypeParameterGenerator} based on the given {@link ITypeParameter}.
   *
   * @param param
   *          The {@link ITypeParameter} that should be converted to an {@link ITypeParameterGenerator}. Must not be
   *          {@code null}.
   * @return A new {@link ITypeParameterGenerator} initialized to generate source that is structurally similar to the
   *         one from the given {@link ITypeParameter}.
   */
  public static ITypeParameterGenerator<?> create(ITypeParameter param) {
    return new TypeParameterGenerator<>(param);
  }

  @Override
  public TYPE withBinding(String binding) {
    if (Strings.hasText(binding)) {
      m_bounds.add(JavaBuilderContextFunction.create(binding));
    }
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withBindingFrom(Class<A> apiDefinition, Function<A, String> bindingSupplier) {
    if (bindingSupplier != null) {
      m_bounds.add(new ApiFunction<>(apiDefinition, bindingSupplier));
    }
    return thisInstance();
  }

  @Override
  public TYPE withBindingFunc(Function<IJavaBuilderContext, String> bindingSupplier) {
    if (bindingSupplier != null) {
      m_bounds.add(JavaBuilderContextFunction.create(bindingSupplier));
    }
    return thisInstance();
  }

  @Override
  public Stream<String> bounds() {
    return boundsFunc()
        .map(JavaBuilderContextFunction::apply)
        .flatMap(Optional::stream);
  }

  @Override
  public Stream<JavaBuilderContextFunction<String>> boundsFunc() {
    return m_bounds.stream();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    builder.append(ensureValidJavaName(elementName(builder.context()).orElse(Character.toString(JavaTypes.C_QUESTION_MARK))));
    var bounds = m_bounds.stream()
        .<ISourceGenerator<IJavaSourceBuilder<?>>> map(binding -> b -> b.refFunc(binding))
        .map(g -> g.generalize(JavaSourceBuilder::create));
    builder.append(bounds, " " + JavaTypes.EXTENDS + " ", " & ", null);
  }
}
