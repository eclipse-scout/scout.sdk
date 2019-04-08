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
package org.eclipse.scout.sdk.core.generator.typeparam;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link TypeParameterGenerator}</h3>
 *
 * @since 6.1.0
 */
public class TypeParameterGenerator<TYPE extends ITypeParameterGenerator<TYPE>> extends AbstractJavaElementGenerator<TYPE> implements ITypeParameterGenerator<TYPE> {

  private final List<String> m_bounds;

  protected TypeParameterGenerator() {
    m_bounds = new ArrayList<>();
  }

  protected TypeParameterGenerator(ITypeParameter param) {
    super(param);
    m_bounds = param.bounds()
        .map(IType::reference)
        .collect(toList());
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

  /**
   * @return A new empty {@link ITypeParameterGenerator}.
   */
  public static ITypeParameterGenerator<?> create() {
    return new TypeParameterGenerator<>();
  }

  @Override
  public TYPE withBound(String bound) {
    m_bounds.add(Ensure.notNull(bound));
    return currentInstance();
  }

  @Override
  public Stream<String> bounds() {
    return m_bounds.stream();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    builder.append(ensureValidJavaName(elementName().orElse("?")));
    Stream<ISourceGenerator<ISourceBuilder<?>>> bounds = m_bounds.stream()
        .<ISourceGenerator<IJavaSourceBuilder<?>>> map(bound -> b -> b.ref(bound))
        .map(g -> g.generalize(JavaSourceBuilder::create));
    builder.append(bounds, " extends ", " & ", null);
  }
}