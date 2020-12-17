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
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <h3>{@link IQuery}</h3>
 *
 * @since 6.1.0
 */
public interface IQuery<TYPE> {

  /**
   * @return The elements that match this {@link IQuery} as {@link Stream}.
   */
  Stream<TYPE> stream();

  /**
   * @return The first element that matches this {@link IQuery}.
   */
  Optional<TYPE> first();

  /**
   * @return {@code true} if there exists at least one element that matches this {@link IQuery}. {@code false}
   *         otherwise.
   */
  boolean existsAny();

  /**
   * @param index
   *          The zero based index of the element matching this query that should be returned.
   * @return The element at the specified position matching this {@link IQuery} or an empty {@link Optional} if no such
   *         element exists.
   * @throws IllegalArgumentException
   *           If the index is smaller than zero.
   */
  Optional<TYPE> item(int index);
}
