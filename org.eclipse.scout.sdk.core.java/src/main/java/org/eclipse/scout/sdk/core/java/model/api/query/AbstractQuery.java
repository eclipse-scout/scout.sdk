/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.query;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <h3>{@link AbstractQuery}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractQuery<TYPE> implements IQuery<TYPE> {

  protected abstract Stream<TYPE> createStream();

  @Override
  public Stream<TYPE> stream() {
    return createStream();
  }

  @Override
  public Optional<TYPE> first() {
    return stream().findAny();
  }

  @Override
  public boolean existsAny() {
    return first().isPresent();
  }

  @Override
  public Optional<TYPE> item(int index) {
    return stream().skip(index).findAny();
  }

  @Override
  public int count() {
    //noinspection NumericCastThatLosesPrecision
    return (int) stream().count();
  }
}
