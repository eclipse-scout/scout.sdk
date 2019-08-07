/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api.query;

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
}
