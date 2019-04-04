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

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;

/**
 * <h3>{@link ITypeParameterGenerator}</h3>
 * <p>
 * An {@link IJavaElementGenerator} that creates type parameters.
 *
 * @since 6.1.0
 */
public interface ITypeParameterGenerator<TYPE extends ITypeParameterGenerator<TYPE>> extends IJavaElementGenerator<TYPE> {

  /**
   * Adds the specified bound to this {@link ITypeParameterGenerator}.
   *
   * @param bound
   *          The fully qualified name of the bound to add. Must not be {@code null}.
   * @return This generator.
   */
  TYPE withBound(String bound);

  /**
   * @return A {@link Stream} returning all type parameter bounds.
   */
  Stream<String> bounds();

}
