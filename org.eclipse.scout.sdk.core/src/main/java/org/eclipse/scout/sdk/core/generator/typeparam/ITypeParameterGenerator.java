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
package org.eclipse.scout.sdk.core.generator.typeparam;

import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.util.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.util.apidef.IApiSpecification;

/**
 * <h3>{@link ITypeParameterGenerator}</h3>
 * <p>
 * An {@link IJavaElementGenerator} that creates type parameters.
 *
 * @since 6.1.0
 */
public interface ITypeParameterGenerator<TYPE extends ITypeParameterGenerator<TYPE>> extends IJavaElementGenerator<TYPE> {

  /**
   * Adds the specified binding to this {@link ITypeParameterGenerator}.
   *
   * @param binding
   *          The fully qualified name of the binding to add. Must not be {@code null}.
   * @return This generator.
   */
  TYPE withBinding(String binding);

  <A extends IApiSpecification> TYPE withBindingFrom(Class<A> apiDefinition, Function<A, String> bindingSupplier);

  /**
   * @return A {@link Stream} returning all type parameter bounds.
   */
  Stream<ApiFunction<?, String>> bounds();

}
